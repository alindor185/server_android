package com.example.youtubepart1;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ToastManager {
    private static Toast toast;
    public static void showToast(String message, Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
