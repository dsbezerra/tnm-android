package com.tnmlicitacoes.app.ui.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SegmentsQuery;
import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener;
import com.tnmlicitacoes.app.ui.base.BaseActivity;
import com.tnmlicitacoes.app.ui.fragment.SegmentSelectFragment;
import com.tnmlicitacoes.app.ui.fragment.CitySelectFragment;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

public class ChangeChosenActivity extends BaseActivity implements OnAccountConfigurationListener {

    private static final String TAG = "ChangeChosenActivity";

    public static final int CITIES_CHANGE_FRAGMENT = 0;

    public static final int CATEGORIES_CHANGE_FRAGMENT = 1;

    public static final String FRAGMENT_ID = "FRAGMENT_ID";

    private Fragment mFragment;

    private TextView mRemainingText;

    private Button mButtonConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_chosen);
        setupToolbar();

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
            return;
        }

        initViews();

        mFragment = getSupportFragmentManager().findFragmentByTag("changeFrag");
        if(mFragment == null) {

            int fragmentId = extras.getInt("FRAGMENT_ID", -1);
            if(fragmentId == CITIES_CHANGE_FRAGMENT) {
                mFragment = new CitySelectFragment();
            } else if (fragmentId == CATEGORIES_CHANGE_FRAGMENT) {
                mFragment = new SegmentSelectFragment();
            } else {
                Toast.makeText(this, getString(R.string.sorry_operation_error_message), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, mFragment, "changeFrag");
            ft.commit();
        }

        BillingUtils.setDefaultMaxItems(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        mRemainingText = (TextView) findViewById(R.id.remainingText);
        mButtonConfirm = (Button) findViewById(R.id.buttonConfirm);
        if(mButtonConfirm != null) {
            mButtonConfirm.setEnabled(false);
        }

        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                persistData();
            }
        });
    }

    private void persistData() {
        if(mFragment instanceof CitySelectFragment) {
            persistCities();
        } else if (mFragment instanceof SegmentSelectFragment) {
            persistCategories();
        }
        SettingsUtils.putBoolean(this, SettingsUtils.PREF_NEED_TO_UPDATE_TOPICS, true);
        SettingsUtils.eraseTemporarySettings(this);
    }

    private void persistCities() {
    }

    private void persistCategories() {
    }

    @Override
    public void onCitySelected(int selectedSize, CitiesQuery.Node city) {
        mRemainingText.setText(getString(R.string.sub_max_selected, selectedSize, BillingUtils.getMaxText(BillingUtils.SUBSCRIPTION_MAX_ITEMS)));
        mRemainingText.setVisibility(selectedSize > 0 ? View.VISIBLE : View.INVISIBLE);
        mButtonConfirm.setEnabled(selectedSize > 0);
    }

    @Override
    public void onSegmentSelected(int selectedSize, SegmentsQuery.Node segment) {
        mRemainingText.setText(getString(R.string.sub_max_selected, selectedSize, BillingUtils.getMaxText(BillingUtils.SUBSCRIPTION_MAX_ITEMS)));
        mRemainingText.setVisibility(selectedSize > 0 ? View.VISIBLE : View.INVISIBLE);
        mButtonConfirm.setEnabled(selectedSize > 0);
    }
}
