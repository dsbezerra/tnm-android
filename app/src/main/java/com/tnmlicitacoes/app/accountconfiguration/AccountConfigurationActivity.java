package com.tnmlicitacoes.app.accountconfiguration;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.tnmlicitacoes.app.apollo.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.apollo.SegmentsQuery;
import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.main.MainActivity;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class AccountConfigurationActivity extends BaseAuthenticatedActivity implements OnAccountConfigurationListener {

    @Override
    public String getLogTag() {
        return TAG;
    }

    /* The logging tag */
    private static final String TAG = "AccountConfigurationActivity";

    /* The city select view indicator */
    private static final int VIEW_CITY_SELECT = 0;

    /* The segment select view indicator */
    private static final int VIEW_SEGMENT_SELECT = 1;

    /* Holds a list of fragments to this activity*/
    private List<AccountConfigurationFragment> mFragments = new ArrayList<>();

    /* The current displayed fragment */
    private Fragment mCurrentFragment;

    /* Displays the information about how many items were selected */
    private TextView mSelectedText;

    /* The continue button */
    private Button mContinueButton;

    /* Tracks the current select view */
    private int mCurrentViewNum = VIEW_CITY_SELECT;

    /* The realm instance */
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_configuration);
        mRealm = Realm.getDefaultInstance();

        mFragments = getFragments();
        mCurrentFragment = getCurrentFragment(mCurrentViewNum);

        initViews();
        initViewListeners(this);

        if(savedInstanceState != null) {
            mCurrentViewNum = savedInstanceState.getInt("currentViewNum");
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.init_config_content, mCurrentFragment);
        fragmentTransaction.commit();

        updateSelectedText(0);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                String id = FirebaseInstanceId.getInstance().getId();
                String token = FirebaseInstanceId.getInstance().getToken();
                LOG_DEBUG(TAG, "InstanceID: " + id);
                LOG_DEBUG(TAG, "Token: " + token);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void initViewListeners(final Context context) {
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentFragment instanceof SelectCityFragment) {
                    mCurrentViewNum++;
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
                    fragmentTransaction.replace(R.id.init_config_content, getCurrentFragment(mCurrentViewNum));
                    fragmentTransaction.commit();
                    updateSelectedText(0);

                } else if (mCurrentFragment instanceof SelectSegmentFragment) {

                    SettingsUtils.putBoolean(AccountConfigurationActivity.this,
                            SettingsUtils.PREF_NEED_TO_UPDATE_TOPICS, true);
                    SettingsUtils.putBoolean(AccountConfigurationActivity.this,
                            SettingsUtils.PREF_INITIAL_CONFIG_IS_FINISHED, true);

                    Intent intent = new Intent(AccountConfigurationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                setupToolbar();
            }
        });
    }

    private void initViews() {
        mSelectedText = (TextView) findViewById(R.id.remainingCities);
        mContinueButton = (Button) findViewById(R.id.buttonContinue);
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupToolbar();
    }

    @Override
    public void onBackPressed() {
        if(mCurrentViewNum != 0) {
            goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void goBack() {
        mCurrentViewNum--;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
        fragmentTransaction.replace(R.id.init_config_content, getCurrentFragment(mCurrentViewNum));
        fragmentTransaction.commit();
        setupToolbar();
        updateSelectedText(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentViewNum", mCurrentViewNum);
    }

    @Override
    public void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Configuração inicial");
            mCurrentFragment = getCurrentFragment(mCurrentViewNum);
            if (mCurrentFragment instanceof SelectCityFragment) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else if(mCurrentFragment instanceof SelectSegmentFragment) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            {
                if (mCurrentViewNum != VIEW_CITY_SELECT) {
                    goBack();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback for when a city is selected
     * Updates the selected text on success and shou a dialog on failure
     * Failure only happens when the user cannot add more cities because of his subscription limit
     * @param newCount count of selected cities and -1 on failure
     */
    @Override
    public void onCitySelected(int newCount, CitiesQuery.Node city) {
        // Let's update the selected text in the bottom of view
        // and add to database
        if (newCount >= 0 && city != null) {
            // Persist if is a new selected. Remove if is already persisted
            PickedCity resultCity = mRealm.where(PickedCity.class).equalTo("id", city.id()).findFirst();
            if (resultCity == null) {
                // Create realm object
                PickedCity pickedCity = new PickedCity(city.id(), city.name(), city.state().name());
                // Persist realm object
                mRealm.beginTransaction();
                mRealm.copyToRealm(pickedCity);
                mRealm.commitTransaction();
            } else {
                mRealm.beginTransaction();
                resultCity.deleteFromRealm();
                mRealm.commitTransaction();
            }

            // Update selected text
            updateSelectedText(newCount);
        } else if (newCount >= 0) {
            // Update selected text
            updateSelectedText(newCount);
        } else {
            // If we fall here, it means the user can't select more cities
            // let's show a dialog explaining why he can't add more cities
            // TODO(diego): Explaining dialog
            Toast.makeText(this, "Limite excedido!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for when a segment is selected
     * Updates the selected text on success and shou a dialog on failure
     * Failure only happens when the user cannot add more segments because of his subscription limit
     * @param newCount count of selected segments and -1 on failure
     */
    @Override
    public void onSegmentSelected(int newCount, SegmentsQuery.Node segment) {
        // Let's update the selected text in the bottom of view
        // and add to database
        if (newCount >= 0 && segment != null) {
            // Persist if is a new selected. Remove if is already persisted
            PickedSegment resultSegment = mRealm.where(PickedSegment.class).equalTo("id", segment.id()).findFirst();
            if (resultSegment == null) {
                // Create realm object
                PickedSegment pickedSegment = new PickedSegment(segment.id(), segment.name());
                // Persist realm object
                mRealm.beginTransaction();
                mRealm.copyToRealm(pickedSegment);
                mRealm.commitTransaction();
            } else {
                mRealm.beginTransaction();
                resultSegment.deleteFromRealm();
                mRealm.commitTransaction();
            }

            // Update selected text
            updateSelectedText(newCount);
        } else if (newCount >= 0) {
            // Update selected text
            updateSelectedText(newCount);
        } else {
            // If we fall here, it means the user can't select more segments
            // let's show a dialog explaining why he can't add more segments
            // TODO(diego): Explaining dialog
            Toast.makeText(this, "Limite excedido!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCompleteInitialisation(String tag) {

    }

    /**
     * Returns the current fragment displayed instantiating them if they're not initilized
     * @param current
     * @return
     */
    private Fragment getCurrentFragment(int current) {
        if(mFragments != null) {
            if(mFragments.size() > 0) {
                return mFragments.get(current);
            } else {
                mFragments = getFragments();
                return mFragments.get(current);
            }
        }
        return null;
    }

    private static List<AccountConfigurationFragment> getFragments() {
//        return new ArrayList<>(Arrays.asList(
//                new SelectCityFragment(),
//                new SelectSegmentFragment()
//        ));
        return  new ArrayList<>();
    }

    private void updateSelectedText(int selectedSize) {
        if(selectedSize > 0) {
            int max = ((AccountConfigurationFragment)mCurrentFragment).getMaximum();
            mSelectedText.setText(getString(R.string.accountconfiguration_sub_max_selected, selectedSize, max));
            mContinueButton.setEnabled(true);
        } else {
            mSelectedText.setText("");
            mContinueButton.setEnabled(false);
        }
    }
}
