package com.tnmlicitacoes.app.details;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseActivity;
import com.tnmlicitacoes.app.utils.NoticeUtils;

/**
 *  DetailsActivity
 *  Show the seeDetails about the selected public notice
 */

public class DetailsActivity extends BaseActivity {

    @Override
    public String getLogTag() {
        return TAG;
    }

    private static final String TAG = "DetailsActivity";

    private DetailsFragment mDetailsFragment;

    private ScrollView mContentView;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null || extras.isEmpty()) {
            Toast.makeText(this, getString(R.string.bidding_details_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_details);
        setupToolbar("Detalhes da Licitação");
        initViews();

        mDetailsFragment = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DetailsFragment.TAG);
        if (mDetailsFragment == null) {
            String id = extras.getString(DetailsFragment.NOTICE_ID);
            boolean fromNotification = extras.getBoolean(DetailsFragment.FROM_NOTIFICATION);
            mDetailsFragment = DetailsFragment.newInstance(id, fromNotification);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_details, mDetailsFragment, DetailsFragment.TAG);
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem seeWebsite = menu.findItem(R.id.action_see_in_website);
        MenuItem seeOnline = menu.findItem(R.id.action_see_online);

        seeOnline.setVisible(mDetailsFragment != null && mDetailsFragment.hasPdfDirectLink());
        seeWebsite.setVisible(mDetailsFragment != null && mDetailsFragment.hasWebsiteUrl());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_download:
                mDetailsFragment.download();
                break;

            case R.id.action_see_online:
                mDetailsFragment.seeOnline();
                break;

            case R.id.action_send_to_email:
                mDetailsFragment.sendToEmail();

            case R.id.action_see_in_website:
                mDetailsFragment.seeInWebsite();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mContentView = (ScrollView) findViewById(R.id.scrollView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ScrollView getContentView() {
        return mContentView;
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
