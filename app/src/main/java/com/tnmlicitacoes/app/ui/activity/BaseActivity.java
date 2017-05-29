package com.tnmlicitacoes.app.ui.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.fcm.MyFcmListenerService;
import com.tnmlicitacoes.app.interfaces.OnSessionChangedListener;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set app title color to white
        setupTaskDescription();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
    }

    protected void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
        }

    }

    protected void setupToolbar(String title) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
        }
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

    private void setupTaskDescription() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc =
                    new ActivityManager.TaskDescription(getString(R.string.app_name),
                            bitmap, getResources().getColor(R.color.colorPrimaryDark));
            setTaskDescription(taskDesc);
        }
    }
}
