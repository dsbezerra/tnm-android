package com.tnmlicitacoes.app.verifynumber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.apollo.RequestCodeMutation;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.registration.InputNameFragment;
import com.tnmlicitacoes.app.registration.RegistrationActivity;
import com.tnmlicitacoes.app.interfaces.OnVerifyNumberListener;
import com.tnmlicitacoes.app.ui.base.BaseActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.CryptoUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.UIUtils;
import com.tnmlicitacoes.app.utils.Utils;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class VerifyNumberActivity extends BaseActivity implements OnVerifyNumberListener {

    /* The logging tag */
    private static final String TAG = "VerifyNumberActivity";

    /* Keeps track of the current fragment displayed */
    private VerifyNumberFragment mCurrentFragment = null;

    /* Holds a list with the verify number fragments */
    private List<VerifyNumberContent> mFragments;

    /* Transition container */
    private ViewGroup mTransitionContainer;

    /* Realm instance */
    private Realm mRealm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);

        // Just to make sure that the database is clean
        // TODO(diego): Extract this to a method that includes erasing preferences too
        mRealm = Realm.getDefaultInstance();
        mRealm.beginTransaction();
        mRealm.deleteAll();
        mRealm.commitTransaction();

        initViews();

        if (savedInstanceState == null) {
            showFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Make possible to go back to input number fragment if the waiting time is over than
        // 4 minutes
        long currentTimestamp = new Date().getTime();
        long requestCodeTimestamp = SettingsUtils.getWaitingForSmsTimestamp(this);
        if (requestCodeTimestamp == SettingsUtils.LONG_DEFAULT) {
            return;
        }

        if (currentTimestamp - requestCodeTimestamp >= Utils.MINUTE_IN_MILLIS * 4) {
            SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (SettingsUtils.isWaitingForSms(this)) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    /**
     * Initializes the views
     */
    private void initViews() {
        mTransitionContainer = (ViewGroup) findViewById(R.id.root);
    }

    /**
     * Show the next fragment with animation
     */
    private void showFragment() {
        mCurrentFragment = (VerifyNumberFragment) getCurrentFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment instanceof WaitingSmsFragment) {
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.setCustomAnimations(
                    R.anim.activity_fade_enter, R.anim.activity_fade_exit,
                    R.anim.activity_fade_enter, R.anim.activity_fade_exit
            );
        } else {
            fragmentTransaction.setCustomAnimations(
                    R.anim.fragment_enter_from_bottom, 0,
                    R.anim.fragment_enter_from_bottom, 0
            );
        }
        TransitionManager.beginDelayedTransition(mTransitionContainer);
        fragmentTransaction.replace(R.id.verify_number_content, mCurrentFragment)
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
            SettingsUtils.putLong(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS_TIMESTAMP,
                    System.currentTimeMillis());
            // Setup trial variables here
            showFragment();
        } else {
            // TODO(diego): Remove this in Production
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            // TODO: FIX ME FIX ME FIX ME FIX ME
            SettingsUtils.putLong(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS_TIMESTAMP,
                    System.currentTimeMillis());
            SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, true);
            showFragment();
        }
    }

    @Override
    public void onRegisterFinished(String refreshToken, String accessToken) {
        SettingsUtils.putBoolean(this, SettingsUtils.PREF_IS_WAITING_FOR_SMS, false);
        if (!TextUtils.isEmpty(refreshToken) && !TextUtils.isEmpty(accessToken)) {
            final String fAccessToken = accessToken;
            final String fRefreshToken = refreshToken;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Context context = getApplicationContext();
                    try {
                        // Encrypt refresh and access token and save them as Base64 strings
                        // TODO(diego): Remove from prefs and put in the LocalSupplier model in Realm
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
                }
            }).start();

            SettingsUtils.putLong(this, SettingsUtils.PREF_LAST_ACCESS_TOKEN_REFRESH_TIMESTAMP,
                    new Date().getTime());
            SettingsUtils.putBoolean(this, SettingsUtils.PREF_USER_IS_LOGGED, true);

            // Reinitialize Apollo Client with authentication
            ((TnmApplication) getApplication()).initApolloClient(accessToken, false);

            Intent intent = new Intent(this, RegistrationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
