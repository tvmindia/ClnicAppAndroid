package com.tech.thrithvam.theclinicapp;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUpload implements Runnable{
    URL connectURL;
    Context context;
    public int responseString;
    String visitID,userName, clinicID, description;
    String fileName="";
    FileInputStream fileInputStream = null;

    FileUpload(Context context, String urlString, FileInputStream fStream, String fileName, String visitID, String userName, String clinicID, String description){
        try{
            this.context=context;
            connectURL = new URL(urlString);
            fileInputStream = fStream;
            this.fileName=fileName;
            this.visitID = visitID;
            this.userName=userName;
            this.clinicID = clinicID;
            this.description = description;
        }catch(Exception ex){
            //  Log.i("FileUpload", "URL Malformatted");
        }
    }

    void Sending(){
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String Tag="fSnd";
        try
        {
            //         Log.e(Tag,"Starting Http File Sending to URL");

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();

            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"userName\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(userName);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"ClinicID\""+ lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(clinicID);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"VisitID\""+ lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(visitID);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"Description\""+ lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(description);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
            dos.writeBytes(lineEnd);

            //          Log.e(Tag,"Headers are written");

            // create a buffer of maximum size
            int bytesAvailable = fileInputStream.available();

            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[ ] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0,bufferSize);
            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            fileInputStream.close();

            dos.flush();

            Log.e(Tag, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

//            InputStream is = conn.getInputStream();
//
//            // retrieve the response from server
//            int ch;
//
//            StringBuffer b =new StringBuffer();
//            while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
//            String s=b.toString();
//            Log.i("Response",s);
            responseString=conn.getResponseCode();
            dos.close();
        } catch (IOException ioe)
        {
            // Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }


    void UploadFileFn(){
        new UploadFile().execute();
    }

    public class UploadFile extends AsyncTask<Void , Void, Void> {
        ProgressDialog pDialog=new ProgressDialog(context);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(context.getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Sending();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(responseString==200){
                Toast.makeText(context, R.string.success, Toast.LENGTH_LONG).show();
                /*Intent punchItem = new Intent(context, PunchItemDetails.class);
                punchItem.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                punchItem.putExtra("ID", visitID);
                punchItem.putExtra("clinicID", clinicID);
                punchItem.putExtra("type", description);
                context.startActivity(punchItem);*/
                ((Activity)context).finish();
            }
            else {
                Toast.makeText(context, R.string.unsuccessful, Toast.LENGTH_LONG).show();
            }
        }
    }
}