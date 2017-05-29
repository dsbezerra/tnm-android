package com.tnmlicitacoes.app.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.billing.BillingActivity;
import com.tnmlicitacoes.app.billing.IabHelper;
import com.tnmlicitacoes.app.billing.IabResult;
import com.tnmlicitacoes.app.billing.Inventory;
import com.tnmlicitacoes.app.billing.Purchase;
import com.tnmlicitacoes.app.domain.Subscription;
import com.tnmlicitacoes.app.fcm.RegistrationIntentService;
import com.tnmlicitacoes.app.interfaces.GlobalApplicationListener;
import com.tnmlicitacoes.app.interfaces.OnFilterClickListener;
import com.tnmlicitacoes.app.model.Segment;
import com.tnmlicitacoes.app.ui.adapter.NoticeViewPagerAdapter;
import com.tnmlicitacoes.app.ui.fragment.NoticeTabFragment;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.FilterUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class MainActivity extends BaseBottomNavigationActivity implements OnFilterClickListener, GlobalApplicationListener {

    private static final String TAG = "MainActivity";

    private List<Segment> mSegments = new ArrayList<>();

    private NoticeViewPagerAdapter mViewPagerAdapter;

    private TabLayout mTabs;

    private AppBarLayout mAppBarLayout;

    private ViewPager mViewPager;

    private IabHelper mBillingHelper;

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // We need  to put this before the super because the super gets a reference
        // from the xml layout
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();
        AndroidUtilities.sGlobalApplicationListener = this;
        setupToolbar();
        setupDatabaseData();
        initViews();
        setupInAppBilling();
        if(SettingsUtils.getNewestVersionCode(this) > BuildConfig.VERSION_CODE) {
            showUpdateDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SettingsUtils.eraseTemporarySettings(this);
        if(SettingsUtils.isFirstStart(this)) {
            showNotificationDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SettingsUtils.needToUpdateTopics(this)) {
            setupDatabaseData();
            initGcm();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AndroidUtilities.sGlobalApplicationListener = null;
        mRealm.close();
    }

    private void initGcm() {
        if (!SettingsUtils.isTokenInServer(this)) {
            startService(new Intent(this, RegistrationIntentService.class));
        } else if (SettingsUtils.needToUpdateTopics(this)) {
            startService(new Intent(this, RegistrationIntentService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        boolean isPremiumSubscription = SettingsUtils.getBillingState(this).equals(BillingUtils.SKU_SUBSCRIPTION_PREMIUM);
        if(isPremiumSubscription) {
            final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView;
            final MenuItem item = menu.findItem(R.id.action_search);
            searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setQueryHint(getString(R.string.buscarHint));

            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup.LayoutParams lp = v.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
//                    NoticeTabFragment f = getCurrentFragment();
//                    FilterUtils.NON_TEXT_FILTERING = false;
//                    f.getAdapterFilter().filter(newText.toUpperCase());
                    return true;
                }
            });

        } else {
            MenuItem item = menu.findItem(R.id.action_search);
            item.setEnabled(false);
            item.setVisible(false);
        }

        // TODO(diego): Make a proper filter
        MenuItem item = menu.findItem(R.id.action_filter);
        item.setVisible(false);
        item.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {

            return true;
        } else if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.menu_refresh) {
            NoticeTabFragment fragment = getCurrentFragment();
            fragment.refreshData();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * Setup a categories array used in the view pager for namimg tabs
     */
    private void setupSegmentTabs() {

        LOG_DEBUG(TAG, "Starting setup of segment tabs.");

        RealmResults<Segment> segments = mRealm.where(Segment.class).findAll();
        if (segments.size() == 0) {
            LOG_DEBUG(TAG, "Zero segments in database.");
            // TODO(diego): send back to configuration
        }

        // Sorting by name because we want the items sorted...
        segments = segments.sort("name");

        mSegments = new ArrayList<>(segments);

        if(mSegments.size() == 1) {
            mTabs.setTabMode(TabLayout.MODE_FIXED);
            mTabs.setTabGravity(TabLayout.GRAVITY_FILL);
        } else {
            mTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            mTabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        LOG_DEBUG(TAG, "Finished setting up segments tabs.");
    }

    /**
     * Initialization of views
     */
    private void initViews() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTabs = (TabLayout) findViewById(R.id.tabLayout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
    }

    /**
     * Setup categories tabs and locations
     */
    private void setupDatabaseData() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                setupSegmentTabs();
                setupTabsAndViewPager();
            }
        });
    }

    private void setupTabsAndViewPager() {
        mViewPagerAdapter = new NoticeViewPagerAdapter(getSupportFragmentManager(), mSegments);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
                    mAppBarLayout.setExpanded(true, true);
                }
            }
        });
        mTabs.setupWithViewPager(mViewPager);
    }

    private NoticeTabFragment getCurrentFragment() {
        return (NoticeTabFragment) mViewPagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }

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
}
