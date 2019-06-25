package com.smarterhomes.wateronmetermap

import android.annotation.SuppressLint

import android.app.Activity

import android.app.AlertDialog
import android.arch.persistence.room.Room
import android.content.*
import android.content.IntentSender.SendIntentException

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.DocumentsContract.isDocumentUri
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import android.support.v4.provider.DocumentFile
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.Drive

import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.api.client.http.FileContent
import com.google.api.services.drive.DriveScopes
import com.smarterhomes.wateronmetermap.Helpers.DriveServiceHelper
import com.smarterhomes.wateronmetermap.Helpers.InstallerHelper
import com.smarterhomes.wateronmetermap.Interfaces.InstallerInterface
import com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase.AppDataBase
import com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase.UploadDatabase
import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData
import org.jetbrains.anko.doAsync
import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
class MainActivity :SelectSocietyActivity(), ConnectionCallbacks, OnConnectionFailedListener,InstallerInterface {

//    private val
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mBitmapToSave: Bitmap? = null

    var currentPhotoPath: String? = null

    private var f: File?=null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        Log.d(TAG,"Called on firstRun");
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);
        f = File.createTempFile("JPEG_"+preferences.getString("mtrLocation","")+"_"+preferences.getString("mtrPoint",""), /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */)
        Log.d("f path",f!!.path)

        f.apply {
            currentPhotoPath = this!!.absolutePath
        }
        return f!!
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.smarterhomes.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CODE_CAPTURE_IMAGE)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            Log.i(TAG, "API client connected.")
            dispatchTakePictureIntent()
            return
        }
