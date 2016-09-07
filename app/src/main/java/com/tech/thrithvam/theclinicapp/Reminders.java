package com.tech.thrithvam.theclinicapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reminders extends AppCompatActivity {

    ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        listview=(ListView)findViewById(R.id.listschedulereminders);
        new Scheduledremainders().execute();

    }

    /*----------------------Thread to load Scheduled  dates --------------------------*/
    public class Scheduledremainders extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData,clinicid,doctorid;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        Cryptography cryptography=new Cryptography();
        ArrayList<String[]> ScheduleData =new ArrayList<>();
        ProgressDialog pDialog=new ProgressDialog(Reminders.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            DatabaseHandler db= new DatabaseHandler(Reminders.this);
            clinicid=db.GetUserDetail("ClinicID");
            doctorid=db.GetUserDetail("DoctorID");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/GetReminderScheduleDetails";
            HttpURLConnection c = null;
            try {
                postData = "{\"doctorid\":\"" +doctorid + "\",\"clinicid\":\"" +clinicid + "\"}";
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
                        strJson="{\"JSON\":" + strJson.replace("\\\"", "\"").replace("\\\\", "\\")+ "}";
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
                    msg=jsonObject.optString("Message", "");
                    pass = jsonObject.optBoolean("Flag", true);
                    String[] data=new String[4];
                    data[0]=jsonObject.optString("event_start").replace("/Date(", "").replace(")/", "");
                    data[1] = jsonObject.optString("Starttime");
                    data[2] = jsonObject.optString("Endtime");
                    data[3] = jsonObject.optString("Name");
                    ScheduleData.add(data);
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
                new AlertDialog.Builder(Reminders.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();
            }
            else {
                //Referring the List view in the Tile
                //ListView visitList= (ListView) findViewById(R.id.listschedulereminders);
                CustomAdapter adapter=new CustomAdapter(Reminders.this, R.layout.reminderschedule, ScheduleData,"ReminderSchedule");
                listview.setAdapter(adapter);




            }
        }
    }

}
