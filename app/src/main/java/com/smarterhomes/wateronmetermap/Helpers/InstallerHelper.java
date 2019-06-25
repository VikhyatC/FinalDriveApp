package com.smarterhomes.wateronmetermap.Helpers;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.smarterhomes.wateronmetermap.Interfaces.InstallerInterface;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by vikhyat on 30/4/19.
 */

public class InstallerHelper {
    private static InstallerInterface installerInterface = null;
    public static void GetMeteringDetails(int apt_id,InstallerInterface installerInterface){
        InstallerHelper.installerInterface = installerInterface;
        GetDataTask dataTask = new GetDataTask();
        dataTask.execute(String.valueOf(apt_id));
    }

    public static void getApartmentsInSociety(InstallerInterface installerInterface,String society_name){
        InstallerHelper.installerInterface = installerInterface;
        GetApartmentDataTask apartmentDataTask = new GetApartmentDataTask();
        apartmentDataTask.execute(society_name);
    }

    public static void getSocietiesList(InstallerInterface installerInterface) {
        InstallerHelper.installerInterface = installerInterface;
        GetSocietyDataTask dataTask = new GetSocietyDataTask();
        dataTask.execute();
    }

    public static void SendUrlToServer(int apt_id,String mtrPt,String email_id,String state,String url,InstallerInterface installerInterface){
        InstallerHelper.installerInterface = installerInterface;
        StoreTask task = new StoreTask();
        task.execute(String.valueOf(apt_id),mtrPt,email_id,url,state);
    }

    private static class GetDataTask extends AsyncTask<String,Void,Void> {

        private int HttpResult;
        private String response;
        private String responseError;

        @Override
        protected Void doInBackground(String... strings) {
            String url = "http://partnerapi.wateron.in/api/getBulkData/getMtrData";
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setRequestMethod("GET");
                HttpResult = con.getResponseCode();
                StringBuilder sb = new StringBuilder();
                Log.d("Refresh resultCode", String.valueOf(HttpResult));
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    response = sb.toString();
                    installerInterface.fetchMeteringPoints(response);
                    System.out.println("dataRefreshed :" + response);
                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getErrorStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    responseError = sb.toString();
                    System.out.println(con.getResponseMessage());
                    installerInterface.ErrorfetchingData();
                    Log.d("dataError" ,responseError);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class GetSocietyDataTask extends AsyncTask<String,Void,Void> {
        private InputStream in;
        private String strResult;
        private int HttpResult;
        private String strError;

        @Override
        protected Void doInBackground(String... strings) {
            String url = "http://partnerapi.wateron.in/api/getBulkData/getSocData";
            Message msg = Message.obtain();
            msg.what=1;
            try{
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setRequestMethod("GET");
                HttpResult = con.getResponseCode();
                StringBuilder sb = new StringBuilder();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    strResult = sb.toString();
                    Log.d("ResultOK",strResult);
                    installerInterface.ShowSocieties(strResult);
                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getErrorStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    strError = sb.toString();
                    System.out.println(con.getResponseMessage());
                    installerInterface.ErrorfetchingData();
                    Log.d("dataError" ,strError);
                }
            }catch (IOException e1){
                    e1.printStackTrace();
            }

            return null;
        }
    }

    private static InputStream openHTTPConnection(String strURL, String strParams){
        InputStream in = null;
        int resCode = -1;

        try{
            URL url = new URL(strURL);
            URLConnection urlConn = url.openConnection();
            Log.d("URL",strURL);
            if(!(urlConn instanceof HttpURLConnection)){
                throw new IOException("URL is not an Http URL");
            }

            HttpURLConnection httpConn = (HttpURLConnection)urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("POST");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);

            DataOutputStream os = new DataOutputStream( httpConn.getOutputStream());
            os.writeBytes(strParams);
            os.flush();
            os.close();

            httpConn.connect();
            resCode = httpConn.getResponseCode();

            Log.d("ResponseCode", String.valueOf(resCode));
            if(resCode == HttpsURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }else{
                installerInterface.ErrorfetchingData();
            }
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return in;
    }

    private static class GetApartmentDataTask extends AsyncTask<String,Void,Void> {
        private InputStream in;
        private String strResult;
        private int HttpResult;
        private String strError;

        @Override
        protected Void doInBackground(String... strings) {
            String url = "http://partnerapi.wateron.in/api/getBulkData/getAptData";
            Message msg = Message.obtain();
            msg.what=1;
            try{
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setRequestMethod("GET");
                HttpResult = con.getResponseCode();
                StringBuilder sb = new StringBuilder();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    strResult = sb.toString();
                    Log.d("ResultOK",strResult);
                    installerInterface.ShowApartments(strResult);
                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getErrorStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    strError = sb.toString();
                    System.out.println(con.getResponseMessage());
                    installerInterface.ErrorfetchingData();
                    Log.d("dataError" ,strError);
                }
            }catch (IOException e1){
                e1.printStackTrace();
            }

            return null;   
        }
    }

    private static class StoreTask extends AsyncTask<String,Void,Void> {
        private InputStream in;
        private String strResult;
        private String url;

        @Override
        protected Void doInBackground(String... strings) {

            if (strings[4].trim().toLowerCase().equals("before")){
                url = "http://installerapi.wateron.in/updatebimgdetails.php?aptId="+strings[0]+"&"+"mpId="+strings[1]+"&"+"email="+strings[2];
            }else{
                url = "http://installerapi.wateron.in/updateaimgdetails.php?aptId="+strings[0]+"&"+"mpId="+strings[1]+"&"+"email="+strings[2];
            }

            Message msg = Message.obtain();
            msg.what=1;
            try{
                if (strings[4].trim().toLowerCase().equals("before")){
                    in = openHTTPConnection(url, "bimg="+strings[3]);
                }else{
                    in = openHTTPConnection(url, "aimg="+strings[3]);
                }

                if(in != null) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder strIn = new StringBuilder();
                    String line;
                    while((line=r.readLine()) != null){
                        strIn.append(line).append('\n');
                    }
                    strResult = strIn.toString();
                    Log.d("Main", "Server sent: "+strResult);
                    installerInterface.UrlPassedToserver(strResult);
                }
                Bundle b = new Bundle();
                b.putString("Result",strResult);
                msg.setData(b);
                in.close();
            }
            catch (IOException e1){
                e1.printStackTrace();
            }
            return null;
        }
    }
}