//        requestSignIn()
        identifyTextAndSave();
    }

    override fun onPause() {

        Log.d("Calling on Pause ","now");
        super.onPause()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

//    private var dateBundle: Intent

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CAPTURE_IMAGE ->{
                // Called after a photo has been taken.

                Log.d("REQ&RESP",requestCode.toString()+"&"+resultCode.toString())
                if (resultCode == Activity.RESULT_OK) {
                    // Store the image data as a bitmap for writing later.
                    val file = File(f!!.absolutePath)

                    val `is` = contentResolver.openInputStream(Uri.fromFile(file))

                    var bitmap: Bitmap
                    try {
                        bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                        mBitmapToSave = bitmap;
                         val options = BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inSampleSize = 2;
                        options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
                        mBitmapToSave = BitmapFactory.decodeStream(`is`, null, options);

//                        val strPath = FileUtils.getPath(this, Uri.fromFile(file))
//                        Log.d("FilePathFound",strPath)
//                        System.out.println("FilePathFound :"+strPath)

                    } catch (e: FileNotFoundException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }

                }else if(resultCode == Activity.RESULT_CANCELED){
                    Log.e("result","canceled")
                    startActivity(Intent(applicationContext,SelectSocietyActivity::class.java))
                    finish();
                }
            }

            WRITE_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data!=null){
                    data.data?.also { uri ->
                        Log.i(TAG, "Uri: $uri")
                        writeFileContentToUri(uri)
//                        var documentFile = DocumentFile.fromTreeUri(this, uri);

                    //Then we split file path into array of strings.
                    //ex: parts:{"", "storage", "extSdCard", "MyFolder", "MyFolder", "myImage.jpg"}
                    // There is a reason for having two similar names "MyFolder" in
                    //my exmple file path to show you similarity in names in a path will not
                    //distract our hiarchy search that is provided below.
//                    String[] parts = (file.getPath()).split("\\/");
//
//                    // findFile method will search documentFile for the first file
//                    // with the expected `DisplayName`
//
//                    // We skip first three items because we are already on it.(sdCardUri = /storage/extSdCard)
//                    for (int i = 3; i < parts.length; i++) {
//                        if (documentFile != null) {
//                            documentFile = documentFile.findFile(parts[i]);
//                        }
//                      }
//
//                    if (documentFile == null) {
//
//                        // File not found on tree search
//                        // User selected a wrong directory as the sd-card
//                        // Here must inform user about how to get the correct sd-card
//                        // and invoke file chooser dialog again.
//
//                     } else {
//
//                        // File found on sd-card and it is a correct sd-card directory
//                        // save this path as a root for sd-card on your database(SQLite, XML, txt,...)
//
//                        // Now do whatever you like to do with documentFile.
//                        // Here I do deletion to provide an example.
//
//
//                    }
                    }

                }
            }

            REQUEST_CODE_SIGN_IN_NEW->{
                if(resultCode == Activity.RESULT_OK && data!=null){
//                    val intent = Intent(this,SelectSocietyActivity::class.java)
//                    intent.putExtra("note",1)
//                    startActivity(intent)
//                    this.finish()
                }
            }

        }
    }

    private var mDriveServiceHelper: DriveServiceHelper?= null

    private var textDeteted: String? = ""

    private fun identifyTextAndSave() {
       doAsync {
           val textRecognizer = TextRecognizer.Builder(applicationContext).build();
           var state:String = ""
           try {
               if (!textRecognizer.isOperational()) {
                   AlertDialog.
                           Builder(applicationContext).
                           setMessage("Text recognizer could not be set up on your device").show();

               }else{
                   val frame = Frame.Builder().setRotation(1).setBitmap(mBitmapToSave).build();
                   val origTextBlocks = textRecognizer.detect(frame);
                   val textBlocks = ArrayList<TextBlock>();
                   for (i in 0..origTextBlocks.size()-1) {
                       val textBlock = origTextBlocks.valueAt(i);
                       textBlocks.add(textBlock);
                   }
                   Collections.sort(textBlocks, object : Comparator<TextBlock> {
                       override fun compare(o1: TextBlock, o2: TextBlock): Int {
                           val diffOfTops = o1.boundingBox.top - o2.boundingBox.top
                           val diffOfLefts = o1.boundingBox.left - o2.boundingBox.left
                           return if (diffOfTops != 0) {
                               diffOfTops
                           } else diffOfLefts
                       }
                   })

                   val detectedText = StringBuilder()
                   for ( textBlock in textBlocks) {
                       if (textBlock != null && textBlock.getValue() != null) {
                           detectedText.append(textBlock.getValue());
                           detectedText.append("\n");
                       }
                   }
                   state = getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).getString("stateCheck","")
                   val locationName = getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).getString("mtrLocation","")
                   val MtrPoint = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).getString("mtrPoint","");
                   textDeteted = locationName+"_"+MtrPoint+"_"+state+"_"+"Test"
               }

           }
           finally {
               textRecognizer.release();
               Log.d("DetetedText:state", textDeteted.toString()+":"+state)


               var imgBytes = getBytesFromBitmap(mBitmapToSave)




               if(!state.equals("")){
                   val storeTask = Thread(Runnable {
                       val preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);
                       val stateImg = preferences.getString("stateCheck","")
                       var imgBytes = getBytesFromBitmap(mBitmapToSave)

                       /*try {

                           if (SelectSocietyActivity.mDriveServiceHelper!=null){
                               //createFileInFolder()

                           }

                       }finally {

                           val apt_id =  preferences.getInt("aptId",-1);
                           val mtrpt_id =  preferences.getString("mtrPoint","")
                           val mail_id = preferences.getString("mailId","")
                           val url = preferences.getString("imgurl","")

                           InstallerHelper.SendUrlToServer(apt_id,mtrpt_id,mail_id,state,url,this@MainActivity)
//                                     preferences.getString()
                           preferences.edit().putString("SocfolderDrive",preferences.getString("societySelected","")).apply()

                       }*/

                       if (SelectSocietyActivity.mDriveServiceHelper!=null){
                           //createFileInFolder()
                           val apt_id =  preferences.getInt("aptId",-1);
                           val mtrpt_id =  preferences.getString("mtrPoint","")
                           val mail_id = preferences.getString("mailId","")
                          // val url = preferences.getString("imgurl","")
                           val society_name = preferences.getString("societySelected","")
                           val society_id = preferences.getInt("societyIdSelected",-1)
                           val apt_name = preferences.getString("aptSelected","")
                           val meter_name = preferences.getString("mtrLocation","")


                           val uploadData = UploadData()

                           val db = Room.databaseBuilder(applicationContext,
                                   UploadDatabase::class.java,"upload_details.db").build()

                           Log.d("apt_id",apt_id.toString())



                           uploadData.aptId = apt_id.toString()
                           uploadData.meterId = mtrpt_id
                           uploadData.imgUrls = f!!.path
                           uploadData.apt_name = apt_name
                           uploadData.society_name = society_name
                           uploadData.societyId = society_id.toString()
                           uploadData.state = stateImg
                           uploadData.meter_name = meter_name


                           db.uploadDao().SaveUploadedData(uploadData)
                           Log.d("UploadDB","saved")

                           runOnUiThread { SelectSocietyActivity.loadingBar.visibility = View.GONE }
                       }

                   })
                   storeTask.start()
               }

           }
       }

    }


    /*public byte[] getBytesFromBitmap(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.JPEG, 70, stream);
    return stream.toByteArray();
}*/

    fun getBytesFromBitmap(bitmap: Bitmap?):Array<Byte>{
        val stream:ByteArrayOutputStream = ByteArrayOutputStream()

        bitmap!!.compress(Bitmap.CompressFormat.JPEG,70,stream)

        return stream.toByteArray().toTypedArray()

    }





    private fun requestSignIn() {
        Log.d(TAG, "Requesting sign-in")

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        val client = GoogleSignIn.getClient(this, signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN_NEW)
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString())
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0).show()
            return
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, a
        // authorization
        // dialog is displayed to the user.
        try {
//            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION)
//            requestSignIn()
        } catch (e: SendIntentException) {
            Log.e(TAG, "Exception while starting resolution activity", e)
        }

    }

    override fun onConnected(connectionHint: Bundle?) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFile(mimeType: String, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested MIME type.

//            setDataAndType()
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI,"content://com.google.android.apps.docs.storage")
//            putExtra(Intent.)
        }

        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }

    private fun writeFileContentToUri(uri: Uri){
        try{
                     val pfd =
                            this.getContentResolver().
                              openFileDescriptor(uri, "w");

                     val fileOutputStream =
                         FileOutputStream(
                            pfd.getFileDescriptor());


                    val stream = ByteArrayOutputStream()
                    mBitmapToSave!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
//                    mBitmapToSave!!.recycle()

                     //fileOutputStream.write(stream);
                    val bytes = ByteArrayOutputStream()
                    mBitmapToSave!!.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                    fileOutputStream.write(bytes.toByteArray())
                    fileOutputStream.close();
                    pfd.close();

                    } catch ( e:FileNotFoundException) {
                     e.printStackTrace();
              } catch ( e:IOException) {
                     e.printStackTrace();
              }
    }


    private fun createFileInFolder() {
        Log.d(TAG,"Created image file successfully")
        val folder_id = getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).getString("folderId","")
        val fileMetadata = com.google.api.services.drive.model.File();
        fileMetadata.setName(textDeteted);
        fileMetadata.setParents(Collections.singletonList(folder_id));
        val mediaContent = FileContent("image/jpeg", f);
        val file = SelectSocietyActivity.googleDriveService!!.files().create(fileMetadata, mediaContent)
        .setFields("id, parents")
        .execute();
        getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit().putString("imgurl","https://drive.google.com/open?id="+file.id).putString("mtrfileName",file.name).apply()
        System.out.println("File ID: " + file.id);

    }


    override fun onConnectionSuspended(cause: Int) {
        Log.i(TAG, "GoogleApiClient connection suspended")
    }

    companion object {

        private val TAG = "drive-WaterOn"
        private val REQUEST_CODE_CAPTURE_IMAGE = 1
        private val REQUEST_CODE_SIGN_IN_NEW: Int = 8
        private val WRITE_REQUEST_CODE:Int = 43
    }

}
