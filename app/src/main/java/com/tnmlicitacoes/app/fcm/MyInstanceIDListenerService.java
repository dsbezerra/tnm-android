package com.tnmlicitacoes.app.fcm;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        LOG_DEBUG(TAG, "Refreshed token: " + refreshedToken);

        // Start the service that sends the token to our server
        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra("refreshedToken", refreshedToken);
        startService(intent);
    }
}
