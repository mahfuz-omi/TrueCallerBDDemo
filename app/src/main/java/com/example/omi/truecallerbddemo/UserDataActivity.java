package com.example.omi.truecallerbddemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import com.example.omi.truecallerbddemo.application.TrueCallerBDApplication;
import com.example.omi.truecallerbddemo.constant.WebServiceURL;
import com.example.omi.truecallerbddemo.model.LoginRequest;
import com.example.omi.truecallerbddemo.model.UserCredential;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserDataActivity extends AppCompatActivity {
    ProgressDialog pDialog;

    EditText fullNameEditText;
    TextView errorTextView;

    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        this.phoneNumber = getIntent().getStringExtra("phoneNo");

        this.fullNameEditText = (EditText)findViewById(R.id.fullNameEditText);
        this.errorTextView = (TextView)findViewById(R.id.errorText);
    }


    public void requestLogin(String fullName)
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setFull_name(fullName);
        loginRequest.setPhone_no(phoneNumber);

        String loginRequestJson = new Gson().toJson(loginRequest);
        System.out.println("optionReqiestJson: "+loginRequestJson);



        pDialog = new ProgressDialog(UserDataActivity.this);
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

                        Intent intent = new Intent(UserDataActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(UserDataActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                /////
                if (pDialog.isShowing())
                    pDialog.dismiss();

                Toast.makeText(UserDataActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        }
        )
        {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> responseHeaders = response.headers;
                for(String key:responseHeaders.keySet())
                {
                    if("access-token".equalsIgnoreCase(key))
                    {
                        UserCredential userCredential = new UserCredential();
                        userCredential.setAccess_token(responseHeaders.get(key));
                        ((TrueCallerBDApplication)getApplication()).setUserCredential(userCredential);
                    }
                    System.out.println(key+":"+responseHeaders.get(key));

                }
                System.out.println("omi token"+responseHeaders.get("access-token"));
                return super.parseNetworkResponse(response);
            }


        };


        int socketTimeout = 20000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        acknowledgeRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(UserDataActivity.this);
        requestQueue.add(acknowledgeRequest);
    }


    public void login(View v)
    {
        String fullName = fullNameEditText.getText().toString();
        if(fullName.isEmpty())
        {
            errorTextView.setText("full name can't be empty");
            return;

        }

        this.requestLogin(fullName);



    }
}
