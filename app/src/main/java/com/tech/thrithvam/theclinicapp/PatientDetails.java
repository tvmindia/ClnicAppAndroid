package com.tech.thrithvam.theclinicapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDetails extends AppCompatActivity {

    Cryptography cryptography=new Cryptography();
    String AppointmentDate;
    Bundle extras;
    SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
    Calendar cal= Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);
        extras = getIntent().getExtras();
        AppointmentDate = extras.getString("AppointmentDate");

        if(!AppointmentDate.equals("null")){                                    //updated
            cal.setTimeInMillis(Long.parseLong(AppointmentDate));
            AppointmentDate=(formatted.format(cal.getTime()));
        }
        new  PatientDetail().execute();
    }


    /*----------------------Thread to load Appointment list  dates --------------------------*/
    public class PatientDetail extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData,clinicid,doctorid;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> visitListData =new ArrayList<>();
        ProgressDialog pDialog=new ProgressDialog(PatientDetails.this);
        FileInputStream fStream=null;
        String fileNameString = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            DatabaseHandler db= new DatabaseHandler(PatientDetails.this);
            clinicid=db.GetUserDetail("ClinicID");
            doctorid=db.GetUserDetail("DoctorID");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/GetAppointmentPatientDetails";
            HttpURLConnection c = null;
            try {
                postData = "{\"doctorid\":\"" +doctorid + "\",\"clinicid\":\"" +clinicid + "\",\"appointmentdate\":\"" +AppointmentDate + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);
                        strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"", "\"")+ "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message","");
                    pass = jsonObject.optBoolean("Flag", true);
                    String[] data=new String[5];
                    data[0] = jsonObject.optString("Name");
                    data[1] = jsonObject.optString("appointmentno");
                    data[2] = jsonObject.optString("AllottingTime");
                    data[3] = jsonObject.optString("Mobile");
                    data[4] = jsonObject.optString("Location");
                    /*data[5]=jsonObject.optString("AppointmentDate").replace("/Date(", "").replace(")/", "");
                    data[6] = jsonObject.optString("DoctorID");*/

                    visitListData.add(data);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(!pass) {
                new AlertDialog.Builder(PatientDetails.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).show();
            }
            else {


                //Refering the List view in the Patient Details Tile
                ListView visitList= (ListView) findViewById(R.id.listpatientdetails);
                CustomAdapter adapter=new CustomAdapter(PatientDetails.this, R.layout.patientdetails_listview, visitListData,"PatientDetails");
                visitList.setAdapter(adapter);

            }
        }
    }
}
