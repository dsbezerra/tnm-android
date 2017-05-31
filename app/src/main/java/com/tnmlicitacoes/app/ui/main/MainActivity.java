package com.tnmlicitacoes.app.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.evernote.android.state.State;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.billing.BillingActivity;
import com.tnmlicitacoes.app.billing.IabHelper;
import com.tnmlicitacoes.app.billing.IabResult;
import com.tnmlicitacoes.app.billing.Inventory;
import com.tnmlicitacoes.app.billing.Purchase;
import com.tnmlicitacoes.app.model.Subscription;
import com.tnmlicitacoes.app.interfaces.OnFilterClickListener;
import com.tnmlicitacoes.app.interfaces.OnUpdateListener;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.ui.fragment.NoticeTabFragment;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class MainActivity extends BaseAuthenticatedActivity implements
        OnFilterClickListener, OnUpdateListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    @State
    public String mCurrentFragmentTag;

    private IabHelper mBillingHelper;

    private BottomNavigationView mBottomNavigationView;

    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupToolbar();
        setupInAppBilling();

        AndroidUtilities.sOnUpdateListener = this;
        if (SettingsUtils.getNewestVersionCode(this) > BuildConfig.VERSION_CODE) {
            showUpdateDialog();
        }

        Fragment fragment;
        // First time the activity is created we start by showing the user
        // the NoticesFragment (which contains the segments tabs and notice lists)
        if (savedInstanceState == null) {
            fragment = new NoticesFragment();
            // Register the AuthStateListener so the fragment knows when the token changes and
            // needs to refetch the content again or fetch for the first time (in case the token was expired
            // before the user enter in this fragment)
            mAuthStateListener = (NoticesFragment) fragment;
            mCurrentFragmentTag = NoticesFragment.TAG;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_content, fragment, NoticesFragment.TAG);
            fragmentTransaction.commit();
        } else {
            // If we happen to fall here it means the activity is recreating (orientation changes, etc)
            // to be able to listen for again onAuthChange we need to register the listener only if the
            // current fragment is the NoticesFragment
            NoticesFragment noticesFragment = (NoticesFragment) getSupportFragmentManager()
                    .findFragmentByTag(NoticesFragment.TAG);
            if (noticesFragment != null) {
                mAuthStateListener = noticesFragment;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SettingsUtils.eraseTemporarySettings(this);
        if (SettingsUtils.isFirstStart(this)) {
            showNotificationDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillingHelper != null) {
            mBillingHelper.dispose();
        }
        AndroidUtilities.sOnUpdateListener = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the action bar menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Now we want too show different items for each fragment
        // so lets check here which one do we need to show
        if (mCurrentFragmentTag == null) {
            return super.onPrepareOptionsMenu(menu);
        }

        boolean isMainFragment    = mCurrentFragmentTag.equals(NoticesFragment.TAG);
        boolean isBiddingFragment = mCurrentFragmentTag.equals(MyNoticesFragment.TAG);
        boolean isAccountFragment = mCurrentFragmentTag.equals(AccountFragment.TAG);

        MenuItem searchItem   = menu.findItem(R.id.action_search);
        MenuItem filterItem   = menu.findItem(R.id.action_filter);
        MenuItem refreshItem  = menu.findItem(R.id.action_refresh);
        MenuItem settingsItem = menu.findItem(R.id.action_settings);

        refreshItem.setVisible(isMainFragment);
        filterItem.setVisible(isMainFragment);
        searchItem.setVisible(isMainFragment);
        settingsItem.setVisible(isAccountFragment);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {

            return true;
        } else if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_refresh) {
            return handleRefreshForFragment();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, com.tnmlicitacoes.app.settings.SettingsActivity.class);
            startActivity(intent);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle refresh menu item for the fragment
     * @return true if it can be handled false if not
     */
    private boolean handleRefreshForFragment() {

        NoticesFragment noticesFragment = (NoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(NoticesFragment.TAG);
        if (noticesFragment != null) {
            NoticeTabFragment noticeTabFragment = noticesFragment.getCurrentTabFragment();
            if (noticeTabFragment != null) {
                noticeTabFragment.refreshData();
                return true;
            }
        }

        // Put handlers for other fragments here if needed!

        return false;
    }

    private void showNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notification_dialog_title);
        builder.setMessage(R.string.notification_dialog_message);
        builder.setPositiveButton(R.string.dialog_okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingsUtils.activateNotifications(MainActivity.this);
                SettingsUtils.putBoolean(MainActivity.this, SettingsUtils.PREF_IS_FIRST_START, false);
            }
        });

        builder.setNegativeButton(R.string.dialog_no_thanks, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SettingsUtils.putBoolean(MainActivity.this, SettingsUtils.PREF_IS_FIRST_START, false);
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SettingsUtils.putBoolean(MainActivity.this, SettingsUtils.PREF_IS_FIRST_START, false);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showExpiredTrialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.trial_dialog_title);
        builder.setMessage(R.string.trial_dialog_message);
        builder.setPositiveButton(R.string.go_to_subs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BillingUtils.sIsTrialActive = false;
                Intent i = new Intent(MainActivity.this, BillingActivity.class);
                i.putExtra("IS_CHANGING_SUBSCRIPTION", true);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        builder.setNegativeButton(R.string.dialog_no_thanks, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                BillingUtils.sIsTrialActive = false;
                SettingsUtils.erasePreference(MainActivity.this);
                finish();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                BillingUtils.sIsTrialActive = false;
                SettingsUtils.erasePreference(MainActivity.this);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initViews() {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private void setupInAppBilling() {

        if(mBillingHelper == null) {

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Creating IAB helper.");
            mBillingHelper = new IabHelper(MainActivity.this, Utils.decode(Utils.BASE64_PIECES));

            mBillingHelper.enableDebugLogging(false);

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Starting setup.");
            try {
                mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    @Override
                    public void onIabSetupFinished(IabResult result) {
                        if(!result.isSuccess()) {
                            if(BuildConfig.DEBUG)
                                LOG_DEBUG(TAG, "Problem setting up in-app billing: " + result);
                            return;
                        }

                        if (mBillingHelper == null) return;

                        if(BuildConfig.DEBUG)
                            LOG_DEBUG(TAG, "Setup successful. Querying inventory.");
                        mBillingHelper.queryInventoryAsync(mGotInventoryListener);
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Query inventory finished.");

            if (mBillingHelper == null) return;

            if (result.isFailure()) {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "Failed to query inventory: " + result);
                return;
            }

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium plan?
            Purchase currentSubscription = null;
            Purchase legacySubPurchase = inventory.getPurchase(BillingUtils.SKU_SUBSCRIPTION_LEGACY);
            Purchase basicSubPurchase = inventory.getPurchase(BillingUtils.SKU_SUBSCRIPTION_BASIC);
            Purchase defaultSubPurchase = inventory.getPurchase(BillingUtils.SKU_SUBSCRIPTION_DEFAULT);
            Purchase unlimitedSubPurchase = inventory.getPurchase(BillingUtils.SKU_SUBSCRIPTION_PREMIUM);

            if(legacySubPurchase != null) {
                currentSubscription = legacySubPurchase;
            }
            else if(basicSubPurchase != null) {
                currentSubscription = basicSubPurchase;
            }
            else if (defaultSubPurchase != null) {
                currentSubscription = defaultSubPurchase;
            }
            else if (unlimitedSubPurchase != null) {
                currentSubscription = unlimitedSubPurchase;
            }

            String premiumSku;
            if(currentSubscription == null) {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "User does not have Premium subscriptions");

                long currentTime = System.currentTimeMillis();
                long registrationTime = SettingsUtils.getActivationDateFromPrefs(MainActivity.this);
                if(registrationTime == 0) {

                } else if((currentTime - registrationTime) / Utils.DAY_IN_MILLIS > 30) {
                    SettingsUtils.putBoolean(MainActivity.this, SettingsUtils.PREF_IS_TRIAL_EXPIRED, true);
                    showExpiredTrialDialog();
                } else {
                    if(BuildConfig.DEBUG) {
                        LOG_DEBUG(TAG, "Not in time to check...");
                    }

                    SettingsUtils.putString(MainActivity.this, SettingsUtils.PREF_BILLING_SUB_NAME, "Trial");
                    BillingUtils.SUBSCRIPTION_MAX_ITEMS = BillingUtils.SUBSCRIPTION_MAX_ITEMS_BASIC;
                }


            } else if(Utils.verifyDeveloperPayload(currentSubscription)){
                premiumSku = currentSubscription.getSku();
                SettingsUtils.putString(MainActivity.this, SettingsUtils.PREF_BILLING_STATE, premiumSku);
                Subscription subscription = BillingUtils.getSubscription(MainActivity.this, premiumSku);

                if(subscription != null) {
                    SettingsUtils.putString(MainActivity.this, SettingsUtils.PREF_BILLING_SUB_NAME, subscription.getName());
                    BillingUtils.SUBSCRIPTION_MAX_ITEMS = subscription.getQuantity();
                }
            }
        }
    };

    @Override
    public void onFilter(CharSequence constraint) {
//        NoticeTabFragment fragment = getCurrentFragment();
//        Filter filter = fragment.getAdapterFilter();
//        FilterUtils.NON_TEXT_FILTERING = true;
//        fragment.startAnimation();
//        filter.filter(constraint);
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update_dialog_title);
        builder.setMessage(R.string.update_dialog_message);
        builder.setPositiveButton(R.string.update_dialog_positive_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(Utils.createPlayStoreIntent(MainActivity.this));
            }
        });

        builder.setNegativeButton(R.string.update_dialog_negative_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onNewUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showUpdateDialog();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean result = false;

        // We need to remove the elevation from NoticesFragment and MyNoticesFragment
        // because it is overlapping their TabLayout.
        // We expand too when we change the fragments because we want our users to know where
        // they are.
        mAppBarLayout.setElevation(0);
        mAppBarLayout.setExpanded(true);

        // We remove the listener here because only the NoticesFragment needs it!
        mAuthStateListener = null;

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag(mCurrentFragmentTag);
        NoticesFragment noticesFragment = (NoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(NoticesFragment.TAG);
        MyNoticesFragment biddingsFragment = (MyNoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(MyNoticesFragment.TAG);
        AccountFragment accountFragment = (AccountFragment) getSupportFragmentManager()
                .findFragmentByTag(AccountFragment.TAG);

        boolean hideAndAdd = false;

        int id = item.getItemId();
        if (id == R.id.action_home) {
            if (noticesFragment == null) {
                noticesFragment = new NoticesFragment();
                hideAndAdd = true;
            }

            mAuthStateListener = noticesFragment;
            mCurrentFragmentTag = NoticesFragment.TAG;

            if (hideAndAdd) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .add(R.id.main_content, noticesFragment, NoticesFragment.TAG)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .show(noticesFragment)
                        .commit();
            }

            setupToolbar();
            result = true;

        } else if (id == R.id.action_my_biddings) {
            if (biddingsFragment == null) {
                biddingsFragment = new MyNoticesFragment();
                hideAndAdd = true;
            }

            mCurrentFragmentTag = MyNoticesFragment.TAG;
            if (hideAndAdd) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .add(R.id.main_content, biddingsFragment, MyNoticesFragment.TAG)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .show(biddingsFragment)
                        .commit();
            }

            setupToolbar("Minhas licitações");
            result = true;

        } else if (id == R.id.action_account) {
            mAppBarLayout.setElevation(AndroidUtilities.dp(MainActivity.this, 4));
            if (accountFragment == null) {
                accountFragment = new AccountFragment();
                hideAndAdd = true;
            }

            mCurrentFragmentTag = AccountFragment.TAG;
            if (hideAndAdd) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .add(R.id.main_content, accountFragment, AccountFragment.TAG)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .show(accountFragment)
                        .commit();
            }

            setupToolbar("Conta");
            result = true;
        }

        return result;
    }
}
