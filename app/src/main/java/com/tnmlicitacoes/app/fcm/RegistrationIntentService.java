package com.tnmlicitacoes.app.fcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;
import static com.tnmlicitacoes.app.utils.NotificationUtils.Topics;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    private static List<String> mTopics = new ArrayList<String>() {{
        add(Topics.GLOBAL);
        add(Topics.TIPS);
        add(Topics.NEWS);
        add(Topics.SALES);
        add(Topics.GENERAL);
        add(Topics.UPDATES);
    }};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Fill topics with segments ids
        populateTopicsArray();

        String token = null;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            token = extras.getString("refreshedToken");
            sendRegistrationToServer(token);
            SettingsUtils.putBoolean(getApplicationContext(),
                    SettingsUtils.PREF_NEED_TO_UPDATE_TOPICS,
                    false);
        }
    }

    /**
     * Persist registration to TáNaMão servers
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        Context context = getApplicationContext();
        //mInstallation.deviceType = "android";
        //mInstallation.deviceToken = token;
        //mInstallation.email = SettingsUtils.getUserDefaultEmail(context);
        //mInstallation.deviceId = SettingsUtils.getDeviceId(context);
        //mInstallation.subscription = SettingsUtils.getBillingState(context);

        // Subscribe to topic channels
        subscribeTopics();
        // Unsubscribe to topics channels
        unsubscribeTopics();

        // TODO(diego): Use OkHttp3 to send the token to our server
    }


    private void subscribeTopics() {
        FirebaseMessaging pubSub = FirebaseMessaging.getInstance();
        for (String topic : mTopics) {
            pubSub.subscribeToTopic(topic);
            LOG_DEBUG(TAG, "Subscribed to topic: " + topic);
        }

        // We use the phone as topic just in case we need to send individual notifications
        String phone = SettingsUtils.getUserPhoneNumber(this);
        if(!TextUtils.isEmpty(phone) && phone.startsWith("55")) {
            pubSub.subscribeToTopic("phone-" + phone);
        }
    }

    private void unsubscribeTopics() {
        FirebaseMessaging pubSub = FirebaseMessaging.getInstance();

        String unsubPref = SettingsUtils.getUnsubscribeTopics(getApplicationContext());
        if(TextUtils.isEmpty(unsubPref)) {
            return;
        }

        String[] unsubTopics = unsubPref.split(";");
        if(unsubTopics.length == 0) {
            return;
        }

        for (String topic : unsubTopics) {
            pubSub.unsubscribeFromTopic(topic);
            LOG_DEBUG(TAG, "Unsubscribed from topic: " + topic);
        }

        SettingsUtils.putString(getApplicationContext(), SettingsUtils.PREF_TMP_UNSUBSCRIBE_TOPICS, "");
    }

    private void populateTopicsArray() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PickedSegment> segments = realm.where(PickedSegment.class).findAll();
        LOG_DEBUG(TAG, "Adding segments to topics array...");
        for (int i = 0; i < segments.size(); i++) {
            PickedSegment segment = segments.get(i);
            mTopics.add(segment.getId());
        }
        LOG_DEBUG(TAG, "Added " + segments.size() + " to topics array!");
    }
}
