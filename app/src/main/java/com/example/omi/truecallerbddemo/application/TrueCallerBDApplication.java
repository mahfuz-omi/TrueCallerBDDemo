package com.example.omi.truecallerbddemo.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.omi.truecallerbddemo.model.UserCredential;
import com.google.gson.Gson;

/**
 * Created by omi on 12/28/2016.
 */

public class TrueCallerBDApplication extends Application {

    private boolean isLoggedIn;
    private UserCredential userCredential;


    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate()
    {
        super.onCreate();

        this.sharedPreferences = getSharedPreferences("omi", Context.MODE_PRIVATE);
        this.isLoggedIn = this.sharedPreferences.getBoolean("isUserLoggedIn",false);

        String userCredentialSt = this.sharedPreferences.getString("userCredential",null);
        if(userCredentialSt == null)
        {
            this.userCredential = null;
        }

        else
        {
            this.userCredential = new Gson().fromJson(userCredentialSt,UserCredential.class);
        }
    }
    public void logginUser(UserCredential userCredential)
    {
        this.userCredential = userCredential;
        this.isLoggedIn = true;



        String userCredentialSt = new Gson().toJson(userCredential);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userCredential",userCredentialSt);
        editor.putBoolean("isUserLoggedIn",true);
        editor.commit();
    }

    public void logoutUser()
    {
        this.isLoggedIn = false;
        this.userCredential = null;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userCredential",null);
        editor.putBoolean("isUserLoggedIn",false);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public UserCredential getUserCredential() {
        return userCredential;
    }

    public void setUserCredential(UserCredential userCredential) {
        this.userCredential = userCredential;
    }
}
