package com.tech.thrithvam.theclinicapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.CalendarContract;
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
    Calendar cal= Calendar.getInstance();
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
        TextView P_Name,Apmnt_No,Allotting_Time,Mobile,Location;
        TextView Time1,day,month,dayofweek;
        TextView r_month,r_day,r_dayofweek,r_Time,Clinicname;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);


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
            /*===============================Schedule Calendar List======================================*/
            case "ScheduleCalendar":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.scheulegridview, null);
                    holder.month  = (TextView) convertView.findViewById(R.id.txtmonth);
                    holder.day=(TextView) convertView.findViewById(R.id.monthday);
                    holder.dayofweek=(TextView) convertView.findViewById(R.id.txtweekday);
                    holder.Time1  = (TextView) convertView.findViewById(R.id.view_agenda_event_starttime1 );
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.Time1.setText(objects.get(position)[0]);
                if(!objects.get(position)[1].equals("null")){
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[1]));

                    String strmonth=(String) android.text.format.DateFormat.format("MMM",cal.getTime()); //Jun
                    holder.month.setText(strmonth);
                    String strday=(String) android.text.format.DateFormat.format("dd",cal.getTime()); //20
                    holder.day.setText(strday);
                    String dayOfTheWeek =(String) android.text.format.DateFormat.format("EEE",cal.getTime()); //Mon
                    holder.dayofweek.setText(dayOfTheWeek);
                }

                break;
              /*===============================Schedule Calendar List======================================*/
            case "ReminderSchedule":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.reminderschedule, null);
                    holder.r_month  = (TextView) convertView.findViewById(R.id.txt_reminder_month);
                    holder.r_day=(TextView) convertView.findViewById(R.id.txt_reminder_date);
                    holder.r_dayofweek=(TextView) convertView.findViewById(R.id.txt_reminder_week_day);
                    holder.r_Time  = (TextView) convertView.findViewById(R.id.txt_event_time );
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.r_Time.setText(objects.get(position)[1]+'-'+objects.get(position)[2]);
                if(!objects.get(position)[0].equals("null")){
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[0]));
                    String strmonth=(String) android.text.format.DateFormat.format("MMM",cal.getTime()); //Jun
                    holder.r_month.setText(strmonth);
                    String strday=(String) android.text.format.DateFormat.format("dd",cal.getTime()); //20
                    holder.r_day.setText(strday);
                    String dayOfTheWeek =(String) android.text.format.DateFormat.format("EEE",cal.getTime()); //Mon
                    holder.r_dayofweek.setText(dayOfTheWeek);
                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String Clinicname= objects.get(position)[3].toString();
                        String Time= objects.get(position)[1]+'-'+objects.get(position)[2];
                        cal.setTimeInMillis(Long.parseLong(objects.get(position)[0]));
                        int month=Integer.parseInt((String) android.text.format.DateFormat.format("MM",cal.getTime()));
                        int year=Integer.parseInt((String) android.text.format.DateFormat.format("yyyy", cal.getTime()));
                        int day=Integer.parseInt((String) android.text.format.DateFormat.format("dd", cal.getTime()));
                        int S_hour,S_min,E_hour,E_min;
                        String format= objects.get(position)[1].substring(objects.get(position)[1].length() - 2, objects.get(position)[1].length());
                        format.trim();
                        if (format.equals("AM") ){
                            String S_time[] = objects.get(position)[1].split(":");
                            S_hour=Integer.parseInt(S_time[0]);
                            S_min=Integer.parseInt(S_time[1].substring(0,1));
                        }
                        else {
                            String S_time[] = objects.get(position)[1].split(":");
                            S_hour=Integer.parseInt(S_time[0])+12;
                            S_min=Integer.parseInt(S_time[1].substring(0,1));
                        }

                        String format2= objects.get(position)[1].substring(objects.get(position)[1].length() - 2, objects.get(position)[1].length());
                        format2.trim();
                        if (format2.equals("AM") ){
                            String E_time[] = objects.get(position)[2].split(":");
                            E_hour=Integer.parseInt(E_time[0]);
                            E_min=Integer.parseInt(E_time[1].substring(0,1));
                        }
                        else {
                            String E_time[] = objects.get(position)[2].split(":");
                            E_hour=Integer.parseInt(E_time[0])+12;
                            E_min=Integer.parseInt(E_time[1].substring(0,1));
                        }

                        Calendar beginTime = Calendar.getInstance();
                        beginTime.set(year, month-1, day,S_hour, S_min);
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(year, month-1, day, E_hour,E_min);
                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, "Schedule Reminder")
                                .putExtra(CalendarContract.Events.DESCRIPTION, "From"+Time)
                                .putExtra(CalendarContract.Events.EVENT_LOCATION,Clinicname)
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                                .putExtra(Intent.EXTRA_EMAIL,"");
                        adapterContext.startActivity(intent);
                    }
                });
                break;
            default:
                break;
        }
       /* if (position % 2 == 1) {
            convertView.setBackgroundColor(Color.parseColor("#c4c3c3"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#e1e6ef"));}*/
        return convertView;
    }

    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) adapterContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
