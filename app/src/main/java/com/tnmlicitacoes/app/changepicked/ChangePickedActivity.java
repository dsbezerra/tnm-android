package com.tnmlicitacoes.app.changepicked;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloCallback;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SegmentsQuery;
import com.tnmlicitacoes.app.UpdateSupplierMutation;
import com.tnmlicitacoes.app.accountconfiguration.SelectCityFragment;
import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener;
import com.tnmlicitacoes.app.model.realm.LocalSupplier;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.accountconfiguration.SelectSegmentFragment;
import com.tnmlicitacoes.app.type.SupplierInput;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.utils.ApiUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import io.realm.Realm;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;


public class ChangePickedActivity extends BaseAuthenticatedActivity implements OnAccountConfigurationListener{

    @Override
    public String getLogTag() {
        return TAG;
    }

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

    /** UI handler used to update views from Apollo callbacks */
    private Handler mUiHandler = new Handler(Looper.getMainLooper());


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
            fragment = getSupportFragmentManager().findFragmentByTag(SelectCityFragment.TAG);
            if (fragment == null) {
                fragment = new SelectCityFragment();
            }
            fragmentTag = SelectCityFragment.TAG;
        } else if (mCurrentView == SEGMENTS) {
            fragment = getSupportFragmentManager().findFragmentByTag(SelectSegmentFragment.TAG);
            if (fragment == null) {
                fragment = new SelectSegmentFragment();
            }
            fragmentTag = SelectSegmentFragment.TAG;
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
        SelectCityFragment fragment = (SelectCityFragment) getSupportFragmentManager()
                .findFragmentByTag(SelectCityFragment.TAG);
        if (fragment != null) {
            HashMap<String, CitiesQuery.Node> newSelected = fragment.getAdapter().getSelected();
            if (newSelected != null && !newSelected.isEmpty()) {
                // Remove all first
                mRealm.beginTransaction();
                mRealm.where(PickedCity.class).findAll().deleteAllFromRealm();
                mRealm.commitTransaction();

                // Cities arg from GraphQL
                List<Object> cities = new ArrayList<>();

                // Then add each one
                mRealm.beginTransaction();
                for (String key : newSelected.keySet()) {
                    CitiesQuery.Node nodeCity = newSelected.get(key);
                    cities.add(nodeCity.id());
                    if (nodeCity != null) {
                        mRealm.copyToRealmOrUpdate(PickedCity.copyToRealmFromGraphQL(nodeCity));
                    }
                }
                mRealm.commitTransaction();

                // Send mutation to servers
                UpdateSupplierMutation mutation = UpdateSupplierMutation.builder()
                        .supplier(SupplierInput
                                .builder()
                                .cities(cities).build())
                        .build();

                mApplication.getApolloClient()
                        .mutate(mutation)
                        .enqueue(new ApolloCallback<>(new ApolloCall.Callback<UpdateSupplierMutation.Data>() {
                            @Override
                            public void onResponse(@Nonnull Response<UpdateSupplierMutation.Data> response) {
                                Toast.makeText(ChangePickedActivity.this, "Alterado com sucesso!", Toast.LENGTH_SHORT).show();

                                if (!response.hasErrors()) {
                                    finish();
                                } else {
                                    // For now if we get errors let's finish
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@Nonnull ApolloException e) {
                                // For now if we get errors let's finish
                                finish();
                            }
                        }, mUiHandler));
            }
        }
    }

    // TODO(diego): Add call to graphql server so we can update the user picked segments in server too
    // put flag to update firebase topics
    private void updateSegments() {
        SelectSegmentFragment fragment = (SelectSegmentFragment) getSupportFragmentManager()
                .findFragmentByTag(SelectSegmentFragment.TAG);
        if (fragment != null) {
            HashMap<String, SegmentsQuery.Node> newSelected = fragment.getAdapter().getSelected();
            if (newSelected != null && !newSelected.isEmpty()) {
                // Remove all first
                mRealm.beginTransaction();
                mRealm.where(PickedSegment.class).findAll().deleteAllFromRealm();
                mRealm.commitTransaction();

                // Segments arg from GraphQL
                List<Object> segments = new ArrayList<>();

                // Then add each one
                mRealm.beginTransaction();
                for (String key : newSelected.keySet()) {
                    SegmentsQuery.Node nodeSegment = newSelected.get(key);
                    segments.add(nodeSegment.id());
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

                SettingsUtils.putBoolean(this, SettingsUtils.PREF_NEED_TO_UPDATE_TOPICS, true);

                // Send mutation to servers
                UpdateSupplierMutation mutation = UpdateSupplierMutation.builder()
                        .supplier(SupplierInput
                                .builder()
                                .segments(segments).build())
                        .build();

                mApplication.getApolloClient()
                        .mutate(mutation)
                        .enqueue(new ApolloCallback<>(new ApolloCall.Callback<UpdateSupplierMutation.Data>() {
                            @Override
                            public void onResponse(@Nonnull Response<UpdateSupplierMutation.Data> response) {
                                Toast.makeText(ChangePickedActivity.this, "Alterado com sucesso!", Toast.LENGTH_SHORT).show();

                                if (!response.hasErrors()) {
                                    finish();
                                } else {
                                    // For now if we get errors let's finish
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@Nonnull ApolloException e) {
                                // For now if we get errors let's finish
                                finish();
                            }
                        }, mUiHandler));
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

    @Override
    public void onCompleteInitialisation(String tag) {}

    private void updateSelectedText(int selectedSize) {
        if (selectedSize > 0) {
            int max = mCurrentView == CITIES ? mRealm.where(LocalSupplier.class).findFirst().getCityNum() :
                    mRealm.where(LocalSupplier.class).findFirst().getSegNum();
            mSelectedText.setText(getString(R.string.accountconfiguration_sub_max_selected,
                    selectedSize, max));
            mConfirmButton.setEnabled(true);
        } else {
            mSelectedText.setText("");
            mConfirmButton.setEnabled(false);
        }
    }
}
