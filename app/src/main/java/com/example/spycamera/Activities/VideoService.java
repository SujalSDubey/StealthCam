package com.example.spycamera.Activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.spycamera.MainActivity;
import com.example.spycamera.R;
import com.example.spycamera.Utilities.Helper;
import com.example.spycamera.Utilities.SPHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VideoService extends Service {
    public static Context context;
    private static final String TAG = "RecorderService";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private static Camera mServiceCamera;
    private boolean mRecordingStatus;
    private MediaRecorder mMediaRecorder;
    int cameraPos = 0;
    WindowManager wm;

    private File videoFolder;

    @Override
    public void onCreate() {
        mRecordingStatus = false;
        context = this;
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        context = this;
        initData();
        showNotification();
        record();
        final Handler handler = new Handler(Looper.getMainLooper());
        String videoDuration = SPHelper.getVideoDuration(context);
        if (bundle != null) {
            String bundleVideoDuration = bundle.getString("videoDuration");
            if (bundleVideoDuration != null) videoDuration = bundleVideoDuration;
        } else {
            videoDuration = SPHelper.getVideoDuration(context);
        }

        handler.postDelayed(this::stopSelf, Integer.parseInt(videoDuration) * 60000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    void record() {
        mServiceCamera = Camera.open(cameraPos);
        mServiceCamera.setErrorCallback(errorCallback);
        mSurfaceView = new SurfaceView(context);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                if (!mRecordingStatus)
                    startRecording();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1, //Must be at least 1x1
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        wm.addView(mSurfaceView, params);

    }

    CameraErrorCallback errorCallback = new CameraErrorCallback();

    @SuppressWarnings("deprecation")
    public class CameraErrorCallback implements android.hardware.Camera.ErrorCallback {
        @Override
        public void onError(int error, android.hardware.Camera camera) {
            Toast.makeText(context, "Camera already in use or not available", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopRecording();
            mRecordingStatus = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (MainActivity.booleanValueListener != null) {
            MainActivity.booleanValueListener.setValue(false);
        }
    }

    public boolean startRecording() {
        try {
            Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();
            Camera.Parameters params = mServiceCamera.getParameters();
            mServiceCamera.setParameters(params);
            Camera.Parameters p = mServiceCamera.getParameters();

            final List<Size> listSize = p.getSupportedPreviewSizes();
            Size mPreviewSize = listSize.get(2);
            Log.v(TAG, "use: width = " + mPreviewSize.width
                    + " height = " + mPreviewSize.height);
            p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mServiceCamera.setParameters(p);

            try {
                mServiceCamera.setPreviewDisplay(mSurfaceHolder);
                mServiceCamera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                stopSelf();
            }

            mServiceCamera.unlock();
            String mTimeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String mImageName = "snap_" + mTimeStamp + ".mp4";

            if (videoFolder != null) {
                ContextWrapper wrapper = new ContextWrapper(context);
                videoFolder = wrapper.getDir("Videos", MODE_PRIVATE);
            }
            videoFolder = new File(videoFolder, "snap_" + mImageName + ".mp4");
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setOutputFile(videoFolder);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoEncodingBitRate(3000000);
//            mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecordingStatus = true;

            return true;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            stopSelf();
            e.printStackTrace();
            return false;
        }
    }

    public void stopRecording() {
        try {
            try {
                wm.removeView(mSurfaceView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();
            try {
                mServiceCamera.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder.stop();
            mMediaRecorder.reset();

            mServiceCamera.stopPreview();
            mMediaRecorder.release();

            mServiceCamera.release();
            mServiceCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MainActivity.isDataChangeListener.setValue(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initData() {
        ContextWrapper wrapper = new ContextWrapper(context);
        videoFolder = wrapper.getDir("Videos", MODE_PRIVATE);
        if (SPHelper.getVideoChoice(context).equals("FRONT")) {
            cameraPos = 1;
        } else {
            cameraPos = 0;
        }
    }

    public void showNotification() {
        StrictMode.VmPolicy.Builder sb = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(sb.build());
        sb.detectFileUriExposure();
        PendingIntent viewDownloadPI;
        Intent intentViewDocs = new Intent(context, MainActivity.class);
        intentViewDocs.putExtra("CLICKED", "STOP");
        intentViewDocs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        viewDownloadPI = PendingIntent.getActivity(context, 1,
                intentViewDocs, flags);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(context, "Notification");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Notification");

        builder.setContentIntent(viewDownloadPI)
                .setSmallIcon(R.drawable.logo)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Spy Camera")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Click To Stop"))
                .setAutoCancel(false)
                .setOngoing(true);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        startForeground(3, notification);
    }

    public void createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            String description = "channeldescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }
}
