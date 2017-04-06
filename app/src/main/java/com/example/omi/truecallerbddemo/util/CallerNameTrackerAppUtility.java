package com.example.omi.truecallerbddemo.util;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by omi on 4/5/2017.
 */

public class CallerNameTrackerAppUtility {

    public static void showSnackbar(Activity activity, int rootViewId, String message)
    {
        View rootView = activity.findViewById(rootViewId);
        Snackbar snackbar = Snackbar
                .make(rootView, message, Snackbar.LENGTH_LONG);

        snackbar.show();

    }
}
