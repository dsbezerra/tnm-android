package com.tnmlicitacoes.app.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.interfaces.OnSessionChangedListener;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.NotificationUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import org.json.JSONException;

import java.util.Map;
import java.util.Random;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;
import static com.tnmlicitacoes.app.utils.NotificationUtils.DEVICE;
import static com.tnmlicitacoes.app.utils.NotificationUtils.Topics;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleGeneralTopic;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleGlobalTopic;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleMulticastNotification;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleNewBiddingsTopic;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleNewsTopic;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleSalesTopic;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleTipsTopic;
import static com.tnmlicitacoes.app.utils.NotificationUtils.handleUpdatesTopic;

public class MyFcmListenerService extends FirebaseMessagingService {
    private static final String TAG = "MyFcmListenerService";

    @Override
    public void onMessageReceived(RemoteMessage message) {

        String from = message.getFrom();
        Map data = message.getData();

        if(!SettingsUtils.isNotificationsEnabled(getApplicationContext())) {
            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Notifications disabled, aborting...");
            return;
        }

       if(from.startsWith(Topics.TOPICS)) {

           if(BuildConfig.DEBUG)
               LOG_DEBUG(TAG, "Message received from a topic");
           if(data == null) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Bundle is null, aborting...");
               return;
           }

           String deviceType = data.get(DEVICE).toString();
           if(deviceType != null) {
               try {
                   int type = Integer.parseInt(deviceType);
                   if(!NotificationUtils.checkForDevice(type)) {
                       if(BuildConfig.DEBUG)
                           LOG_DEBUG(TAG, "Message to another operation system, aborting...");
                       return;
                   }
               } catch (NumberFormatException e) {
                   return;
               }
           }

           if(BuildConfig.DEBUG)
               LOG_DEBUG(TAG, "Message to Android system, let's process now!");

           if(from.contains(Topics.GLOBAL)) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Handle global topic");
               handleGlobalTopic(getApplicationContext(), data);
           }
           else if (from.contains(Topics.TIPS) && SettingsUtils.isTipsNotificationsEnabled(getApplicationContext())) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Handle tips topic");
               handleTipsTopic(getApplicationContext(), data);
           }
           else if (from.contains(Topics.NEWS) && SettingsUtils.isNewsNotificationsEnabled(getApplicationContext())) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Handle news topic");
               handleNewsTopic(getApplicationContext(), data);
           }
           else if (from.contains(Topics.SALES)) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Handle sales topic");
               handleSalesTopic(getApplicationContext(), data);
           }
           else if (from.contains(Topics.GENERAL)) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Handle general topic");
               handleGeneralTopic(getApplicationContext(), data);
           }
           else if (from.contains(Topics.DEV)) {
               if(BuildConfig.DEBUG)
                   LOG_DEBUG(TAG, "Handle dev topic");
           }
           else if (from.contains(Topics.UPDATES)) {
               handleUpdatesTopic(getApplicationContext(), data);
           }
           else {

               // Message came from a category topic
               try {
                   if(SettingsUtils.isBiddingsNotificationsEnabled(getApplicationContext())) {
                       handleNewBiddingsTopic(getApplicationContext(), data);
                   }
               } catch (JSONException e) {
                   e.printStackTrace();
               }
           }

       } else {

           if(BuildConfig.DEBUG) {
               LOG_DEBUG(TAG, "Message");
           }

           handleMulticastNotification(getApplicationContext(), data);
       }
    }

    private int generateRandomNotificationID() {
        return new Random().nextInt(9999 - 1000) + 1000;
    }
}
