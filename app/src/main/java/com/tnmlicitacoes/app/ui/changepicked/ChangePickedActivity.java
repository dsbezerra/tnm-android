package com.tnmlicitacoes.app.ui.changepicked;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SegmentsQuery;
import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.ui.accountconfiguration.CitySelectFragment;
import com.tnmlicitacoes.app.ui.accountconfiguration.SegmentSelectFragment;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.HashMap;

import io.realm.Realm;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;


public class ChangePickedActivity extends BaseAuthenticatedActivity implements OnAccountConfigurationListener{

    /* The logging tag */
    private static final String TAG = "ChangePickedActivity";

    /* View to display argument key */
    public static final String VIEW = "view";

    /* Cities fragment id */
    public static final int CITIES   = 0;

    /* Segments id */
    public static final int SEGMENTS = 1;

    /* The bottom remaining text */
    private TextView mSelectedText;

    /* The bottom confirm button */
    private Button mConfirmButton;

    /* Current displayed fragment */
    private int mCurrentView;

    /* Realm instance */
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_change_picked);
        handleIntent();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    /**
     * Initializes the views
     */
    private void initViews() {
        mSelectedText = (TextView) findViewById(R.id.remainingText);
        mConfirmButton = (Button) findViewById(R.id.buttonConfirm);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentView == CITIES) {
                    updateCities();
                } else if (mCurrentView == SEGMENTS) {
                    updateSegments();
                }

                Toast.makeText(ChangePickedActivity.this, "Alterado com sucesso!", Toast.LENGTH_SHORT).show();
                SettingsUtils.putBoolean(ChangePickedActivity.this, SettingsUtils.PREF_NEED_TO_UPDATE_TOPICS, true);
                finish();
            }
        });

        setupToolbar(mCurrentView == CITIES ? "Alterando cidades" : "Alterando segmentos");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Handle the intent
     */
    private void handleIntent() {

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null && !extras.isEmpty()) {
                mCurrentView = extras.getInt(VIEW);
                showFragmentForView();
                return;
            }
        }

        // If we couldn't handle this intent there's no point to continue showing this
        // activity.
        finish();
    }

    /**
     * Displays the fragment for the specified view
     */
    private void showFragmentForView() {

        Fragment fragment = null;
        String fragmentTag = "";
        if (mCurrentView == CITIES) {
            fragment = getSupportFragmentManager().findFragmentByTag(CitySelectFragment.TAG);
            if (fragment == null) {
                fragment = new CitySelectFragment();
            }
            fragmentTag = CitySelectFragment.TAG;
        } else if (mCurrentView == SEGMENTS) {
            fragment = getSupportFragmentManager().findFragmentByTag(SegmentSelectFragment.TAG);
            if (fragment == null) {
                fragment = new SegmentSelectFragment();
            }
            fragmentTag = SegmentSelectFragment.TAG;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, fragmentTag)
                    .commit();
        }
    }

    // TODO(diego): Add call to graphql server so we can update the user picked cities in server too
    // put flag to update firebase topics
    private void updateCities() {
        CitySelectFragment fragment = (CitySelectFragment) getSupportFragmentManager()
                .findFragmentByTag(CitySelectFragment.TAG);
        if (fragment != null) {
            HashMap<String, CitiesQuery.Node> newSelected = fragment.getAdapter().getSelected();
            if (newSelected != null && !newSelected.isEmpty()) {
                // Remove all first
                mRealm.beginTransaction();
                mRealm.where(PickedCity.class).findAll().deleteAllFromRealm();
                mRealm.commitTransaction();

                // Then add each one
                mRealm.beginTransaction();
                for (String key : newSelected.keySet()) {
                    CitiesQuery.Node nodeCity = newSelected.get(key);
                    if (nodeCity != null) {
                        mRealm.copyToRealmOrUpdate(PickedCity.copyToRealmFromGraphQL(nodeCity));
                    }
                }
                mRealm.commitTransaction();
            }
        }
    }

    // TODO(diego): Add call to graphql server so we can update the user picked segments in server too
    // put flag to update firebase topics
    private void updateSegments() {
        SegmentSelectFragment fragment = (SegmentSelectFragment) getSupportFragmentManager()
                .findFragmentByTag(SegmentSelectFragment.TAG);
        if (fragment != null) {
            HashMap<String, SegmentsQuery.Node> newSelected = fragment.getAdapter().getSelected();
            if (newSelected != null && !newSelected.isEmpty()) {
                // Remove all first
                mRealm.beginTransaction();
                mRealm.where(PickedSegment.class).findAll().deleteAllFromRealm();
                mRealm.commitTransaction();

                // Then add each one
                mRealm.beginTransaction();
                for (String key : newSelected.keySet()) {
                    SegmentsQuery.Node nodeSegment = newSelected.get(key);
                    if (nodeSegment != null) {
                        mRealm.copyToRealmOrUpdate(new PickedSegment(
                                nodeSegment.id(), nodeSegment.name(), nodeSegment.icon(),
                                nodeSegment.defaultImg(), nodeSegment.mqdefault(),
                                nodeSegment.hqdefault()
                        ));
                        LOG_DEBUG(TAG, "Added " + nodeSegment.name());
                    }
                }
                mRealm.commitTransaction();
            }
        }
    }

    @Override
    public void onCitySelected(int selectedSize, CitiesQuery.Node city) {
        updateSelectedText(selectedSize);
    }

    @Override
    public void onSegmentSelected(int selectedSize, SegmentsQuery.Node segment) {
        updateSelectedText(selectedSize);
    }

    private void updateSelectedText(int selectedSize) {
        if(selectedSize > 0) {
            mSelectedText.setText(getString(R.string.accountconfiguration_sub_max_selected,
                    selectedSize,
                    BillingUtils.getMaxText(BillingUtils.SUBSCRIPTION_MAX_ITEMS)));
            mConfirmButton.setEnabled(true);
        } else {
            mSelectedText.setText("");
            mConfirmButton.setEnabled(false);
        }
    }
}
