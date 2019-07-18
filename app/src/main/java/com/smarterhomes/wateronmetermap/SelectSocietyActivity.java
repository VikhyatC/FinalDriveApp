package com.smarterhomes.wateronmetermap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.smarterhomes.wateronmetermap.Helpers.DriveServiceHelper;
import com.smarterhomes.wateronmetermap.Helpers.InstallerHelper;
import com.smarterhomes.wateronmetermap.Interfaces.InstallerInterface;
import com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase.AppDataBase;
import com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase.UploadDatabase;
import com.smarterhomes.wateronmetermap.RoomArch.model.ApartmentData;
import com.smarterhomes.wateronmetermap.RoomArch.model.MeterPointData;
import com.smarterhomes.wateronmetermap.RoomArch.model.SocietyData;
import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.view.View.GONE;
import static com.google.android.gms.drive.Drive.getDriveResourceClient;

/**
 * Created by vikhyat on 15/4/19.
 */

public class SelectSocietyActivity extends AppCompatActivity implements InstallerInterface{
    private static final String TAG = "SelectActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final String DOMAIN_NAME = "smarterhomes.com";
    private Spinner societyList,aptList,mtrList;
    private List<String> societies,apartments,metering_names ;
    private List<Integer> apt_ids,mtrPoints,socIds;
    private Button select_btn,scan_switch,manual_switch,scan_btn;
    private LinearLayout manual_view,scan_view;
    public static boolean FIRSTRUN = true;
    private RadioButton radioBefore,radioAfter;
    private RadioGroup radioGroupUrl;
    public static ProgressBar loadingBar;
    protected static Drive googleDriveService = null;
    protected static DriveServiceHelper mDriveServiceHelper = null;
    private ArrayAdapter<String> adapter,adapter_apts,adapter_meterpts;
    private ImageView company_logo,logOuticon;
    private int note;
    public static TextView info_txt;
    private boolean isFound;
    private ImageButton upload_btn,resync_btn;
    private AppDataBase db ;
    private GoogleSignInClient mGoogleclient;
    private boolean isAptFound;
    private boolean isLocFound;
    private int no_of_records;
    private UploadDatabase uploadDatabase;
    private int flag = 0;
    private ProgressDialog progressDialog;
    Snackbar snackbar;
    public static RadioButton scan_rd_btn;
    public static RadioGroup scan_radio_grp;
    public static Button camera_btn;
    private int count = 0;
    public static  int scan_selected_btn = 0;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("Back pressed","Now");
        //FIRSTRUN = true;
        this.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        setContentView(R.layout.activity_select_society);
        societyList=(Spinner)findViewById(R.id.societyList);
        company_logo=(ImageView)findViewById(R.id.company_logo);
        logOuticon=(ImageView)findViewById(R.id.logOuticon);
        aptList=(Spinner)findViewById(R.id.aptList);
        radioGroupUrl=(RadioGroup)findViewById(R.id.radioUrl);
        radioBefore=(RadioButton)findViewById(R.id.radioBefore);
        radioAfter=(RadioButton)findViewById(R.id.radioAfter);
        loadingBar=(ProgressBar)findViewById(R.id.loadingBar);
        mtrList=(Spinner)findViewById(R.id.mtrList);
        loadingBar.setVisibility(View.VISIBLE);
        select_btn=(Button)findViewById(R.id.select_btn);
        resync_btn = findViewById(R.id.resync_button);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading data please wait...");
        scan_switch = findViewById(R.id.scan_switch);
        manual_switch = findViewById(R.id.manual_switch);
        manual_view = findViewById(R.id.manual_view);
        scan_view = findViewById(R.id.scaner_view);
        info_txt = findViewById(R.id.info_txt);
        scan_btn = findViewById(R.id.scan_btn);
        camera_btn = findViewById(R.id.camera_btn);
        scan_radio_grp = findViewById(R.id.scan_radioUrl);


        scan_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manual_switch.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                manual_switch.setTextColor(getResources().getColor(android.R.color.black));

                scan_switch.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                scan_switch.setTextColor(getResources().getColor(android.R.color.white));

