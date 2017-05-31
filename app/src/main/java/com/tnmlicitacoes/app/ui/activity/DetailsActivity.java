package com.tnmlicitacoes.app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseActivity;
import com.tnmlicitacoes.app.ui.fragment.DetailsFragment;

/**
 *  DetailsActivity
 *  Show the details about the selected public notice
 */

public class DetailsActivity extends BaseActivity {

    private DetailsFragment mDetailsFragment;

    private ScrollView mContentView;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Toast.makeText(this, getString(R.string.bidding_details_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_details);
        setupToolbar("Detalhes da Licitação");
        initViews();

        mDetailsFragment = (DetailsFragment) getSupportFragmentManager().findFragmentByTag("detailsFrag");
        if(mDetailsFragment == null) {
            mDetailsFragment = new DetailsFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_details, mDetailsFragment, "detailsFrag");
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_see_online) {
            mDetailsFragment.showOnline();
            return true;
        }
        else if (id == R.id.action_send_to_email) {
            mDetailsFragment.sendEmail();
            return true;
        }
        else if (id == R.id.action_download) {
            mDetailsFragment.startDownloadProcess();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mContentView = (ScrollView) findViewById(R.id.scrollView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public void toggleProgressBar(boolean isVisible) {
        mProgressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mContentView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

}
