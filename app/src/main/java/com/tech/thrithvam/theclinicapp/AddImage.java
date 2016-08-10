package com.tech.thrithvam.theclinicapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.senab.photoview.PhotoViewAttacher;

public class AddImage extends AppCompatActivity {

    ImageView newImage;
    TextView fileSize;
    EditText fileName,description;
    PhotoViewAttacher mAttacher;
    File imageFile;
    Boolean isFromCamera=false;
    final int PHOTO_FROM_CAMERA=555;
    final int PHOTO_FROM_GALLERY=444;
    Bundle extras;
    DatabaseHandler db= new DatabaseHandler(this);
    Cryptography cryptography=new Cryptography();
    String descriptionString;
    String searchstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        extras=getIntent().getExtras();

        newImage=(ImageView)findViewById(R.id.newImage);
        fileSize=(TextView)findViewById(R.id.fileSize);
        fileSize.setVisibility(View.GONE);
        fileName=(EditText)findViewById(R.id.fileName);
        description=(EditText)findViewById(R.id.description);

        //---------------------making directory to store image temporally if not exists----
        File folder = new File(Environment.getExternalStorageDirectory() + "/ClinicApp");
        if (!folder.exists()) {
            folder.mkdir();
        }
        //--------------------checking whether called from widget---------------------
        /*if(extras.getString("From").equals("widget")){    //checking whether call is from widget
            Toast.makeText(AddImage.this,R.string.take_photo_instruction, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageFile = new File(Environment.getExternalStorageDirectory(), "ClinicApp/TempImage.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, PHOTO_FROM_CAMERA);
        }*/
        final CharSequence[] items = {getResources().getString(R.string.take_photo), getResources().getString(R.string.choose_from_galley), getResources().getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddImage.this);
        builder.setTitle(getResources().getString(R.string.upload_image));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getResources().getString(R.string.take_photo))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    imageFile = new File(Environment.getExternalStorageDirectory(), "ClinicApp/TempImage.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                    startActivityForResult(intent, PHOTO_FROM_CAMERA);
                } else if (items[item].equals(getResources().getString(R.string.choose_from_galley))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), PHOTO_FROM_GALLERY);
                } else if (items[item].equals(getResources().getString(R.string.cancel))) {
                    finish();
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap;
        if (requestCode == PHOTO_FROM_CAMERA && resultCode == RESULT_OK)
        {
            fileSize.setText(Long.toString((imageFile.length() / 1024)) + " KB");
            fileSize.setVisibility(View.VISIBLE);
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            newImage.setImageBitmap(bitmap);
            mAttacher = new PhotoViewAttacher(newImage); //library for pinch zoom
            isFromCamera=true;
            SimpleDateFormat currentDate = new SimpleDateFormat("ddMMyy_hhmmssS");
            Date todayDate = new Date();
            String thisDate = currentDate.format(todayDate);
            String filenameGenerated="img"+thisDate;
            fileName.setText(filenameGenerated);
        }
        else  if (requestCode == PHOTO_FROM_GALLERY && resultCode == RESULT_OK)
        {
            Uri selectedImageUri = data.getData();
            String[] projection = { MediaStore.MediaColumns.DATA };
            CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,null);
            Cursor cursor =cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            bitmap = BitmapFactory.decodeFile(selectedImagePath);
            newImage.setImageBitmap(bitmap);
            imageFile =new File(cursor.getString(column_index));
            fileName.setText(imageFile.getName());
            fileSize.setText(Long.toString((imageFile.length() / 1024)) + " KB");
            fileSize.setVisibility(View.VISIBLE);
            mAttacher = new PhotoViewAttacher(newImage); //library for pinch zoom
            cursor.close();
        }
        else {
            finish();
        }
    }

    public void rotateImage(View view){
          mAttacher.setRotationBy(90f);
    }

    public void Upload(View view){
        if(isOnline())
        {
            if(db.GetUserDetail("UserName")!=null)
            { //checking whether logged in or not
                descriptionString=description.getText().toString();
                /*--------------------calling uploading functions-----------------------------------*/
                new VisitList().execute();
            }
            else {
                Toast.makeText(AddImage.this,R.string.login_instruction,Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else
        {
            Toast.makeText(AddImage.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //----------------------Thread to load visit list with name and date etc------------
    public class VisitList extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> visitListData =new ArrayList<>();
        ProgressDialog pDialog=new ProgressDialog(AddImage.this);
        FileInputStream fStream=null;
        String fileNameString = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/GetVisitList";
            HttpURLConnection c = null;
            try {
                postData = "{\"username\":\"" +db.GetUserDetail("UserName") + "\"}";
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
                    pass=jsonObject.optBoolean("Flag", true);
                    String[] data=new String[4];
                    data[0]=jsonObject.optString("Name");
                    data[1]=jsonObject.optString("DOB");
                    data[2]=jsonObject.optString("Date").replace("/Date(", "").replace(")/", "");
                    data[3]=jsonObject.optString("VisitID");
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
                new AlertDialog.Builder(AddImage.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new VisitList().execute();
                            }
                        }).setCancelable(false).show();
            }
            else {

                try {
                    fStream= new FileInputStream(imageFile);
                    fileNameString=fileName.getText().toString().trim();
                    if(isFromCamera){
                        fileNameString+=".jpg";}
                } catch (FileNotFoundException e) {
                    Toast.makeText(AddImage.this, R.string.unsuccessful, Toast.LENGTH_SHORT).show();
                }

                LinearLayout linearLayoutobj =new LinearLayout(AddImage.this);
                linearLayoutobj.setOrientation(LinearLayout.VERTICAL);
                linearLayoutobj.setPadding(5,0,5,0);

                ListView visitList=new ListView(AddImage.this);
                CustomAdapter adapter=new CustomAdapter(AddImage.this, R.layout.visit_list, visitListData,"AddImage",fileNameString,descriptionString,fStream);
                visitList.setAdapter(adapter);
              /*  ListView.LayoutParams mParam = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.Listview_height));
                visitList.setLayoutParams(mParam);*/
                linearLayoutobj.addView(visitList);


                final AlertDialog.Builder builder = new AlertDialog.Builder(AddImage.this);
                builder.setTitle(R.string.select_visit);
                builder.setView(linearLayoutobj);
                builder.setPositiveButton(R.string.btnadvncesearch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        LinearLayout linearLayoutsearch = new LinearLayout(AddImage.this);
                        linearLayoutsearch.setOrientation(LinearLayout.VERTICAL);
                        linearLayoutsearch.setPadding(15, 0,15,0);

                        /*search textbox*/
                        final EditText searchbox = new EditText(AddImage.this);
                        searchbox.setHint(R.string.search);
                        linearLayoutsearch.addView(searchbox);


                        final AlertDialog.Builder buildersearch = new AlertDialog.Builder(AddImage.this);
                        buildersearch.setTitle(R.string.search_patient);
                        buildersearch.setView(linearLayoutsearch);

                        buildersearch.setPositiveButton(R.string.btnsearch, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                searchstring = searchbox.getText().toString();
                                new VisitSearch().execute();
                            }
                        });

                        buildersearch.setNegativeButton(R.string.btnback, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new VisitList().execute();
                            }
                        });
                        final AlertDialog alert1 = buildersearch.create();
                        alert1.show();

                    }
                });
                final AlertDialog alert = builder.create();
                alert.show();


            }
        }
    }


    //----------------------Thread to load visit list with during Search------------//

    public class VisitSearch extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData,Stringsearch,clinicid;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> visitListData =new ArrayList<>();
        ProgressDialog pDialog=new ProgressDialog(AddImage.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            Stringsearch=searchstring;
            DatabaseHandler db= new DatabaseHandler(AddImage.this);
            clinicid=db.GetUserDetail("ClinicID");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/SearchVisitList";
            HttpURLConnection c = null;
            try {
                postData = "{\"stringsearch\":\"" +Stringsearch + "\",\"clinicid\":\"" +clinicid + "\"}";
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
                    msg=jsonObject.optString("Message", "");
                    pass=jsonObject.optBoolean("Flag", true);
                    String[] data=new String[4];
                    data[0]=jsonObject.optString("Name");
                    data[1]=jsonObject.optString("DOB");
                    data[2]=jsonObject.optString("Date").replace("/Date(", "").replace(")/", "");
                    data[3]=jsonObject.optString("VisitID");
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
                new AlertDialog.Builder(AddImage.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new VisitList().execute();

                            }
                        }).setCancelable(false).show();
            }
            else {
                FileInputStream fStream=null;
                String fileNameString = null;
                try {
                    fStream= new FileInputStream(imageFile);
                    fileNameString=fileName.getText().toString().trim();
                    if(isFromCamera){
                        fileNameString+=".jpg";}
                } catch (FileNotFoundException e) {
                    Toast.makeText(AddImage.this, R.string.unsuccessful, Toast.LENGTH_SHORT).show();
                }

                LinearLayout linearLayoutobj =new LinearLayout(AddImage.this);
                linearLayoutobj.setOrientation(LinearLayout.VERTICAL);
                linearLayoutobj.setPadding(5, 0, 5, 0);

                ListView visitList=new ListView(AddImage.this);
                CustomAdapter adapter=new CustomAdapter(AddImage.this, R.layout.visit_list, visitListData,"AddImage",fileNameString,descriptionString,fStream);
                visitList.setAdapter(adapter);
              /*  ListView.LayoutParams mParam = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.Listview_height));
                visitList.setLayoutParams(mParam);*/
                linearLayoutobj.addView(visitList);


                final AlertDialog.Builder builder = new AlertDialog.Builder(AddImage.this);
                builder.setTitle(R.string.select_visit);
                builder.setView(linearLayoutobj);
                builder.setPositiveButton(R.string.btnadvncesearch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LinearLayout linearLayoutsearch = new LinearLayout(AddImage.this);
                        linearLayoutsearch.setOrientation(LinearLayout.VERTICAL);
                        linearLayoutsearch.setPadding(15,0,15,0);

                        /*search textbox*/
                        final EditText searchbox = new EditText(AddImage.this);
                        searchbox.setHint(R.string.search);
                        linearLayoutsearch.addView(searchbox);


                        final AlertDialog.Builder buildersearch = new AlertDialog.Builder(AddImage.this);
                        buildersearch.setTitle(R.string.search_patient);
                        buildersearch.setView(linearLayoutsearch);
                        buildersearch.setPositiveButton(R.string.btnsearch, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                searchstring = searchbox.getText().toString();
                                new VisitSearch().execute();
                            }
                        });

                        buildersearch.setNegativeButton(R.string.btnback, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new VisitList().execute();
                            }
                        });
                        final AlertDialog alert2 = buildersearch.create();
                        alert2.show();

                    }
                });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
