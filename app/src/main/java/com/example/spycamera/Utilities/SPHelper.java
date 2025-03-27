package com.example.spycamera.Utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SPHelper {

    private static SharedPreferences sharedpreferences;

    public static void addPhotoSettings(Context context, String cameraChoice, String photoChoice, String photoChoiceInput, String photoBuffer) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("cameraChoice", cameraChoice);
        editor.putString("photoChoice", photoChoice);
        editor.putString("photoChoiceInput", photoChoiceInput);
        editor.putString("photoBuffer", photoBuffer);
        editor.apply();
    }

    public static void addVideoSettings(Context context, String videoChoice, String videoDuration) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("videoChoice", videoChoice);
        editor.putString("videoDuration", videoDuration);
        editor.apply();
    }

    public static String getCameraChoice(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getString("cameraChoice", "");
    }

    public static String getPhotoChoice(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getString("photoChoice", "");
    }

    public static String getPhotoChoiceInput(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getString("photoChoiceInput", "");
    }

    public static String getVideoChoice(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getString("videoChoice", "");
    }

    public static String getVideoDuration(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getString("videoDuration", "");
    }

    public static String getPhotoBuffer(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getString("photoBuffer", "");
    }

    public static void setLastMessageProcessedDateTime(Context context, long dateTime) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("lastMessageProcessedDateTime", dateTime);
        editor.apply();
    }

    public static long getLastMessageProcessedDateTime(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getLong("lastMessageProcessedDateTime", -1);
    }

    public static void setLastAppRunTime(Context context, long dateTime) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("lastAppRunTime", dateTime);
        editor.apply();
    }

    public static long getLastAppRunTime(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getLong("lastAppRunTime", -1);
    }

    public static void setFirstAppRunTime(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        long firstAppRunTime = getFirstAppRunTime(context);
        if (firstAppRunTime <= 0) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putLong("firstAppRunTime", System.currentTimeMillis());
            editor.apply();
        }
    }


    public static long getFirstAppRunTime(Context context) {
        sharedpreferences = context.getSharedPreferences("SPY_CAMERA", MODE_PRIVATE);
        return sharedpreferences.getLong("firstAppRunTime", -1);
    }
}
