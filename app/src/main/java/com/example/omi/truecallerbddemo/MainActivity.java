package com.example.omi.truecallerbddemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.omi.truecallerbddemo.application.TrueCallerBDApplication;
import com.example.omi.truecallerbddemo.receiver.IncomingCallReceiver;
import com.example.omi.truecallerbddemo.service.ContactsSendService;

public class MainActivity extends AppCompatActivity {

    private int READ_PHONE_STATE_PERMISSION_CODE = 1;
    private int OVERLAY_PERMISSION_REQ_CODE = 2;

    boolean isPhoneStateReadGranted,isContactReadGranted,isDrawingGranted;

    Button startServiceButton,endServiceButton;

    public final static String PREFERENCE_FIRST_RUN = "isFirstRun";
    public final static String PREFERENCE_IS_STARTED = "isStarted";
    ComponentName receiverComponent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!((TrueCallerBDApplication)getApplication()).isLoggedIn())
        {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        System.out.println(Build.MANUFACTURER);
        receiverComponent = new ComponentName(this, IncomingCallReceiver.class);
        getPackageManager().setComponentEnabledSetting(receiverComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);

        this.startServiceButton = (Button)findViewById(R.id.startServiceButton);
        this.endServiceButton = (Button)findViewById(R.id.endServiceButton);

        this.startServiceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                requestReadPhoneStatePermission();
            }
        });

        this.endServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPackageManager().setComponentEnabledSetting(receiverComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);

                Toast.makeText(MainActivity.this, "service stopped", Toast.LENGTH_SHORT).show();
                endServiceButton.setEnabled(false);
                startServiceButton.setEnabled(true);
            }
        });

        this.checkStartButton();

    }

    private boolean isReadPhoneStateContactDrawingAllowed()
    {
        //Getting the permission status
        int phoneStateResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int contactResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        this.isDrawingGranted = true;
        if(Build.VERSION.SDK_INT >= 23)
        {
            if (!Settings.canDrawOverlays(this))
                this.isDrawingGranted = false;
        }

        if(phoneStateResult == PackageManager.PERMISSION_GRANTED)
            this.isPhoneStateReadGranted = true;
        if(contactResult == PackageManager.PERMISSION_GRANTED)
            this.isContactReadGranted = true;

        if (this.isPhoneStateReadGranted && this.isContactReadGranted && this.isDrawingGranted)
            return true;
        return false;
    }


    private void requestReadPhoneStatePermission()
    {
        if(this.isDrawingGranted == false)
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }

        if(this.isPhoneStateReadGranted == false || this.isContactReadGranted == false)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS},READ_PHONE_STATE_PERMISSION_CODE);
        else
            checkStartButton();

    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == READ_PHONE_STATE_PERMISSION_CODE)
        {

            //If permission is granted
            if(grantResults.length >0)
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    this.isPhoneStateReadGranted = true;
                else
                    this.isPhoneStateReadGranted = false;
                if(grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    this.isContactReadGranted = true;
                else
                    this.isContactReadGranted = false;
            }
            else
            {

                this.startServiceButton.setEnabled(true);
                Toast.makeText(this,"Oops you just denied the permissions",Toast.LENGTH_LONG).show();
            }

            this.checkStartButton();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        this.checkStartButton();
    }



    public void checkStartButton()
    {
        if(isReadPhoneStateContactDrawingAllowed())
        {
            //SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            //boolean isStartedService = p.getBoolean(PREFERENCE_IS_STARTED, false);
            //if(isStartedService)
            //{
                this.startServiceButton.setEnabled(false);
                this.endServiceButton.setEnabled(true);
                getPackageManager().setComponentEnabledSetting(receiverComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
            //}




            int status = getPackageManager().getComponentEnabledSetting(receiverComponent);
            if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            {
                System.out.println("receiver is enabled");
            }
            else if(status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            {
                System.out.println("receiver is disabled");
            }

            if(Build.MANUFACTURER.equalsIgnoreCase("xiaomi"))
            {
                new AlertDialog.Builder(this)
                        .setTitle("Message")
                        .setMessage("For getting notification when call arrives, you have to autostart this application. Go to settings->permissions->autostart and then add this app to autostart")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();
            }

            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isFirstRun = p.getBoolean(PREFERENCE_FIRST_RUN, true);
            if(isFirstRun)
            {
                //start service
                Intent intent = new Intent(this, ContactsSendService.class);
                startService(intent);
            }

            return;
        }
        else
        {
            getPackageManager().setComponentEnabledSetting(receiverComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this,PrivacyPolicyActivity.class);
        startActivity(intent);
        return true;

    }
}