                manual_view.setVisibility(GONE);
                scan_view.setVisibility(View.VISIBLE);
            }
        });



        manual_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manual_switch.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                manual_switch.setTextColor(getResources().getColor(android.R.color.white));

                scan_switch.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                scan_switch.setTextColor(getResources().getColor(android.R.color.black));

                manual_view.setVisibility(View.VISIBLE);
                scan_view.setVisibility(GONE);
            }
        });


        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),BarcodeScannerActivity.class));
            }
        });


        snackbar = Snackbar
                .make(findViewById(R.id.main_layout), "No internet connection !", Snackbar.LENGTH_LONG);

        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        upload_btn = findViewById(R.id.upload_button);

        uploadDatabase = Room.databaseBuilder(getApplicationContext(),UploadDatabase.class,"upload_details.db").build();

        Log.d("AlreadyLoggedin", String.valueOf(isSignedIn()));
        if (!isSignedIn()){
            requestSignIn();
        }else{
            GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);

            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(googleAccount != null ? googleAccount.getAccount() : null);
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                            .build();
            mGoogleclient = GoogleSignIn.getClient(this, signInOptions);
            googleDriveService =
                    new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("InstallerDriveApp")
                            .build();

            // The DriveServiceHelper encapsulates all REST API and SAF functionality.
            // Its instantiation is required before handling any onClick actions.
            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
            InitializeApp();
//            loadValuesFromDB();
        }
        setPageListeners();


        resync_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isInternetAvailable()){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            db.apartmentDao().deleteAll();
                            db.meterPointDao().deleteAll();
                            db.societyDao().deleteAll();

                            Log.e("Database","cleared");

                            InitializeApp();

                            Log.e("App init","completed");


                        }
                    }).start();
                }else{
                    snackbar.show();
                }



            }
        });

        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                System.err.println(e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                System.out.println("Permission ID: " + permission.getId());
            }
        };



        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("Upload btn","clicked");

                if(isInternetAvailable()){
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            List<UploadData> uploadDataList = new ArrayList<>();

                            int size = uploadDatabase.uploadDao().getAll().size();
                            Log.d("Size list",size+"");
                            count = 0;
                            String mailId = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).getString("mailId","");
                            if(size != 0){
                                for(int i =0;i<size;i++){
                                    uploadDataList.add(uploadDatabase.uploadDao().getAll().get(i));
                         /* Log.d("Saved apt_num upload db",uploadDataList.get(i).getApt_name());
                          Log.d("Saved byte img",uploadDataList.get(i).getImgBytes()+"");*/



                                    try {
                                        PerformSocietyDriveOperation(uploadDataList.get(i).getSociety_name());
                                        PerformAptDriveOperation(uploadDataList.get(i).getApt_name());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String filename = uploadDataList.get(i).getImgUrls();

                                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                                    File file = new File();
                                    file.setName(uploadDataList.get(i).getMeter_name()+"_"+uploadDataList.get(i).getMeterId()+"_"
                                            +uploadDataList.get(i).getState()+"_"+timestamp);
                                    file.setMimeType("image/jpeg");

                                    Log.d("aptName",uploadDataList.get(i).getApt_name());

                                    file.setParents( Collections.singletonList(getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).getString("folderId","")));



                                    java.io.File f = new java.io.File(filename);
                                    FileContent fc = new FileContent("image/jpeg",f);

                                    try {

                                        File upld_file = googleDriveService.files().create(file,fc).setFields("id,parents").execute();
                                        Log.d("File Id :",upld_file.getId());
                                        String imgUrl = "https://drive.google.com/open?id="+upld_file.getId();
                                        BatchRequest batch = googleDriveService.batch();


                                        Permission domainPermission = new Permission()
                                                .setType("domain")
                                                .setRole("reader")
                                                .setDomain(DOMAIN_NAME);
                                        googleDriveService.permissions().create(upld_file.getId(), domainPermission)
                                                .setFields("id")
                                                .queue(batch, callback);

                                        batch.execute();

                                        InstallerHelper.SendUrlToServer(Integer.parseInt(uploadDataList.get(i).getAptId()),uploadDataList.get(i).getMeterId(),mailId,uploadDataList.get(i).getState(),imgUrl,SelectSocietyActivity.this);
                                        count++;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                }


                                if(count == uploadDataList.size()){
                                    uploadDatabase.uploadDao().deleteAll();
                                    uploadDataList.clear();
                                    Log.d("upldDB del","yes");
                                    Log.d("list size",uploadDataList.size()+"");
                                    Log.d("upld db size",uploadDatabase.uploadDao().getAll().size()+"");

                                }


                                runOnUiThread(() -> progressDialog.dismiss());
                            }else{
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(SelectSocietyActivity.this, "No data to sync !", Toast.LENGTH_SHORT).show();
                                });
                            }


                        }
                    }).start();


                }else{
                    snackbar.show();
                }




            }

        });
    }



    public boolean isInternetAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void loadAfterDefaults() {
        SharedPreferences preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);

        String before_Society = preferences.getString("SocfolderName","");
        Log.d("PrevFolderName",before_Society);
        societyList.setSelection(adapter.getPosition(before_Society));

        String before_Apt = preferences.getString("folderName","");
        aptList.setSelection(adapter.getPosition(before_Apt));

        String positionMtrloc = preferences.getString("mtrfileName","");
        mtrList.setSelection(adapter.getPosition(positionMtrloc));

