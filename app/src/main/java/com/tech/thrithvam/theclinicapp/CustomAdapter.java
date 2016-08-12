package com.tech.thrithvam.theclinicapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CustomAdapter extends ArrayAdapter<String[]> {
    Context adapterContext;
    private static LayoutInflater inflater=null;
    private ArrayList<String[]> objects;
    private String calledFrom;
    FileInputStream fileInputStream;
    String FileName;
    String description;
    DatabaseHandler db;
    public CustomAdapter(Context context, int textViewResourceId, ArrayList<String[]> objects, String calledFrom, String fileName,String description, FileInputStream fileInputStream) {
        super(context, textViewResourceId, objects);
        adapterContext=context;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects=objects;
        this.calledFrom=calledFrom;
        this.fileInputStream=fileInputStream;
        this.FileName=fileName;
        this.description=description;
        db=new DatabaseHandler(context);
    }

    public CustomAdapter(Context context, int textViewResourceId, ArrayList<String[]> objects, String calledFrom) {
        super(context, textViewResourceId, objects);
        adapterContext=context;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects=objects;
        this.calledFrom=calledFrom;
        db=new DatabaseHandler(context);
    }
    public class Holder
    {
        //visit items-----------------------------------------------
        TextView Name,Age,Date;
        TextView AppDate,Count;
        TextView P_Name,Apmnt_No,Allotting_Time,Mobile,Apmnt_Date,Location,DoctorId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        Calendar cal= Calendar.getInstance();

        switch (calledFrom) {
            /*===============================Appointment List======================================*/
            case "Appointments":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.appointments_listview, null);
                    holder.AppDate = (TextView) convertView.findViewById(R.id.appdate);
                    holder.Count = (TextView) convertView.findViewById(R.id.Count);

                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }


                holder.AppDate.setText(objects.get(position)[0]);
                holder.Count.setText(objects.get(position)[1]);

                if(!objects.get(position)[0].equals("null")){                                    //updated
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[0]));
                    holder.AppDate.setText(formatted.format(cal.getTime()));
                }
                final int FinalPosition1 = position;
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                         /*on click codes goes here */
                            Intent goPatientDetails = new Intent(adapterContext, PatientDetails.class);
                            goPatientDetails.putExtra("AppointmentDate",objects.get(FinalPosition1)[0]);
                            adapterContext.startActivity(goPatientDetails);
                        }
                        else {
                            Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                break;
             /*===============================Appointment Patient List======================================*/
            case "PatientDetails":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.patientdetails_listview, null);
                    holder.P_Name = (TextView) convertView.findViewById(R.id.p_name);
                    holder.Location = (TextView) convertView.findViewById(R.id.Location);
                    holder.Apmnt_No = (TextView) convertView.findViewById(R.id.AppointmentNo);
                    holder.Allotting_Time = (TextView) convertView.findViewById(R.id.allotedtime);
                    holder.Mobile = (TextView) convertView.findViewById(R.id.Mobile);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.P_Name.setText(objects.get(position)[0]);
                holder.Apmnt_No.setText(objects.get(position)[1]);
                holder.Allotting_Time.setText(objects.get(position)[2]);
                holder.Mobile.setText(objects.get(position)[3]);
                holder.Location.setText(objects.get(position)[4]);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                         /*on click codes goes here */
                        }
                        else {
                            Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;

            //--------------------------for upload file from widget(to select punch item)------------------
            case "AddImage":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.visit_list, null);
                    holder.Name = (TextView) convertView.findViewById(R.id.name);
                    holder.Age = (TextView) convertView.findViewById(R.id.age);
                    holder.Date = (TextView) convertView.findViewById(R.id.date);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.Name.setText(objects.get(position)[0]);
                holder.Age.setText(objects.get(position)[1]);
                if (!objects.get(position)[2].equals("null")) {
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[2]));
                    holder.Date.setText(formatted.format(cal.getTime()));
                } else {
                    holder.Date.setText("");
                }
                final int FinalPosition = position;
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                            FileUpload hfu= new FileUpload(adapterContext,adapterContext.getResources().getString(R.string.url) +"Webservices/WebServices.asmx/AddVisitAttatchment",fileInputStream,FileName,objects.get(FinalPosition)[3],db.GetUserDetail("UserName"),db.GetUserDetail("ClinicID"),description);
                            hfu.UploadFileFn();  //calling within app
                        }
                        else {
                            Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                break;
            default:
                break;
        }
        if (position % 2 == 1) {
            convertView.setBackgroundColor(Color.parseColor("#c4c3c3"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#e1e6ef"));}
        return convertView;
    }

    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) adapterContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
