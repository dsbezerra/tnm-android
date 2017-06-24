package com.tnmlicitacoes.app.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.evernote.android.state.State;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.billing.IabHelper;
import com.tnmlicitacoes.app.interfaces.OnFilterClickListener;
import com.tnmlicitacoes.app.interfaces.OnUpdateListener;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.ui.fragment.NoticeTabFragment;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

public class MainActivity extends BaseAuthenticatedActivity implements
        OnFilterClickListener, OnUpdateListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    @State
    public String mCurrentFragmentTag;

    private IabHelper mBillingHelper;

    private BottomNavigationView mBottomNavigationView;

    private AppBarLayout mAppBarLayout;

    private DrawerLayout mRightDrawer;

    private ListView mFilterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupToolbar();

        AndroidUtilities.sOnUpdateListener = this;
        if (SettingsUtils.getNewestVersionCode(this) > BuildConfig.VERSION_CODE) {
            showUpdateDialog();
        }

        Fragment fragment;
        // First time the activity is created we start by showing the user
        // the NoticesFragment (which contains the segments tabs and notice lists)
        if (savedInstanceState == null) {
            fragment = NoticesFragment.newInstance();
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

        boolean isMainFragment      = mCurrentFragmentTag.equals(NoticesFragment.TAG);
        boolean isMyNoticesFragment = mCurrentFragmentTag.equals(MyNoticesFragment.TAG);
        boolean isAccountFragment   = mCurrentFragmentTag.equals(AccountFragment.TAG);

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
//                BillingUtils.sIsTrialActive = false;
//                Intent i = new Intent(MainActivity.this, BillingActivity.class);
//                i.putExtra("IS_CHANGING_SUBSCRIPTION", true);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
//                finish();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBarLayout.setElevation(0);
        }
        mAppBarLayout.setExpanded(true);
        ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams()).setScrollFlags(
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        );

        // We remove the listener here because only the NoticesFragment needs it!
        mAuthStateListener = null;

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag(mCurrentFragmentTag);
        NoticesFragment noticesFragment = (NoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(NoticesFragment.TAG);
        MyNoticesFragment myNoticesFragment = (MyNoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(MyNoticesFragment.TAG);
        AccountFragment accountFragment = (AccountFragment) getSupportFragmentManager()
                .findFragmentByTag(AccountFragment.TAG);

        boolean hideAndAdd = false;

        int id = item.getItemId();
        if (id == R.id.action_home) {
            if (noticesFragment == null) {
                noticesFragment = NoticesFragment.newInstance();
                hideAndAdd = true;
            }

            mAuthStateListener = noticesFragment;
            mCurrentFragmentTag = NoticesFragment.TAG;

            if (hideAndAdd) {
                hideAndAddFragments(currentFragment, noticesFragment, NoticesFragment.TAG);
            } else {
                hideAndShowFragments(currentFragment, noticesFragment);
            }

            setupToolbar();
            result = true;

        } else if (id == R.id.action_my_notices) {
            if (myNoticesFragment == null) {
                myNoticesFragment = new MyNoticesFragment();
                hideAndAdd = true;
            }

            mCurrentFragmentTag = MyNoticesFragment.TAG;
            if (hideAndAdd) {
                hideAndAddFragments(currentFragment, myNoticesFragment, MyNoticesFragment.TAG);
            } else {
                hideAndShowFragments(currentFragment, myNoticesFragment);
            }

            setupToolbar(getString(R.string.title_activity_main_my_notices));
            result = true;

        } else if (id == R.id.action_account) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAppBarLayout.setElevation(AndroidUtilities.dp(MainActivity.this, 4));
            }
            ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams()).setScrollFlags(0);
            if (accountFragment == null) {
                accountFragment = new AccountFragment();
                hideAndAdd = true;
            }

            mCurrentFragmentTag = AccountFragment.TAG;
            if (hideAndAdd) {
                hideAndAddFragments(currentFragment, accountFragment, AccountFragment.TAG);
            } else {
                hideAndShowFragments(currentFragment, accountFragment);
            }

            setupToolbar(getString(R.string.title_activity_main_account));
            result = true;
        }

        return result;
    }

    /**
     * Hide and show fragments
     * @param hide the fragment to be hidden
     * @param show the fragment to be shown
     */
    private void hideAndShowFragments(Fragment hide, Fragment show) {
        if (hide != null && show != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(hide)
                    .show(show)
                    .commit();
        }
    }

    /**
     * Hide and add fragments
     * @param hide the fragment to be hidden
     * @param add the fragment to be added
     * @param fragmentTag the tag of the fragment to be added
     */
    private void hideAndAddFragments(Fragment hide, Fragment add, String fragmentTag) {
        if (fragmentTag != null && hide != null && add != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(hide)
                    .add(R.id.main_content, add, fragmentTag)
                    .commit();
        }
    }
}