//        Log.d("Positions",String.valueOf(positionSoc)+":"+String.valueOf(positionApt)+":"+String.valueOf(positionMtrloc));


    }

    private void setPageListeners() {
        societyList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("SocietySelected at ", String.valueOf(position));
                    getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit()
                                .putString("societySelected",societies.get(position))
                                .putInt("societyIdSelected",socIds.get(position))
                                .putInt("societyPosition",position)
                                .apply();
                    loadingBar.setVisibility(View.VISIBLE);

//                    SocietyDriveTask driveTask = new SocietyDriveTask();
//                    driveTask.execute();
                    Log.d("NoOfRecords", String.valueOf(no_of_records));
                Executor dataChecker = Executors.newSingleThreadExecutor();
                dataChecker.execute(() ->{


                });

                Runnable r1 = () -> {
                    no_of_records = db.apartmentDao().hasData();
                    Log.e("no_of_rcrds ",no_of_records+"");
                };

                Runnable r2 = () -> {
                    Log.d("NoOfRecords", String.valueOf(no_of_records));
                    if (no_of_records>0){
                        apartments = new ArrayList<>();
                        apt_ids = new ArrayList<>();
                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> {
                            Log.e("Second exec","running "+socIds.get(position));


                            ArrayList<ApartmentData> apartmentsInSoc = (ArrayList<ApartmentData>) db.apartmentDao().getApartmentsOfSociety(socIds.get(position));

                            Log.e("apt_db_size",apartmentsInSoc.size()+"");

                            for (ApartmentData apartmentData:apartmentsInSoc){
                                Log.d("ApartmentsInSoc",apartmentData.getAptName()+":"+apartmentData.getAptId()+":"+apartmentData.getSocId());
                                apartments.add(apartmentData.getAptName());
                                apt_ids.add(apartmentData.getAptId());
                                flag = 1;
                            }




                            Log.d("ApartmentsSize", String.valueOf(apartmentsInSoc.size()));

                            runOnUiThread(() -> {
                                if (apartmentsInSoc.size()>0){
                                    int before_index = ShowLastSelectedApartment();
                                    if (isAptFound){

                                        Collections.swap(apartments,before_index,0);
                                        Collections.swap(apt_ids,before_index,0);
                                    }
                                    select_btn.setEnabled(true);
                                    aptList.setEnabled(true);
                                    aptList.setSelected(true);
                                    mtrList.setEnabled(true);
                                    mtrList.setSelected(true);
                                    adapter_apts = new ArrayAdapter<String>(SelectSocietyActivity.this,
                                            android.R.layout.simple_spinner_item, apartments);
                                    adapter_apts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    aptList.setAdapter(adapter_apts);
                                }else{
                                    select_btn.setEnabled(false);
                                    aptList.setEnabled(false);
                                    aptList.setSelected(false);
                                    mtrList.setEnabled(false);
                                    mtrList.setSelected(false);
                                    Toast.makeText(SelectSocietyActivity.this,"No Data Mapped",Toast.LENGTH_LONG).show();
                                    loadingBar.setVisibility(GONE);
                                }
                            });
                        });

                    }else{


                        Log.e("API","called");
                        InstallerHelper.getApartmentsInSociety(SelectSocietyActivity.this,societies.get(position));


                    }
                };


                Thread t1 = new Thread(r1);
                Thread t2 = new Thread(r2);

                t1.start();
                try {
                    t1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t2.start();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        aptList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit()
                        .putString("aptSelected",apartments.get(position))
                        .putInt("aptPosition",position)
                        .putInt("aptId",apt_ids.get(position))
                        .apply();
                loadingBar.setVisibility(View.VISIBLE);
//                ApartmentDriveTask driveTask = new ApartmentDriveTask();
//                driveTask.execute();
                Executor dataChecker = Executors.newSingleThreadExecutor();
                dataChecker.execute(() ->{
                    no_of_records = db.meterPointDao().hasData();
                    Log.d("NoOfRecordsMer", String.valueOf(no_of_records));
                    if (no_of_records==0){
                        InstallerHelper.GetMeteringDetails(apt_ids.get(position),SelectSocietyActivity.this);
                    }else{
                        metering_names = new ArrayList<>();
                        mtrPoints = new ArrayList<>();
                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> {
                            ArrayList<MeterPointData> metersInApt = (ArrayList<MeterPointData>) db.meterPointDao().geMetersInApt(apt_ids.get(position));

                            for (MeterPointData meterPointData:metersInApt){
                                Log.d("MetersInApt",meterPointData.getMptName()+":"+meterPointData.getAptId()+":"+meterPointData.getMptId());
                                metering_names.add(meterPointData.getMptName());
                                mtrPoints.add(meterPointData.getMptId());
                            }


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (metersInApt.size()>0){



                                        int mLoc_before_index = ShowLastSelectedMeterLocation();
                                        if (isLocFound){

                                            Collections.swap(metering_names,mLoc_before_index,0);
                                            Collections.swap(mtrPoints,mLoc_before_index,0);
                                        }
                                        adapter_meterpts = new ArrayAdapter<String>(SelectSocietyActivity.this,
                                                android.R.layout.simple_spinner_item, metering_names);
                                        adapter_meterpts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                        mtrList.setAdapter(adapter_meterpts);
                                        FIRSTRUN = true;

                                    }else {
                                        select_btn.setEnabled(false);
                                        mtrList.setEnabled(false);
                                        mtrList.setSelected(false);
                                        Toast.makeText(SelectSocietyActivity.this,"No Meters Mapped",Toast.LENGTH_LONG).show();
                                        loadingBar.setVisibility(GONE);
                                    }


                                    if(FIRSTRUN){
                                        loadingBar.setVisibility(GONE);
                                    }
                                }
                            });

                        });

                    }

                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mtrList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit()
                        .putString("mtrLocation",metering_names.get(position))
                        .putInt("mtrPosition",position)
                        .putString("mtrPoint", String.valueOf(mtrPoints.get(position)))
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = radioGroupUrl.getCheckedRadioButtonId();
                RadioButton btn = (RadioButton)findViewById(id);
                FIRSTRUN = false;
                Log.e("radio id",id+"");
                Log.e("radio btn",btn.getText().toString());
                getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).edit().putBoolean("app_reset",false).putString("stateCheck",btn.getText().toString()).apply();
                Intent intent = new Intent(SelectSocietyActivity.this,MainActivity.class);
                startActivity(intent);
                SelectSocietyActivity.this.finish();
            }
        });
        logOuticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleclient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        requestSignIn();
                    }
                });
            }
        });

