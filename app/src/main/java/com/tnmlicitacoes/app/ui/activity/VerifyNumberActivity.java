package com.tnmlicitacoes.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.fragment.InputNumberFragment;
import com.tnmlicitacoes.app.ui.fragment.WaitingSmsFragment;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerifyNumberActivity extends BaseActivity {

    private static final String TAG = "VerifyNumberActivity";

    private static List<VerifyNumberContent> sFragments;

    private VerifyNumberContent mFragment;

    private ProgressDialog mProgressDialog;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);

        initViews();

        mFragment = getCurrentFragment();

        if(savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.verify_number_content, (Fragment) mFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(!AndroidUtilities.sIsWaitingSms)
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SettingsUtils.isWaitingForSms(this))
            AndroidUtilities.sIsWaitingSms = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        AndroidUtilities.sIsWaitingSms = false;
    }

    private void replaceFragmentContent() {
        // Replace fragment
        mFragment = getCurrentFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.verify_number_content, (Fragment) mFragment);
        fragmentTransaction.commit();
    }

    /**
     * Initialize all views
     */
    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mProgressDialog = new ProgressDialog(this, R.style.MaterialBaseTheme_Light_AlertDialog);
        mProgressDialog.setMessage(getString(R.string.wait_a_moment_dialog_text));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    /**
     * Updates UI
     */
    private void showProgressDialog(boolean processing) {
        if(processing) {
            mProgressDialog.show();
        }
        else {
            mProgressDialog.dismiss();
        }
    }


    /**
     * Get the current fragment to display
     *
     * @return the current fragment
     */
    private VerifyNumberContent getCurrentFragment() {
        if(sFragments == null) {
            sFragments = getFragments();
        }

        for(VerifyNumberContent fragment : sFragments) {
            if(fragment.shouldDisplay(this)) {
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
    private static List<VerifyNumberContent> getFragments() {
        return new ArrayList<VerifyNumberContent>(Arrays.asList(
                new InputNumberFragment(),
                new WaitingSmsFragment()
        ));
    }

    public interface VerifyNumberContent {

        /**
         * Return true if the fragment should be displayed
         * @param context
         */
        boolean shouldDisplay(Context context);
    }
}
