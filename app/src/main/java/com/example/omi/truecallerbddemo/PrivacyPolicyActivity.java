package com.example.omi.truecallerbddemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.omi.truecallerbddemo.application.TrueCallerBDApplication;
import com.example.omi.truecallerbddemo.constant.WebServiceURL;
import com.example.omi.truecallerbddemo.model.UserCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by omi on 12/22/2016.
 */

public class PrivacyPolicyActivity extends AppCompatActivity {
    ProgressDialog pDialog;
    WebView policy_text;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);

        this.policy_text = (WebView) findViewById(R.id.policy_text);
        policy_text.loadUrl(WebServiceURL.POLICY_URL);


    }
}
