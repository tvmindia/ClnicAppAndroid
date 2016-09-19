package com.tech.thrithvam.theclinicapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Schedules2 extends AppCompatActivity {

    private Spinner spinnermonthyear;
    String Month,Year;
    GridView gridview;
    TextView T_Month,T_monthday,T_weekday,T_scheduledtimes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules2);
        gridview=(GridView)findViewById(R.id.gridviewlist1);
        spinnermonthyear = (Spinner) findViewById(R.id.month_year);
        T_Month=(TextView)findViewById(R.id.txtschedulemonth);
        T_monthday=(TextView)findViewById(R.id.txtschedulemonthday);
        T_scheduledtimes=(TextView)findViewById(R.id.scheduledtimes);
        T_weekday=(TextView)findViewById(R.id.txtweekday);

        List<String> list = new ArrayList<String>();
        //For Listing Last Six Months
        for (int l = 6; l >= 0; l--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -l);
            //format it to MMM-yyyy // Jan-2012
            String previousMonthYear = new SimpleDateFormat("MMMM-yyyy").format(cal.getTime());
            list.add(previousMonthYear);
        }
        //For Listing Next Six Months
        for (int l = 1; l <= 6; l++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, l);
            //format it to MMM-yyyy // Jan-2012
            String previousMonthYear = new SimpleDateFormat("MMMM-yyyy").format(cal.getTime());
            list.add(previousMonthYear);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnermonthyear.setAdapter(dataAdapter);
        spinnermonthyear.setSelection(6);
        spinnermonthyear.setPrompt("Select Month");

        //--------Setting spinner-------------------
        spinnermonthyear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               /* Toast.makeText(Schedules2.this, spinnermonthyear.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();*/

                String monthyear = spinnermonthyear.getSelectedItem().toString();
                String split[] = monthyear.split("-");//splitting the month and year from spinner
                Month = split[0];
                Year = split[1];
              /*  Toast.makeText(Schedules2.this, Month, Toast.LENGTH_SHORT).show();
                Toast.makeText(Schedules2.this, Year, Toast.LENGTH_SHORT).show();*/
                new Scheduleddates().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

 /*----------------------Thread to load Scheduled  dates --------------------------*/
        public class Scheduleddates extends AsyncTask<Void , Void, Void> {
            int status;StringBuilder sb;
            String strJson, postData,clinicid,doctorid;
            JSONArray jsonArray;
            String msg;
            boolean pass=false;
            Cryptography cryptography=new Cryptography();
            ArrayList<String[]> ScheduleData =new ArrayList<>();
            ProgressDialog pDialog=new ProgressDialog(Schedules2.this);
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setMessage(getResources().getString(R.string.wait));
                pDialog.setCancelable(false);
                pDialog.show();
                DatabaseHandler db= new DatabaseHandler(Schedules2.this);
                clinicid=db.GetUserDetail("ClinicID");
                doctorid=db.GetUserDetail("DoctorID");
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/GetDoctorScheduleDetailsbymonth";
                HttpURLConnection c = null;
                try {
                    postData = "{\"doctorid\":\"" +doctorid + "\",\"clinicid\":\"" +clinicid + "\",\"Month\":\"" +Month + "\",\"Year\":\"" +Year + "\"}";
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

                        data[0] = jsonObject.optString("Scheduledtime","");
                        data[1]=jsonObject.optString("event_start").replace("/Date(", "").replace(")/", "");

                        Calendar event_start =Calendar.getInstance();
                        event_start.setTimeInMillis(Long.parseLong(data[1]));
                        ScheduleData.add(data);
                    }
                    if(ScheduleData.size()<9) {
                        int count=9-ScheduleData.size();
                        for(int i=0;i<count;i++) {
                            String[] data=new String[1];
                            data[0]="";
                            ScheduleData.add(data);
                        }
                    }
                } catch (Exception ex) {
                    msg=ex.getMessage();
                }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (pDialog.isShowing())
                    pDialog.dismiss();
                T_scheduledtimes.setText("");
                T_weekday.setText("---");
                T_monthday.setText("--");
                T_Month.setText("---");
                if(!pass) {
                    if(ScheduleData.size()<9) {
                        int count=9-ScheduleData.size();
                        for(int i=0;i<count;i++) {
                            String[] data=new String[1];
                            data[0]="";
                            ScheduleData.add(data);
                        }
                    }

                    CustomAdapter adapter = new CustomAdapter(Schedules2.this, R.layout.scheulegridview, ScheduleData, "ScheduleCalendar2");
                    gridview.setAdapter(adapter);
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        }
                    });

                } else {
                    CustomAdapter adapter = new CustomAdapter(Schedules2.this, R.layout.scheulegridview, ScheduleData, "ScheduleCalendar2");
                    gridview.setAdapter(adapter);
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (ScheduleData.get(position)[0] != "") {
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(Long.parseLong(ScheduleData.get(position)[1]));
                                String strmonth = (String) android.text.format.DateFormat.format("MMM", cal.getTime());
                                String strday = (String) android.text.format.DateFormat.format("dd", cal.getTime());
                                String dayOfTheWeek = (String) android.text.format.DateFormat.format("EEE", cal.getTime());

                                T_scheduledtimes.setText(ScheduleData.get(position)[0]);
                                T_weekday.setText(dayOfTheWeek);
                                T_monthday.setText(strday);
                                T_Month.setText(strmonth);
                            } else {
                                T_scheduledtimes.setText("");
                                T_weekday.setText("---");
                             T_monthday.setText("--");
                             T_Month.setText("---");
                         }
                        }
                    });


                }


            }
 }

}


