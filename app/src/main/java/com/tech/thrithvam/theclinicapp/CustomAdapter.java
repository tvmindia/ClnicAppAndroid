package com.tech.thrithvam.theclinicapp;

import android.content.Context;
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
    public class Holder
    {
        //visit items-----------------------------------------------
        TextView Name,Age,Date;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        Calendar cal= Calendar.getInstance();
        final SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        switch (calledFrom) {
            //-------------------for line items--------------------------------
            /*case "AddImage":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.line_item, null);
                    holder.headerID = (TextView) convertView.findViewById(R.id.headerID);
                    holder.headerTxt = (TextView) convertView.findViewById(R.id.headerTxt);
                    holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.lineItemDetails);
                    for (int i = 0; i < Integer.parseInt(objects.get(position)[2]); i++) {
                        View itemDetail = inflater.inflate(R.layout.item_details_two_col, null);
                        TextView lineItemDetailLeft = (TextView) itemDetail.findViewById(R.id.itemLeft);
                        TextView lineItemDetailRight = (TextView) itemDetail.findViewById(R.id.itemRight);
                        lineItemDetailLeft.setText(objects.get(position)[i + 3]);
                        i++;
                        lineItemDetailRight.setText(objects.get(position)[i + 3]);
                        holder.linearLayout.addView(itemDetail);
                    }
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.headerID.setText(objects.get(position)[0]);
                holder.headerTxt.setText(objects.get(position)[1]);
                if (position % 2 == 1) {
                    convertView.setBackgroundColor(Color.parseColor("#fff8e3"));
                } else {
                    convertView.setBackgroundColor(Color.parseColor("#fff4d0"));
                }
                break;*/
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
