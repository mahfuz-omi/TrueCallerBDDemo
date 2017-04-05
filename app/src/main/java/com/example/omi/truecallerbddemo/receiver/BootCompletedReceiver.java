package com.example.omi.truecallerbddemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.omi.truecallerbddemo.MainActivity;

/**
 * Created by omi on 12/28/2016.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {

        Intent activityIntent = new Intent(context,MainActivity.class);
        context.startActivity(activityIntent);

    }
}