//        company_logo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .build();
        mGoogleclient = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(mGoogleclient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.valueOf(resultCode));
        switch (requestCode){
            case REQUEST_CODE_SIGN_IN: if (resultCode == Activity.RESULT_OK && data != null){
                handleSignInResult(data);
                InitializeApp();
                break;
            }else{
                requestSignIn();
            }
        }

    }
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());
                    getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).edit().putString("mailId",googleAccount.getEmail()).apply();
                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("InstallerDriveApp")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    private void InitializeApp() {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDataBase.class, "init_details").build();
        Executor myExecutor = Executors.newSingleThreadExecutor();
        myExecutor.execute(() -> {
            no_of_records = db.societyDao().hasData();
//            Log.d("NoOfRecords", String.valueOf(no_of_records));
            if (no_of_records==0){
                InstallerHelper.getSocietiesList(this);


            }else{
                societies = new ArrayList<>();
                socIds = new ArrayList<>();
                ArrayList<SocietyData> allSocieties = (ArrayList<SocietyData>) db.societyDao().getAll();

                for (SocietyData data : allSocieties){
                    societies.add(data.getSocietyName());
                    socIds.add(data.getSocietyId());
                }




                int before_index = ShowLastSelectedSociety();
                if (isFound){

                    Collections.swap(societies,before_index,0);
                    Collections.swap(socIds,before_index,0);
                }
                adapter = new ArrayAdapter<String>(SelectSocietyActivity.this,
                        android.R.layout.simple_spinner_item, societies);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        societyList.setAdapter(adapter);
                    }
                });

            }
        });


}

    private int ShowLastSelectedMeterLocation() {
        String strName = "";
        int swap_index = 0;
        for (int i=0;i<metering_names.size();i++){
            strName = metering_names.get(i);
            if (strName.trim().toLowerCase().equals(getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).getString("mtrLocation","").trim().toLowerCase())){
                swap_index = i;
                isLocFound = true;
            }
        }
        return swap_index;
    }

    private int ShowLastSelectedApartment() {
        String strName = "";
        int swap_index = 0;
        for (int i=0;i<apartments.size();i++){
            strName = apartments.get(i);
            if (strName.trim().toLowerCase().equals(getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).getString("aptSelected","").trim().toLowerCase())){
                swap_index = i;
                isAptFound = true;
            }
        }
        return swap_index;
    }


    @Override
    public void ShowSocieties(final String response) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int swap_index = 0;
                    societies = new ArrayList<>();
                    socIds = new ArrayList<>();
                    final JSONArray jsSociety = new JSONArray(response);

                    Runnable r1 = () -> {
                        int soc_id = 0;
                        String soc_name = "";
                        for(int i=0; i <jsSociety.length(); i++){
                            JSONObject object = null;
                            try {
                                object = jsSociety.getJSONObject(i);
                                soc_id = object.getInt("societyId");
                                soc_name = object.getString("societyName");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            SocietyData societyData = new SocietyData();
                    /*societies.add(soc_name);
                    socIds.add(soc_id);*/
                            societyData.setSocietyId(soc_id);
                            societyData.setSocietyName(soc_name);

                            Log.d("Saving soc data", String.valueOf(societyData.getSocietyName()+":"+societyData.getSocietyId()));

                            db.societyDao().SaveSocietyData(societyData);
                        }
                    };

                    Runnable r2 = () -> {
                        Log.e("rtrv","running");
                        List<SocietyData> getSoc = db.societyDao().getAll();
                        Log.d("getSoc size",getSoc.size()+"");
                        for(int i=0;i<getSoc.size();i++){

                            Log.d("Rtrv soc name ",getSoc.get(i).getSocietyName()+" id - "+getSoc.get(i).getSocietyId());
                            societies.add(getSoc.get(i).getSocietyName());
                            socIds.add(getSoc.get(i).getSocietyId());
                        }

                        int before_index = ShowLastSelectedSociety();
                        if (isFound){
                            Collections.swap(societies,before_index,0);
                            Collections.swap(socIds,before_index,0);
                        }
                        Log.d("adapter","running");
                        adapter = new ArrayAdapter<String>(SelectSocietyActivity.this,
                                android.R.layout.simple_spinner_item, societies);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                societyList.setAdapter(adapter);
                            }
                        });

                    };

                    Thread t1 = new Thread(r1);
                    Thread t2 = new Thread(r2);

                    t1.start();
                    try {
                        t1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    t2.start();


                    /*int before_index = ShowLastSelectedSociety();
                    if (isFound){
                        Collections.swap(societies,before_index,0);
                        Collections.swap(socIds,before_index,0);
                    }
                    Log.d("adapter","running");
                    adapter = new ArrayAdapter<String>(SelectSocietyActivity.this,
                            android.R.layout.simple_spinner_item, societies);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    societyList.setAdapter(adapter);*/




                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private int ShowLastSelectedSociety() {
        String strName = "";
        int swap_index = 0;
        for (int i=0;i<societies.size();i++){
            strName = societies.get(i);
            if (strName.trim().toLowerCase().equals(getSharedPreferences("defaults_pref",Context.MODE_PRIVATE).getString("societySelected","").trim().toLowerCase())){
                swap_index = i;
                isFound = true;
            }
        }
        return swap_index;
    }

    private void PerformSocietyDriveOperation(String soc) throws IOException {
        String pageToken = null;
        boolean isFound = false;
        SharedPreferences preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);
        String society_name = soc;

        Log.d("society_name",society_name);

        do {
            FileList result = googleDriveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            Log.d("result size",result.getFiles().size()+"");
            for (File file : result.getFiles()) {

                if (society_name.equals(file.getName())){

                    System.out.printf("Found parent folder: %s (%s)\n",
                            file.getName(), file.getId());

                    getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit().putString("SocfolderId",file.getId()).putString("SocfolderName",file.getName()).apply();
                    isFound = true;
                }
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        if (!isFound){
            createFolder(society_name);
        }
    }

    private void createFolder(String society_folder_name) throws IOException {
        File fileMetadata = new com.google.api.services.drive.model.File();
        Log.d(TAG,society_folder_name);
        fileMetadata.setName(society_folder_name);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = googleDriveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit().putString("SocfolderId",file.getId()).putString("SocfolderName",file.getName()).apply();

        System.out.println("Folder ID & Folder name: " + file.getId()+"&"+file.getName());

    }

    @Override
    public void ShowApartments(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                apartments = new ArrayList<>();
                apt_ids = new ArrayList<>();

                try {
//                    JSONObject object = new JSONObject(response);
                    final JSONArray jsApts = new JSONArray(response);
                    SharedPreferences preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);
                    int selected_soc_id = preferences.getInt("societyIdSelected",0);
                    for(int i=0; i <jsApts.length(); i++){
                        ApartmentData data = new ApartmentData();
                        JSONObject object = jsApts.getJSONObject(i);
                        int aptId = object.getInt("aptId");
                        String aptName = object.getString("aptNumber");
//                        apartments.add(aptName);
//                        apt_ids.add(aptId);
                        int socId = object.getInt("societyId");
                        data.setAptId(aptId);
                        data.setAptName(aptName);
                        data.setSocId(socId);

                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> {
                           ArrayList<ApartmentData> apartmentData = new ArrayList<>();
                            Log.d("Saving apt data", String.valueOf(data.getSocId()+":"+data.getAptId()+":"+data.getAptName()));
                            db.apartmentDao().SaveApartmentData(data);
                        });



                    }
                    Executor myExecutor = Executors.newSingleThreadExecutor();
                    myExecutor.execute(() -> {
                        ArrayList<ApartmentData> apartmentData = new ArrayList<>();
                        Log.e("selected soc id",selected_soc_id+"");
                        apartmentData = (ArrayList<ApartmentData>) db.apartmentDao().getApartmentsOfSociety(selected_soc_id);
                        for (ApartmentData data1:apartmentData){
                            Log.d("ApartmnentExec",data1.getAptName());
                            apartments.add(data1.getAptName());
                            apt_ids.add(data1.getAptId());
                        }




                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {



                                int before_index = ShowLastSelectedApartment();
                                if (isAptFound){

                                    Collections.swap(apartments,before_index,0);
                                    Collections.swap(apt_ids,before_index,0);
                                }
                                select_btn.setEnabled(true);
                                aptList.setEnabled(true);
                                aptList.setSelected(true);
                                mtrList.setEnabled(true);
                                mtrList.setSelected(true);
                                adapter_apts = new ArrayAdapter<String>(SelectSocietyActivity.this,
                                        android.R.layout.simple_spinner_item, apartments);
                                adapter_apts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                aptList.setAdapter(adapter_apts);


                                /*adapter_apts = new ArrayAdapter<String>(SelectSocietyActivity.this,
                                        android.R.layout.simple_spinner_item, apartments);
                                adapter_apts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                aptList.setAdapter(adapter_apts);*/
                            }
                        });

                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void ErrorfetchingData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SelectSocietyActivity.this,"Unable to Fetch data please try again",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void fetchMeteringPoints(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtrPoints = new ArrayList<>();
                metering_names = new ArrayList<>();
                SharedPreferences preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);
                int selected_apt_id = preferences.getInt("aptId",0);
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i=0;i<array.length();i++){
                        MeterPointData pointData = new MeterPointData();
                        JSONObject object = array.getJSONObject(i);
                        int mp_id = object.getInt("meteringPointId");
                        String location = object.getString("location");
                        int meter_id = object.getInt("meterId");
                        int aptId = object.getInt("aptId");
                        pointData.setAptId(aptId);
                        pointData.setMptId(mp_id);
                        pointData.setMptName(location);
                        pointData.setMeter_id(meter_id);
                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> {
                            ArrayList<MeterPointData> meterData = new ArrayList<>();
                            Log.d("Saving Mtr data", String.valueOf(pointData.getAptId()+":"+pointData.getMptId()+":"+pointData.getMptName()));
                            db.meterPointDao().SaveMeterPointData(pointData);
                        });

                    }
                    Executor myExecutor = Executors.newSingleThreadExecutor();
                    myExecutor.execute(() -> {
                        ArrayList<MeterPointData> meterData = new ArrayList<>();
                        meterData = (ArrayList<MeterPointData>) db.meterPointDao().geMetersInApt(selected_apt_id);
                        for (MeterPointData data1:meterData){
                            metering_names.add(data1.getMptName());
                            mtrPoints.add(data1.getMptId());
                        }




                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter_meterpts = new ArrayAdapter<String>(SelectSocietyActivity.this,
                                        android.R.layout.simple_spinner_item, metering_names);
                                adapter_meterpts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mtrList.setAdapter(adapter_meterpts);
                                Log.d("FIRSTRUN",FIRSTRUN+"");
                                if (FIRSTRUN){
                                    loadingBar.setVisibility(GONE);
                                }
                            }
                        });
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                populateMeterSpinner();

            }
        });
    }

    private void populateMeterSpinner() {

            int mLoc_before_index = ShowLastSelectedMeterLocation();
            if (isAptFound){

                Collections.swap(metering_names,mLoc_before_index,0);
                Collections.swap(mtrPoints,mLoc_before_index,0);
            }
            adapter_meterpts = new ArrayAdapter<String>(SelectSocietyActivity.this,
                    android.R.layout.simple_spinner_item, metering_names);
            adapter_meterpts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mtrList.setAdapter(adapter_meterpts);

    }

    @Override
    public void UrlPassedToserver(String strResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

   /* private class SocietyDriveTask extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                PerformSocietyDriveOperation(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new ApartmentDriveTask().execute();
        }
    }*/

   /* private class ApartmentDriveTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                PerformAptDriveOperation("");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);



        }
    }*/

    private void PerformAptDriveOperation(String aptname) throws IOException {
        String pageToken = null;
        boolean isFound = false;
        SharedPreferences preferences = getSharedPreferences("defaults_pref", Context.MODE_PRIVATE);
        String apt_name = aptname;

        do {
            FileList result = googleDriveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            Log.d("result apt size",result.getFiles().size()+"");

            for (File file : result.getFiles()) {

                if (apt_name.equals(file.getName())){
                    System.out.printf("Found sub folder: %s (%s)\n",
                            file.getName(), file.getId());

                    getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit().putString("folderId",file.getId()).putString("folderName",file.getName()).apply();
                    isFound = true;
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        if (!isFound){
            createSubFolder(apt_name);
        }

    }

    private void createSubFolder(String apt_name) throws IOException {
        SharedPreferences preferences = getSharedPreferences("defaults_pref",Context.MODE_PRIVATE);
        String folderId = preferences.getString("SocfolderId","");
        File fileMetadata = new File();
        Log.d(TAG,apt_name);
        fileMetadata.setName(apt_name);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(folderId));

        File file = googleDriveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit().putString("folderId",file.getId()).putString("folderName",file.getName()).apply();

        System.out.println("Folder ID & Folder name: " + file.getId()+"&"+file.getName());
    }



}






