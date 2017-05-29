package com.tnmlicitacoes.app.ui.activity;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.tnmlicitacoes.app.GetSupplierAccessToken;
import com.tnmlicitacoes.app.TNMApplication;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.Date;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    private ApolloCall<GetSupplierAccessToken> mGetSupplierAccessTokenCall;

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldRefreshToken()) {

        }
    }

    private void refreshToken() {
        TNMApplication application = (TNMApplication) getApplication();

    }

    /**
     * Checks if we need to refresh the supplier access token
     * @return
     */
    private boolean shouldRefreshToken() {
        long lastRefreshTimestamp = SettingsUtils.getLastAccessTokenRefreshTimestamp(this);
        long currentTimestamp = new Date().getTime();
        return currentTimestamp - lastRefreshTimestamp >= Utils.HOUR_IN_MILLIS;
    }
}
