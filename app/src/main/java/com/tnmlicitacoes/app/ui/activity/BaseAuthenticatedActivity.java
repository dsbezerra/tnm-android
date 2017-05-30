package com.tnmlicitacoes.app.ui.activity;

import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.RefreshSupplierTokenMutation;
import com.tnmlicitacoes.app.TNMApplication;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.Date;

import javax.annotation.Nonnull;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    private static final String TAG = "BaseAuthActivity";

    @Override
    protected void onStart() {
        super.onStart();
        if (TNMApplication.shouldRefreshToken(this)) {
            TNMApplication.IsRefreshingToken = true;
            refreshToken();
        }
    }

    public void refreshToken() {
        // Initialize apollo with refresh token as the authorization header
        TNMApplication application = (TNMApplication) getApplication();
        application.initApolloClient(SettingsUtils.getRefreshToken(this));

        RefreshSupplierTokenMutation mutation = new RefreshSupplierTokenMutation();
        ApolloCall<RefreshSupplierTokenMutation.Data> call = application.getApolloClient()
                .newCall(mutation)
                .cacheControl(CacheControl.NETWORK_ONLY);

        call.enqueue(dataCallback);
    }

    private ApolloCall.Callback<RefreshSupplierTokenMutation.Data> dataCallback =
            new ApolloCall.Callback<RefreshSupplierTokenMutation.Data>() {

        @Override
        public void onResponse(@Nonnull Response<RefreshSupplierTokenMutation.Data> response) {

            if (response.isSuccessful()) {
                String newAccessToken = response.data().refreshSupplierToken();
                SettingsUtils.putString(BaseAuthenticatedActivity.this,
                        SettingsUtils.PREF_ACCESS_TOKEN,
                        newAccessToken);
                SettingsUtils.putLong(BaseAuthenticatedActivity.this,
                        SettingsUtils.PREF_LAST_ACCESS_TOKEN_REFRESH_TIMESTAMP,
                        new Date().getTime());

                TNMApplication.IsRefreshingToken = false;

                // Reinitialize apollo client with the brand new access token
                ((TNMApplication) getApplication()).initApolloClient(newAccessToken);

                // We need to run on the ui thread because some activities modify views
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onTokenRefreshed();
                    }
                });

                LOG_DEBUG(TAG, "TokenRefreshed");
            } else {
                for (Error e : response.errors()) {
                    LOG_DEBUG(TAG, e.message());
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {

        }
    };

    /**
     * Called when the access token is refreshed
     */
    protected abstract void onTokenRefreshed();
}
