package com.tnmlicitacoes.app.verifynumber;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.RequestCodeMutation;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.OnVerifyNumberListener;
import com.tnmlicitacoes.app.ui.accountconfiguration.AccountConfigurationActivity;
import com.tnmlicitacoes.app.ui.base.BaseActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.CryptoUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class VerifyNumberActivity extends BaseActivity implements OnVerifyNumberListener {

    /* Tag for logging */
    private static final String TAG = "VerifyNumberActivity";

    /* Keeps track of the current fragment displayed */
    private VerifyNumberFragment mCurrentFragment = null;

    /* Holds a list with the verify number fragments */
    private List<VerifyNumberContent> mFragments;

    /* Transition container */
    private ViewGroup mTransitionContainer;

    /* The logo */
    private ImageView mLogo;

    boolean mToUpAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);

        mTransitionContainer = (ViewGroup) findViewById(R.id.root);
        mLogo = (ImageView) findViewById(R.id.logo);

        if (savedInstanceState == null) {
            showFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Temporary
        if (mCurrentFragment instanceof WaitingSmsFragment) {
            SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, false);
            showFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void showFragment() {
        mCurrentFragment = (VerifyNumberFragment) getCurrentFragment();
        mToUpAnimation = mCurrentFragment instanceof WaitingSmsFragment;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Transition transition = new ChangeBounds();
        transition.setDuration(mToUpAnimation ? 700 : 300);
        transition.setInterpolator(mToUpAnimation ? new FastOutSlowInInterpolator() : new AccelerateInterpolator());
        transition.setStartDelay(mToUpAnimation ? 0 : 500);
        TransitionManager.beginDelayedTransition(mTransitionContainer, transition);

        // Running in UI thread because some functions that call this function is in another thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLogo.getLayoutParams();
                params.topMargin = mToUpAnimation ? AndroidUtilities.dp(VerifyNumberActivity.this, 20)
                                                  : AndroidUtilities.dp(VerifyNumberActivity.this, 120);
                mLogo.setLayoutParams(params);
            }
        });

        fragmentTransaction
                .setCustomAnimations(R.anim.fragment_enter_from_bottom, R.anim.fragment_exit_to_bottom,
                        R.anim.fragment_enter_from_bottom, R.anim.fragment_exit_to_bottom)
                .replace(R.id.verify_number_content, mCurrentFragment)
                .commit();

    }

    /**
     * Get the current fragment to display
     *
     * @return the current fragment
     */
    private VerifyNumberContent getCurrentFragment() {
        if (mFragments == null) {
            mFragments = getFragments();
        }

        for (VerifyNumberContent fragment : mFragments) {
            if (fragment.shouldDisplay(this)) {
                return fragment;
            }
        }

        return null;
    }

    /**
     * Get the two fragments used in VerifyNumberActivity
     *
     * @return the List of fragments.
     */
    private List<VerifyNumberContent> getFragments() {
        return new ArrayList<VerifyNumberContent>(Arrays.asList(
                new InputNumberFragment(),
                new WaitingSmsFragment()
        ));
    }

    @Override
    public void onRequestCodeResponse(Response<RequestCodeMutation.Data> response, ApolloException e) {
        if (response != null && !response.hasErrors()) {
            SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, true);
            // Setup trial variables here
            showFragment();
        } else {
            if (BuildConfig.DEBUG) {
                SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, true);
                showFragment();
            } else {

            }
        }
    }

    @Override
    public void onRegisterFinished(String refreshToken, String accessToken) {
        SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, false);
        // Store these tokens in a secure way
        // @see https://nelenkov.blogspot.com.br/2012/05/storing-application-secrets-in-androids.html
        if (!TextUtils.isEmpty(refreshToken) && !TextUtils.isEmpty(accessToken)) {
            final String fAccessToken = accessToken;
            final String fRefreshToken = refreshToken;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Context context = getApplicationContext();
                    try {
                        // Encrypt refresh and access token and save them as Base64 strings
                        // TODO(diego): Remove from prefs and put in the Supplier model in Realm
                        byte[] encryptedAccessToken = CryptoUtils.getInstance()
                                .encrypt(context, fAccessToken.getBytes());
                        byte[] encryptedRefreshToken = CryptoUtils.getInstance()
                                .encrypt(context, fRefreshToken.getBytes());
                        // Save to prefs
                        SettingsUtils.putString(context, SettingsUtils.PREF_ACCESS_TOKEN,
                                Base64.encodeToString(encryptedAccessToken, Base64.DEFAULT));
                        SettingsUtils.putString(context, SettingsUtils.PREF_REFRESH_TOKEN,
                                Base64.encodeToString(encryptedRefreshToken, Base64.DEFAULT));
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Save without encryption
                        SettingsUtils.putString(context, SettingsUtils.PREF_ACCESS_TOKEN,
                                fAccessToken);
                        SettingsUtils.putString(context, SettingsUtils.PREF_REFRESH_TOKEN,
                                fRefreshToken);
                    }

                    SettingsUtils.putLong(context, SettingsUtils.PREF_LAST_ACCESS_TOKEN_REFRESH_TIMESTAMP,
                            new Date().getTime());
                }
            }).start();

            SettingsUtils.putBoolean(this, SettingsUtils.PREF_USER_IS_LOGGED, true);

            // Reinitialize Apollo Client with authentication
            ((TnmApplication) getApplication()).initApolloClient(accessToken, false);

            Intent intent = new Intent(this, AccountConfigurationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public interface VerifyNumberContent {
        /**
         * Return true if the fragment should be displayed
         * @param context
         */
        boolean shouldDisplay(Context context);
    }
}
