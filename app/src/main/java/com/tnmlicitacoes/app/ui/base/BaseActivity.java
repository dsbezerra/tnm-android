package com.tnmlicitacoes.app.ui.base;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public abstract class BaseActivity extends AppCompatActivity {

    /* The logging tag */
    private static final String TAG = "BaseActivity";

    /* The main toolbar */
    protected Toolbar mToolbar;

    /* The application singleton */
    protected TnmApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_DEBUG(getLogTag(), "onCreate");
        mApplication = (TnmApplication) getApplication();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LOG_DEBUG(getLogTag(), "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LOG_DEBUG(getLogTag(), "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOG_DEBUG(getLogTag(), "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LOG_DEBUG(getLogTag(), "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LOG_DEBUG(getLogTag(), "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOG_DEBUG(getLogTag(), "onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
    }

    protected void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
        }
    }

    protected void setupToolbar(String title) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    protected Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
            }
        }
        return mToolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public abstract String getLogTag();
}
