package com.tnmlicitacoes.app.fcm;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tnmlicitacoes.app.BuildConfig;
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

        Context appContext = getApplicationContext();

        String from = message.getFrom();
        Map data = message.getData();

        if (!SettingsUtils.isNotificationsEnabled(appContext)) {
            if (BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Notifications disabled, aborting...");
            return;
        }

        if (from.startsWith(Topics.TOPICS)) {

            LOG_DEBUG(TAG, "Message received from a topic");

            if (data == null) {
                LOG_DEBUG(TAG, "Bundle is null, aborting...");
                return;
            }

            Object device = data.get(DEVICE);
            if (device == null) {
                return;
            }

            String deviceType = device.toString();
            if (deviceType != null) {
                try {
                    int type = Integer.parseInt(deviceType);
                    if (!NotificationUtils.checkForDevice(type)) {
                        LOG_DEBUG(TAG, "Message to another operation system, aborting...");
                        return;
                    }
                } catch (NumberFormatException e) {
                    return;
                }
            }

            LOG_DEBUG(TAG, "Message to Android system, let's process now!");

            if (from.contains(Topics.DEV) && BuildConfig.DEBUG) {
                handleDevTopic(appContext, data);
                return;
            }

            if (from.contains(Topics.GLOBAL)) {                    
                handleGlobalTopic(appContext, data);
            } else if (from.contains(Topics.TIPS) && SettingsUtils.isTipsNotificationsEnabled(appContext)) {
                handleTipsTopic(appContext, data);
            } else if (from.contains(Topics.NEWS) && SettingsUtils.isNewsNotificationsEnabled(appContext)) {
                handleNewsTopic(appContext, data);
            } else if (from.contains(Topics.SALES)) {
                handleSalesTopic(appContext, data);
            } else if (from.contains(Topics.GENERAL)) {
                handleGeneralTopic(appContext, data);
            } else if (from.contains(Topics.UPDATES)) {
                handleUpdatesTopic(appContext, data);
            } else {
                // Message came from a category topic
                try {
                    if (SettingsUtils.isBiddingsNotificationsEnabled(appContext)) {
                        handleNewBiddingsTopic(appContext, data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {
            handleMulticastNotification(appContext, data);
        }
    }

    private void handleDevTopic(Context context, Map data) {
        LOG_DEBUG(TAG, "Handling dev topic...");

        try {
            handleNewBiddingsTopic(context, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int generateRandomNotificationID() {
        return new Random().nextInt(9999 - 1000) + 1000;
    }
}
