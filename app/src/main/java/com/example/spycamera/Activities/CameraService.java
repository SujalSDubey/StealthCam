package com.example.spycamera.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.example.spycamera.Utilities.SPHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service {
    public static Context context;
    private TimerTask timerTask;
    private Timer timer;
    Handler hand = new Handler();
    String photoChoice;
    int bufferTime = 0;
    int photoCount = 0;
    int cameraPos = 0;
    Camera camera = null;
    private Camera.Size previewSize;
    boolean isPhotoClicked = false;
    boolean isFileSaved = true;
    WindowManager wm;

    private File imageFolder;
    private SurfaceView preview;
    private boolean isPurgeTimer = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        setInitialData(bundle);
        if (photoChoice == null || photoChoice.equals("0")) {
            startTimerForPhotoDuration();
        } else {
            startTimerForPhotoCount();
        }
        showNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private static void showMessage(String message) {
        Log.i("Camera", message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Uri saveImageToInternalStorage(Bitmap bitmap) {
        String mTimeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String mImageName = "snap_" + mTimeStamp + ".jpg";
        if (imageFolder != null) {
            ContextWrapper wrapper = new ContextWrapper(context);
            imageFolder = wrapper.getDir("Images", MODE_PRIVATE);
        }
        imageFolder = new File(imageFolder, "snap_" + mImageName + ".jpg");
        try {
            OutputStream stream = null;
            stream = new FileOutputStream(imageFolder);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.parse(imageFolder.getAbsolutePath());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            timer.cancel();
//            timerTask.cancel();
//            timer.purge();
            isPurgeTimer = true;
            stopCamera();
            showMessage("Took picture Destroy");
            MainActivity.booleanValueListener.setValue(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setInitialData(Bundle bundle) {
        context = this;
        ContextWrapper wrapper = new ContextWrapper(context);
        imageFolder = wrapper.getDir("Images", MODE_PRIVATE);
        if (bundle != null) {
            String photoChoiceBundle = bundle.getString("photoChoice");
            String photoBuffer = bundle.getString("photoBuffer");
            String photoChoiceInput = bundle.getString("photoChoiceInput");

            photoChoice = photoChoiceBundle == null ? SPHelper.getPhotoChoice(context) : photoChoiceBundle;
            bufferTime = Integer.parseInt(photoBuffer == null ?
                    SPHelper.getPhotoBuffer(context) : photoBuffer);
            photoCount = Integer.parseInt(photoChoiceInput == null ?
                    SPHelper.getPhotoChoiceInput(context) + 1 : photoChoiceInput);

        } else {
            photoChoice = SPHelper.getPhotoChoice(context);
            bufferTime = Integer.parseInt(SPHelper.getPhotoBuffer(context));
            photoCount = Integer.parseInt(SPHelper.getPhotoChoiceInput(context)) + 1;
        }
        if (SPHelper.getCameraChoice(context).equals("FRONT")) cameraPos = 1;
        else cameraPos = 0;
        setSurfaceView();
    }

    void startTimerForPhotoCount() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                hand.post(new TimerTask() {
                    @Override
                    public void run() {
                        if (photoCount != 0) {
                            isPhotoClicked = true;
                            if (isFileSaved) {
                                takePhoto();
                            }
                            photoCount--;
                        } else {
                            stopSelf();
                        }
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, bufferTime * 1000);
    }

    void startTimerForPhotoDuration() {
        int minutesToAdd = Integer.parseInt(SPHelper.getPhotoChoiceInput(context));
        Calendar now = Calendar.getInstance();
        Calendar tmp = (Calendar) now.clone();
        tmp.add(Calendar.MINUTE, minutesToAdd);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                hand.post(new TimerTask() {
                    @Override
                    public void run() {
                        if (tmp.after(Calendar.getInstance())) {
                            isPhotoClicked = true;
                            if (isFileSaved) {
                                takePhoto();
                            }
                        } else {
                            stopSelf();
                        }
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, bufferTime * 1000);
    }

    public void showNotification() {
        StrictMode.VmPolicy.Builder sb = null;
        sb = new StrictMode.VmPolicy.Builder();
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

    private void setSurfaceView() {
        preview = new SurfaceView(context);
        SurfaceHolder holder = preview.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                showMessage("Surface created");


                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraPos, cameraInfo);
                try {
                    camera = Camera.open(cameraPos);
                    camera.setErrorCallback(errorCallback);
                    showMessage("Opened camera");

                    try {
                        camera.setPreviewDisplay(holder);
                        Camera.Parameters parameters = camera.getParameters();
                        previewSize = parameters.getPreviewSize();
                    } catch (Exception e) {
                        stopSelf();
                    }

//                    camera.startPreview();
//                    showMessage("Started preview");
//                    camera.takePicture(null, null, (data, camera1) -> {
//                        showMessage("Took picture");
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                        saveImageToInternalStorage(bmp);
//                        camera1.release();
//                    });


                } catch (Exception e) {
                    if (camera != null)
                        camera.release();
                    stopSelf();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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

        wm.addView(preview, params);
    }

    private void takePhoto() {

        try {
            camera.startPreview();
            showMessage("Started preview");
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public synchronized void onPreviewFrame(byte[] data, Camera arg1) {
                    if (isPhotoClicked) {
                        isFileSaved = false;
                        showMessage("Took picture");
                        int[] rgb = new int[previewSize.width * previewSize.height];
                        decodeYUV420SP(rgb, data, previewSize.width, previewSize.height);
                        Bitmap memoryImage = Bitmap.createBitmap(rgb, previewSize.width,
                                previewSize.height, Bitmap.Config.ARGB_8888);
                        Uri uri = saveImageToInternalStorage(memoryImage);
                        if (uri != null) {
                            MainActivity.isDataChangeListener.setValue(true);
                        }
                        isFileSaved = true;
                        isPhotoClicked = false;
                        if (isPurgeTimer) {
                            timer.cancel();
                            timer.purge();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
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

    public void stopCamera() {
        try {
            try {
                wm.removeView(preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
