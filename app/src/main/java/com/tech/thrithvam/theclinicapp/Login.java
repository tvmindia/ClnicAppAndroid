package com.tech.thrithvam.theclinicapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Login extends AppCompatActivity {
    EditText userName;
    EditText password;
    DatabaseHandler db= new DatabaseHandler(this);
    Cryptography cryptography=new Cryptography();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(db.GetUserDetail("UserName")!=null)
        {
            Intent goHome = new Intent(Login.this, Home.class);
            goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(goHome);
            finish();
        }
        setContentView(R.layout.activity_login);

        userName=(EditText)findViewById(R.id.userName);
        password=(EditText)findViewById(R.id.password);
    }
    public void loginButton(View view) {
        if(isOnline()) {
            userName.setText(userName.getText().toString().trim());
            password.setText(password.getText().toString().trim());
            if (userName.getText().toString().equals("")) {
                userName.setError(getResources().getString(R.string.username_error_msg));
                userName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    public void afterTextChanged(Editable edt) {
                        userName.setError(null);
                    }
                });
            } else if (password.getText().toString().equals("")) {
                password.setError(getResources().getString(R.string.password_error_msg));
            } else {
                new UserLogin().execute();
            }
        }
        else {
            Toast.makeText(Login.this,R.string.network_off_alert, Toast.LENGTH_LONG).show();
        }
    }
    public class UserLogin extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData, passwordString,usernameString,clinicID;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ProgressDialog pDialog=new ProgressDialog(Login.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            usernameString=userName.getText().toString();
            passwordString=password.getText().toString();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            //----------encrypting ---------------------------
            usernameString=cryptography.Encrypt(usernameString);
            passwordString=cryptography.Encrypt(passwordString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "Webservices/WebServices.asmx/UserLogin";
            HttpURLConnection c = null;
            try {
                postData = "{\"username\":\"" +usernameString + "\",\"password\":\"" + passwordString + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
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
                        strJson=sb.substring(a, b + 1);
                        strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"") + "}";
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
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag");
                    clinicID=jsonObject.optString("ClinicID", "");
                }
            } catch (JSONException ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            //Toast.makeText(Login.this,strJson, Toast.LENGTH_LONG).show();

            if(!pass) {
                new AlertDialog.Builder(Login.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                password.setText("");
                            }
                        }).setCancelable(false).show();
            }
            else {
                db.UserLogout();
                db.UserLogin(userName.getText().toString(), clinicID);
                Intent goHome = new Intent(Login.this, Home.class);
                goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Toast.makeText(Login.this,msg,Toast.LENGTH_LONG).show();
                startActivity(goHome);
                finish();
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    //-------------fn to be deleted------------
    public void backdoor(View view) {
        userName.setText("sreejith");
        password.setText("abc");
        new UserLogin().execute();
    }
}
