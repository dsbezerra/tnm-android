package com.tnmlicitacoes.app.fcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.UpdateSupplierMutation;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.type.SupplierInput;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;
import static com.tnmlicitacoes.app.utils.NotificationUtils.Topics;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    private List<String> mTopics = new ArrayList<>();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Fill topics with segments ids
        populateTopicsArray();

        // Unsubscribe to topics channels
        unsubscribeFromTopics();

        // Subscribe to topic channels
        subscribeTopics();

        SettingsUtils.putBoolean(getApplicationContext(),
                SettingsUtils.PREF_NEED_TO_UPDATE_TOPICS,
                false);

        if (intent != null && intent.getExtras() != null && !intent.getExtras().isEmpty()) {
            Bundle extras = intent.getExtras();
            if (extras != null && !extras.isEmpty()) {
                String refreshedToken = extras.getString("refreshedToken");
                if (!TextUtils.isEmpty(refreshedToken)) {
                    sendRegistrationToServer(refreshedToken);
                }
            }
        }
    }

    /**
     * Persist registration to TáNaMão servers
     */
    private void sendRegistrationToServer(String token) {
        UpdateSupplierMutation mutation = UpdateSupplierMutation.builder()
                .supplier(
                        SupplierInput.builder()
                                .deviceId(token)
                                .build())
                .build();

        TnmApplication application = (TnmApplication) getApplication();

        ApolloClient apolloClient = application.getApolloClient();
        if (apolloClient != null) {
            apolloClient.mutate(mutation)
                    .enqueue(new ApolloCall.Callback<UpdateSupplierMutation.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<UpdateSupplierMutation.Data> response) {
                            if (!response.hasErrors()) {
                                LOG_DEBUG(TAG, "Token sent successfully to server!");
                            } else {
                                LOG_DEBUG(TAG, "Something wrong happened!");
                            }
                        }

                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            LOG_DEBUG(TAG, "Something wrong happened!");
                        }
                    });
        }

    }


    private void subscribeTopics() {
        FirebaseMessaging pubSub = FirebaseMessaging.getInstance();
        for (String topic : mTopics) {
            pubSub.subscribeToTopic(topic);
            LOG_DEBUG(TAG, "Subscribed to topic: " + topic);
        }

        // We use the phone as topic just in case we need to send individual notifications
        String phone = SettingsUtils.getUserPhoneNumber(this);
        if (!TextUtils.isEmpty(phone) && phone.startsWith("55")) {
            pubSub.subscribeToTopic("phone-" + phone);
        }
    }

    private void unsubscribeFromTopics() {
        // TODO(diego):
        // Unsubscribe from previously subscribed topics
    }

    private void populateTopicsArray() {
        // Get default topics if this is the first time we add them
        if (!SettingsUtils.isAddedToDefaultTopics(this)) {
            mTopics = getDefaultTopics();
            SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_ADDED_TO_DEFAULT_TOPICS, true);
        }

        Realm realm = Realm.getDefaultInstance();
        RealmResults<PickedSegment> segments = realm.where(PickedSegment.class).findAll();
        LOG_DEBUG(TAG, "Adding segments to topics array...");
        for (int i = 0; i < segments.size(); i++) {
            PickedSegment segment = segments.get(i);
            mTopics.add(segment.getId());
        }
        LOG_DEBUG(TAG, "Added " + segments.size() + " to topics array!");


        if (BuildConfig.DEBUG) {
            mTopics.add(Topics.DEV);
        }
    }

    private List<String> getDefaultTopics() {
        return new ArrayList<>(Arrays.asList(
                Topics.GLOBAL,
                Topics.TIPS,
                Topics.NEWS,
                Topics.SALES,
                Topics.GENERAL,
                Topics.UPDATES
        ));
    }
}
