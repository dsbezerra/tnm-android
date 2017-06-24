package com.tnmlicitacoes.app.ui.base;

import android.util.Base64;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.RefreshSupplierTokenMutation;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.AuthStateListener;
import com.tnmlicitacoes.app.utils.ApiUtils;
import com.tnmlicitacoes.app.utils.CryptoUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.Date;

import javax.annotation.Nonnull;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    /* The logging tag */
    private static final String TAG = "BaseAuthActivity";

    /* Listener that notify views when the authentication state changes */
    protected AuthStateListener mAuthStateListener;

    @Override
    protected void onStart() {
        super.onStart();
        if (TnmApplication.shouldRefreshToken(this)) {
            refreshToken();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthStateListener != null) {
            mAuthStateListener = null;
        }
    }

    /**
     * Refreshes the access token
     */
    public void refreshToken() {

        // If we already refreshing ignore
        if (TnmApplication.IsRefreshingToken) {
            return;
        }

        TnmApplication.IsRefreshingToken = true;

        // Initialize apollo with refresh token as the authorization header
        TnmApplication application = (TnmApplication) getApplication();
        application.initApolloClient(SettingsUtils.getRefreshToken(this), true);

        RefreshSupplierTokenMutation mutation = new RefreshSupplierTokenMutation();
        ApolloCall<RefreshSupplierTokenMutation.Data> call = application.getApolloClient()
                .mutate(mutation)
                .cacheControl(CacheControl.NETWORK_ONLY);

        call.enqueue(dataCallback);
    }

    /**
     * Callback for the refresh token call
     */
    private ApolloCall.Callback<RefreshSupplierTokenMutation.Data> dataCallback =
            new ApolloCall.Callback<RefreshSupplierTokenMutation.Data>() {

        @Override
        public void onResponse(@Nonnull final Response<RefreshSupplierTokenMutation.Data> response) {

            if (!response.hasErrors()) {
                final String newAccessToken = response.data().refreshSupplierToken();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Encrypt refresh and access token and save them as Base64 strings
                            // TODO(diego): Remove from prefs and put in the Supplier model in Realm
                            byte[] encryptedAccessToken = CryptoUtils.getInstance().encrypt(getApplicationContext(),
                                    newAccessToken.getBytes());
                            // Save to prefs
                            SettingsUtils.putString(BaseAuthenticatedActivity.this, SettingsUtils.PREF_ACCESS_TOKEN,
                                    Base64.encodeToString(encryptedAccessToken, Base64.DEFAULT));
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Save without encryption
                            SettingsUtils.putString(BaseAuthenticatedActivity.this, SettingsUtils.PREF_ACCESS_TOKEN,
                                    newAccessToken);
                        }
                    }
                }).start();

                SettingsUtils.putLong(BaseAuthenticatedActivity.this,
                        SettingsUtils.PREF_LAST_ACCESS_TOKEN_REFRESH_TIMESTAMP,
                        new Date().getTime());

                TnmApplication.IsRefreshingToken = false;

                // Reinitialize apollo client with the brand new access token
                ((TnmApplication) getApplication()).initApolloClient(newAccessToken, false);

                // We need to run on the ui thread because some activities modify views
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Notify for changes so the other fragments or activities that implement
                        // this interface can refetch the content with the new access token
                        if (mAuthStateListener != null) {
                            mAuthStateListener.onAuthChanged();
                        }
                    }
                });

                LOG_DEBUG(TAG, "TokenRefreshed");
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ApiUtils.getFirstValidError(getApplicationContext(), response.errors());
                    }
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            LOG_DEBUG(TAG, e.getMessage());
        }
    };
}
