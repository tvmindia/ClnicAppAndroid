package com.tech.thrithvam.theclinicapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Schedules extends AppCompatActivity {
    CalendarView calendarView;
    Calendar SelectedDate=Calendar.getInstance();
    GridView gridview;
    ArrayList<String> ScheduledDates =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        gridview=(GridView)findViewById(R.id.gridschedulelist);
        new Scheduleddates().execute();
    }
    /*----------------------Thread to load Scheduled  dates --------------------------*/
    public class Scheduleddates extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData,clinicid,doctorid;
        String str;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        Cryptography cryptography=new Cryptography();
        ArrayList<String[]> ScheduleData =new ArrayList<>();
        ProgressDialog pDialog=new ProgressDialog(Schedules.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            DatabaseHandler db= new DatabaseHandler(Schedules.this);
            clinicid=db.GetUserDetail("ClinicID");
            doctorid=db.GetUserDetail("DoctorID");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/GetDoctorScheduleDetails";
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
                    msg=jsonObject.optString("Message","");
                    pass = jsonObject.optBoolean("Flag",true);
                    String[] data=new String[2];

                    data[0] = jsonObject.optString("Scheduledtime");
                    data[1]=jsonObject.optString("event_start").replace("/Date(", "").replace(")/", "");

                    Calendar event_start =Calendar.getInstance() ;
                    event_start.setTimeInMillis(Long.parseLong(data[1]));

                    ScheduleData.add(data);

                    SimpleDateFormat formatted = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                    ScheduledDates.add(formatted.format(event_start.getTime()));
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
                new AlertDialog.Builder(Schedules.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(Schedules.this,Home.class);
                                startActivity(intent);
                            }
                        }).setCancelable(false).show();
            }
            else {
                //Referring the List view in the Tile
                GridView visitList= (GridView) findViewById(R.id.gridschedulelist);
                CustomAdapter adapter=new CustomAdapter(Schedules.this, R.layout.scheulegridview, ScheduleData,"ScheduleCalendar");
                visitList.setAdapter(adapter);

                SimpleDateFormat formatted = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                int position =  ScheduledDates.indexOf(formatted.format(SelectedDate.getTime()));
                if (position>=0) {  //Scroll to today's Date
                    gridview.smoothScrollToPosition(position);
                }
                else {
                    Toast.makeText(Schedules.this, R.string.NoSchedulesToday, Toast.LENGTH_SHORT).show();
                }

                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {

                        SelectedDate.set(i, i1, i2);
                        SimpleDateFormat formatted = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        int position = ScheduledDates.indexOf(formatted.format(SelectedDate.getTime()));


                        if (position >= 0) {
                            gridview.smoothScrollToPosition(position);
                        } else {
                            Toast.makeText(Schedules.this, R.string.NoSchedules, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }
    }
}
