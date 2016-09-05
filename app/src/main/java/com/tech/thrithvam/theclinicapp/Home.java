package com.tech.thrithvam.theclinicapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Home extends AppCompatActivity {

    DatabaseHandler db= new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /*Toast.makeText(Home.this, "UserName"+db.GetUserDetail("UserName"), Toast.LENGTH_LONG).show();
        Toast.makeText(Home.this, "ClinicID"+db.GetUserDetail("ClinicID"), Toast.LENGTH_LONG).show();
        Toast.makeText(Home.this, db.GetUserDetail("DoctorID"), Toast.LENGTH_LONG).show();
        Toast.makeText(Home.this, db.GetUserDetail("DoctorName"), Toast.LENGTH_LONG).show();*/
    }
    public void goaddImage(View view){
        Intent intent=new Intent(Home.this,AddImage.class);
        startActivity(intent);

    }
    public void goAppointments(View view){
        Intent intent=new Intent(Home.this,Appointments.class);
        startActivity(intent);

    }
    public void goSchedules(View view){
        Intent intent=new Intent(Home.this,Schedules.class);
        startActivity(intent);

    }
    public void goRemainders(View view){
        Intent intent=new Intent(Home.this,Reminders.class);
        startActivity(intent);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                final Intent intentUser = new Intent(this, Login.class);
                new AlertDialog.Builder(Home.this).setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.UserLogout();
                                startActivity(intentUser);
                                finish();
                            }
                        }).setNegativeButton(R.string.no_button, null).show();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
