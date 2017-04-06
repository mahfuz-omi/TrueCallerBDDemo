package com.example.omi.truecallerbddemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.example.omi.truecallerbddemo.application.TrueCallerBDApplication;
import com.example.omi.truecallerbddemo.constant.WebServiceURL;
import com.example.omi.truecallerbddemo.model.CheckPhoneNoRequest;
import com.example.omi.truecallerbddemo.model.LoginRequest;
import com.example.omi.truecallerbddemo.model.UserCredential;
import com.example.omi.truecallerbddemo.util.CallerNameTrackerAppUtility;
import com.google.gson.Gson;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.fabric.sdk.android.Fabric;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    ProgressDialog pDialog;
    String phoneNo;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "WpCGhI1LEjPpFaSi4oIAqeyOI";
    private static final String TWITTER_SECRET = "YlzmtlZgOqFmLCx19NYQVlnx858O0kLfXOUFV5ndo1ggdolA8L";


    public void requestLogin()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setFull_name("");
        loginRequest.setPhone_no(phoneNo);

        String loginRequestJson = new Gson().toJson(loginRequest);
        System.out.println("optionReqiestJson: "+loginRequestJson);



        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();


        JsonObjectRequest acknowledgeRequest = new JsonObjectRequest(Request.Method.POST, WebServiceURL.LOGIN_URL,loginRequestJson, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {

                //////
                if (pDialog.isShowing())
                    pDialog.dismiss();
                System.out.println(response);
                if(response.has("success"))
                {
                    try {
                        String phone_no = response.getString("phone_no");
                        String full_name = response.getString("full_name");
                        ((TrueCallerBDApplication)getApplication()).getUserCredential().setPhone_no(phone_no);
                        ((TrueCallerBDApplication)getApplication()).getUserCredential().setFull_name(full_name);
                        ((TrueCallerBDApplication)getApplication()).logginUser(((TrueCallerBDApplication)getApplication()).getUserCredential());

                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                /////
                if (pDialog.isShowing())
                    pDialog.dismiss();

                CallerNameTrackerAppUtility.showSnackbar(LoginActivity.this,R.id.loginRoot,error.toString());

            }
        }
        )
        {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> responseHeaders = response.headers;
                for(String key:responseHeaders.keySet())
                {
                    UserCredential userCredential = new UserCredential();
                    userCredential.setAccess_token(responseHeaders.get(key));
                    ((TrueCallerBDApplication)getApplication()).setUserCredential(userCredential);

                }
                System.out.println("omi token"+responseHeaders.get("access-token"));
                return super.parseNetworkResponse(response);
            }
        };


        int socketTimeout = 20000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        acknowledgeRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(acknowledgeRequest);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
        setContentView(R.layout.activity_login);
        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, final String phoneNumber)
            {
                // TODO: associate the session userID with your user model
//                Toast.makeText(getApplicationContext(), "Authentication successful for "
//                        + phoneNumber, Toast.LENGTH_LONG).show();
                phoneNo = phoneNumber;
                //Toast.makeText(LoginActivity.this, phoneNo+"authentication success", Toast.LENGTH_SHORT).show();
                CallerNameTrackerAppUtility.showSnackbar(LoginActivity.this,R.id.loginRoot,phoneNo+" Authentication success");


                CheckPhoneNoRequest checkPhoneNoRequestObject = new CheckPhoneNoRequest();
                checkPhoneNoRequestObject.setPhone_no(phoneNumber);

                String checkPhoneNoRequestJson = new Gson().toJson(checkPhoneNoRequestObject);
                System.out.println("optionReqiestJson: "+checkPhoneNoRequestJson);

                pDialog = new ProgressDialog(LoginActivity.this);
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();


                JsonObjectRequest acknowledgeRequest = new JsonObjectRequest(Request.Method.POST, WebServiceURL.CHECK_PHONE_NO_URL,checkPhoneNoRequestJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {

                        //////
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                        System.out.println(response);
                        try {
                            String status = response.getString("result");
                            if("new".equalsIgnoreCase(status))
                            {
                                // new account
                                Intent intent = new Intent(LoginActivity.this,UserDataActivity.class);
                                intent.putExtra("phoneNo",phoneNumber);
                                startActivity(intent);
                                finish();

                            }
                            else if("old".equalsIgnoreCase(status))
                            {

                                // old account
                                requestLogin();

                            }
                            else
                            {
                                CallerNameTrackerAppUtility.showSnackbar(LoginActivity.this,R.id.loginRoot,"Network Error.Please try again later.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        /////
                        if (pDialog.isShowing())
                            pDialog.dismiss();

                        CallerNameTrackerAppUtility.showSnackbar(LoginActivity.this,R.id.loginRoot,"Network Error.Please try again later.");

                    }
                }
                )
                {


                };


                int socketTimeout = 20000;//5 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                acknowledgeRequest.setRetryPolicy(policy);

                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                requestQueue.add(acknowledgeRequest);


            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
                CallerNameTrackerAppUtility.showSnackbar(LoginActivity.this,R.id.loginRoot,phoneNo+" Authentication Failed");
            }
        });


    }
}
