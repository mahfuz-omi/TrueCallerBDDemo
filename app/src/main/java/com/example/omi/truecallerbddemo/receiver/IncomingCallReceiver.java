package com.example.omi.truecallerbddemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.omi.truecallerbddemo.MainActivity;
import com.example.omi.truecallerbddemo.R;
import com.example.omi.truecallerbddemo.application.TrueCallerBDApplication;
import com.example.omi.truecallerbddemo.constant.WebServiceURL;
import com.example.omi.truecallerbddemo.service.ContactsSendService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by omi on 12/14/2016.
 */

public class IncomingCallReceiver extends BroadcastReceiver {
    private WindowManager windowManager;
    private Context context;


    @Override
    public void onReceive(final Context context, Intent intent)
    {
        this.context = context;
        System.out.println(intent.getAction());
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getCallState())
        {

            case TelephonyManager.CALL_STATE_RINGING:
                String phoneNr= intent.getStringExtra("incoming_number");
                System.out.println("phone no: "+phoneNr);
                //Toast.makeText(context, phoneNr,Toast.LENGTH_LONG).show();
                try {
                    this.doJob(phoneNr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    public void doJob(String phoneNumber) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("number",phoneNumber);

        JsonArrayRequest getContactRequest = new JsonArrayRequest(Request.Method.POST, WebServiceURL.GET_CONTACT_URL,jsonObject.toString(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
                System.out.println("teamMovedResponse string:  "+response);
                if(response.length() > 0 )
                {
                    ArrayList<String> names = new ArrayList<>();
                    try
                    {
                        for(int i=0;i<response.length();i++)
                        {
                            JSONObject nameObject = response.getJSONObject(i);
                            String name = nameObject.getString("name");
                            names.add(name);
                        }
                        showDialog(names);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
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
                headers.put("access-token",((TrueCallerBDApplication)context.getApplicationContext()).getUserCredential().getAccess_token());
                return headers;

            }
        };

        int socketTimeout = 20000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        getContactRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(getContactRequest);


    }

    public void showDialog(ArrayList<String> names)
    {

        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.CENTER;
        params.x = 0;
        params.y = 100;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View trueCallerView = inflater.inflate(R.layout.show_name_view, null);
        TextView nameView = (TextView) trueCallerView.findViewById(R.id.name);
        StringBuffer sb = new StringBuffer();
        for(String name:names)
        {
            sb.append(name+",");
        }
        sb.deleteCharAt(sb.length()-1);
        nameView.setText(sb.toString());
        ImageButton cross = (ImageButton)trueCallerView.findViewById(R.id.delete);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(trueCallerView);
            }
        });
        windowManager.addView(trueCallerView, params);


    }






}

