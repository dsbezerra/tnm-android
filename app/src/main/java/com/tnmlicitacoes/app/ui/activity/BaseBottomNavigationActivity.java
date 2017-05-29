package com.tnmlicitacoes.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;

import com.tnmlicitacoes.app.R;

public abstract class BaseBottomNavigationActivity extends BaseActivity {

    // TODO(diego): Do one activity that handle all other fragments for this to work

    private static final String TAG = "BaseBottomNavigationActivity";

    protected BottomNavigationView mBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        mBottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Intent intent = null;

                switch (item.getItemId()) {
                    case R.id.action_home:
                        intent = new Intent(BaseBottomNavigationActivity.this,
                                MainActivity.class);
                        break;

                    case R.id.action_my_biddings:
                        intent = new Intent(BaseBottomNavigationActivity.this,
                                MyBiddingsActivity.class);
                        break;

                    case R.id.action_account:
                        intent = new Intent(BaseBottomNavigationActivity.this,
                                MySubscriptionActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
                    return true;
                }

                return false;
            }
        });
    }
}
