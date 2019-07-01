package com.smarterhomes.wateronmetermap;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.zxing.Result;
import com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase.AppDataBase;
import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.ApartmentDao;
import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.MeterPointDao;
import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.SocietyDao;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static java.security.AccessController.getContext;

public class BarcodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    ZXingScannerView zXingScannerView;
    FocusHandler focusHandler;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    AppDataBase dataBase;
    String socName="",aptName="",mtrName = "";
    int aptId,socId,mtrPointId;
    Activity activity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zXingScannerView = new ZXingScannerView(this);
        focusHandler = new FocusHandler(new Handler(), zXingScannerView);
        setContentView(zXingScannerView);

        dataBase = Room.databaseBuilder(getApplicationContext(),
                AppDataBase.class, "init_details").build();



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        SelectSocietyActivity.camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = SelectSocietyActivity.scan_radio_grp.getCheckedRadioButtonId();
                Log.e("selected id",id+"");
                RadioButton btn = getLayoutByRes(R.layout.activity_select_society,null).findViewById(id);
                Log.e("selected btn",btn.getText()+"");
                SelectSocietyActivity.FIRSTRUN = false;
                getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit().putBoolean("app_reset",false).putString("stateCheck",btn.getText().toString()).apply();
                Intent intent = new Intent(BarcodeScannerActivity.this,MainActivity.class);
                startActivity(intent);
                BarcodeScannerActivity.this.finish();
            }
        });

    }

    public  View getLayoutByRes(@LayoutRes int layoutRes, @Nullable ViewGroup viewGroup)
    {
        final LayoutInflater factory = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return factory.inflate(layoutRes, viewGroup);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();


            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void handleResult(Result result) {

        Runnable r1 = () -> {
            String rslt = result.getText().substring(0,result.getText().length()-1);

            int i=0;
            while(i<rslt.length() && rslt.charAt(i) == '0'){
                i++;
            }

            StringBuffer sb = new StringBuffer(rslt);
            String meterid = String.valueOf(sb.replace(0,i,""));

            Log.e("mterid scanned",meterid);

            mtrName = dataBase.meterPointDao().getMeterName(Integer.parseInt(meterid));
            aptId = dataBase.meterPointDao().getAptId(Integer.parseInt(meterid));
            aptName = dataBase.apartmentDao().getAptName(aptId);
            socId = dataBase.apartmentDao().getSocId(aptId);
            socName = dataBase.societyDao().getSocName(socId);
            mtrPointId = dataBase.meterPointDao().getMtrPointId(Integer.parseInt(meterid));


        };

        Runnable r2 = () -> runOnUiThread(() -> {
            SelectSocietyActivity.info_txt.setText("Society - "+socName+"\nApartment - "+aptName+"\nMeter - "+mtrName);
            Log.d("RESULT INFO",socName+" "+aptName+" "+mtrName);

            getSharedPreferences("defaults_pref", Context.MODE_PRIVATE).edit()
                    .putString("aptId",String.valueOf(aptId))
                    .putString("mtrPoint",String.valueOf(mtrPointId))
                    .putString("societySelected",socName)
                    .putString("societyIdSelected",String.valueOf(socId))
                    .putString("aptSelected",aptName)
                    .putString("mtrLocation",mtrName)
                    .apply();

        });

        //SelectSocietyActivity.info_txt.setText(result.getText());
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);

        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t2.start();



        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
        focusHandler.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        zXingScannerView.setResultHandler(this);
        zXingScannerView.setAutoFocus(false);
        zXingScannerView.startCamera();
        focusHandler.start();
    }
}
