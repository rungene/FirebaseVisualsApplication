package com.rungenes.firebasevisualsapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        String notificationBody = "";
        String notificationTitle ="";
        String notificationData ="";



        try {
            notificationBody = remoteMessage.getNotification().getBody();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationData = remoteMessage.getData().toString();

            

        }catch (NullPointerException e){

            Log.e(TAG, "onMessageReceived:NullPointerException: "+e.getMessage());
        }
        Log.d(TAG, "onMessageReceived: data: "+notificationData);
        Log.d(TAG, "onMessageReceived: Body: "+notificationBody);
        Log.d(TAG, "onMessageReceived: Title :"+notificationTitle);

    }


}
