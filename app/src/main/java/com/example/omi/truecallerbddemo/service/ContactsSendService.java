package com.example.omi.truecallerbddemo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.omi.truecallerbddemo.MainActivity;
import com.example.omi.truecallerbddemo.application.TrueCallerBDApplication;
import com.example.omi.truecallerbddemo.constant.WebServiceURL;
import com.example.omi.truecallerbddemo.model.Contact;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by omi on 12/14/2016.
 */

public class ContactsSendService extends Service {

    ArrayList<Contact> contacts;
    Gson gson;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        this.gson = new Gson();

        this.contacts = new ArrayList<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replace(" ","");
            phoneNumber = phoneNumber.replace("-","");
            Contact contact = new Contact(name,phoneNumber);
            contacts.add(contact);
        }
        phones.close();

        String contantsSendJson = gson.toJson(contacts);
        System.out.println("contacts json: "+contantsSendJson);

        JsonObjectRequest sendContactRequest = new JsonObjectRequest(Request.Method.POST, WebServiceURL.SEND_CONTACT_URL,contantsSendJson, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                System.out.println("teamMovedResponse string:  "+response.toString());
                if(response.has("success"))
                {
                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(ContactsSendService.this);
                    SharedPreferences.Editor editor = p.edit();
                    editor.putBoolean(MainActivity.PREFERENCE_FIRST_RUN,false);
                    editor.commit();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                System.out.println(error.toString());
            }
        }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("access-token",((TrueCallerBDApplication)getApplication()).getUserCredential().getAccess_token());
                return headers;

            }


        };


        int socketTimeout = 20000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sendContactRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(sendContactRequest);

    }
}
