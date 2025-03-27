package com.example.spycamera.Utilities;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.example.spycamera.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.net.URLConnection;
import java.util.List;

public class Helper {

    private static ProgressDialog progressDialog;
    private static AlertDialog dialog;

    public static Uri getUri(Context context, File file) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                return FileProvider.getUriForFile(context,
                        context.getApplicationContext()
                                .getPackageName() + ".provider",
                        file);
            } else {
                return Uri.fromFile(file);
            }
        } catch (Exception exception) {
            return null;
        }
    }

    public static void setSnackBar(View root, String snackTitle) {
        Snackbar snackbar = Snackbar.make(root, snackTitle, Snackbar.LENGTH_SHORT);
        snackbar.setDuration(500);
        snackbar.show();
    }

    public static void startActivity(Context context, Class<?> activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }

    public static void requestOverlayDisplayPermission(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.getBaseContext());
        builder.setCancelable(true);
        builder.setTitle("Screen Overlay Permission Needed");
        builder.setMessage("Enable 'Display over other apps' from System Settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
            activity.startActivityForResult(intent, RESULT_OK);
        });
        dialog = builder.create();
        dialog.show();
    }

    public static boolean checkOverlayDisplayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static void showProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context, R.style.MyTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public static void shareFile(Context context, File file) {
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                uri = FileProvider.getUriForFile(context,
                        context.getApplicationContext()
                                .getPackageName() + ".provider",
                        file);
            } else {
                uri = Uri.fromFile(file);
            }
            String fileType = URLConnection.guessContentTypeFromName(file.getName());
            new ShareCompat.IntentBuilder(context)
                    .setType(fileType)
                    .setType("message/rfc822")
                    .setStream(uri)
                    .setChooserTitle("Share")
                    .startChooser();

        } catch (Exception exception) {
            exception.printStackTrace();
            Toast.makeText(context, "No application found to open the file", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isAppOnForeground(Context context) {
        context = context.getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
