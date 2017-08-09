package com.tnmlicitacoes.app.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.AuthStateListener;
import com.tnmlicitacoes.app.interfaces.FilterListener;
import com.tnmlicitacoes.app.interfaces.OnUpdateListener;
import com.tnmlicitacoes.app.main.account.AccountFragment;
import com.tnmlicitacoes.app.main.home.NoticeTabFragment;
import com.tnmlicitacoes.app.main.home.NoticesFragment;
import com.tnmlicitacoes.app.main.mynotices.DownloadedFragment;
import com.tnmlicitacoes.app.main.mynotices.MyNoticesFragment;
import com.tnmlicitacoes.app.search.SearchActivity;
import com.tnmlicitacoes.app.settings.SettingsActivity;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.HashMap;

public class MainActivity extends BaseAuthenticatedActivity implements FragmentManager.OnBackStackChangedListener,
        FilterListener, OnUpdateListener, BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    public String getLogTag() {
        return TAG;
    }

    /** The logging tag */
    private static final String TAG = "MainActivity";

    /** The current fragment content */
    private MainContent mFragment;

    /** Displays the bottom navigation menu */
    private BottomNavigationView mBottomNavigationView;

    /** Displays the app bar */
    private AppBarLayout mAppBarLayout;

    /* The action mode for the  MyNoticesFragment */
    private ActionMode mActionMode;

    private DialogFragment mFilterDialog;

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

        // First time the activity is created we start by showing the user
        // the NoticesFragment (which contains the segments tabs and notice lists)
        if (savedInstanceState == null) {
            mFragment = NoticesFragment.newInstance();
            // Register the AuthStateListener so the fragment knows when the token changes and
            // needs to refetch the content again or fetch for the first time (in case the token was expired
            // before the user enter in this fragment)
            mAuthStateListener = (NoticesFragment) mFragment;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_content, (Fragment) mFragment, NoticesFragment.TAG);
            fragmentTransaction.commit();
        } else {
            // If we happen to fall here it means the activity is recreating (orientation changes, etc)
            // to be able to listen for again onAuthChange we need to register the listener only if the
            // current fragment is the NoticesFragment
            mFragment = (NoticesFragment) getSupportFragmentManager()
                    .findFragmentByTag(NoticesFragment.TAG);
            if (mFragment != null) {
                mAuthStateListener = (AuthStateListener) mFragment;
            }
        }

        // Listens for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.LEFT);
            slide.excludeTarget(R.id.main_content, true);
            getWindow().setExitTransition(slide);
            getWindow().setAllowEnterTransitionOverlap(true);
        }

    }

    @Override
    public void onBackPressed() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        } else {
            super.onBackPressed();
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
        // so lets check here which one we need to show
        if (mFragment == null) {
            return super.onPrepareOptionsMenu(menu);
        }


        boolean isMainFragment      = mFragment instanceof NoticesFragment;
        //boolean isMyNoticesFragment = mCurrentFragmentTag.equals(MyNoticesFragment.Companion.getTAG());
        boolean isAccountFragment   = mFragment instanceof AccountFragment;

        MenuItem searchItem   = menu.findItem(R.id.action_search);
        //MenuItem filterItem   = menu.findItem(R.id.action_filter);
        MenuItem refreshItem  = menu.findItem(R.id.action_refresh);
        MenuItem settingsItem = menu.findItem(R.id.action_settings);

        refreshItem.setVisible(isMainFragment);
        // filterItem.setVisible(isMainFragment);
        searchItem.setVisible(isMainFragment);
        settingsItem.setVisible(isAccountFragment);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh) {
            return handleRefreshForFragment();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(NoticeFilterDialog.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        if (mFilterDialog == null) {
            mFilterDialog = new NoticeFilterDialog().withListener(this);
        }
        mFilterDialog.show(ft, NoticeFilterDialog.TAG);
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
    public void onFilter(HashMap<String, Object> filterParams) {
        if (filterParams != null) {
            NoticesFragment noticesFragment = (NoticesFragment) getSupportFragmentManager()
                    .findFragmentByTag(NoticesFragment.TAG);
            if (noticesFragment != null) {
                NoticeTabFragment noticeTabFragment = noticesFragment.getCurrentTabFragment();
                if (noticeTabFragment != null) {
                   noticeTabFragment.fetchNoticesFiltered(filterParams);
                }
            }
        }
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

        // If we already are in the view, ignore the execution of this method
        int id = item.getItemId();
        if (id == mBottomNavigationView.getSelectedItemId()) {
            return false;
        }

        // We remove the listener here because only the NoticesFragment needs it!
        // TODO(diego): Add to AccountFragment too
        mAuthStateListener = null;

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.main_content);
        NoticesFragment noticesFragment = (NoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(NoticesFragment.TAG);
        MyNoticesFragment myNoticesFragment = (MyNoticesFragment) getSupportFragmentManager()
                .findFragmentByTag(MyNoticesFragment.Companion.getTAG());
        AccountFragment accountFragment = (AccountFragment) getSupportFragmentManager()
                .findFragmentByTag(AccountFragment.TAG);

        boolean hideAndAdd = false;
        boolean shouldUpdateManually = false;

        if (id == R.id.action_home) {
            if (noticesFragment == null) {
                noticesFragment = NoticesFragment.newInstance();
                hideAndAdd = true;
            }

            mAuthStateListener = noticesFragment;

            if (hideAndAdd) {
                hideAndAddFragments(currentFragment, noticesFragment);
            } else {
                shouldUpdateManually = true;
                hideAndShowFragments(currentFragment, noticesFragment);
            }

            mFragment = noticesFragment;

            result = true;

        } else if (id == R.id.action_my_notices) {
            if (myNoticesFragment == null) {
                myNoticesFragment = new MyNoticesFragment();
                hideAndAdd = true;
            }
            if (hideAndAdd) {
                hideAndAddFragments(currentFragment, myNoticesFragment);
            } else {
                shouldUpdateManually = true;
                hideAndShowFragments(currentFragment, myNoticesFragment);
            }

            mFragment = myNoticesFragment;

            result = true;

        } else if (id == R.id.action_account) {
            if (accountFragment == null) {
                accountFragment = new AccountFragment();
                hideAndAdd = true;
            }
            if (hideAndAdd) {
                hideAndAddFragments(currentFragment, accountFragment);
            } else {
                shouldUpdateManually = true;
                hideAndShowFragments(currentFragment, accountFragment);
            }

            mAuthStateListener = accountFragment;

            mFragment = accountFragment;

            result = true;
        }

        // Updates bottom UI in cases when onBackStackChanged is not called
        if (shouldUpdateManually) {
            updateUiAccordinglyWithFragment(mFragment);
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
            hide.onHiddenChanged(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(hide)
                    .show(show)
                    .commit();
            show.onHiddenChanged(false);
        }
    }

    /**
     * Hide and add fragments
     * @param hide the fragment to be hidden
     * @param add the fragment to be added
     */
    private void hideAndAddFragments(Fragment hide, Fragment add) {
        if (hide != null && add != null) {
            hide.onHiddenChanged(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(hide)
                    .add(R.id.main_content, add)
                    .addToBackStack(null)
                    .commit();
            add.onHiddenChanged(false);
        }
    }

    /**
     * Gets the action mode instance
     * @return
     */
    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void initActionMode(DownloadedFragment.ActionModeCallback callback) {
        mActionMode = startSupportActionMode(callback);
    }

    /**
     * Finishes the action mode
     */
    public void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (current instanceof MyNoticesFragment) {
            MyNoticesFragment fragment = (MyNoticesFragment) current;
            fragment.getDownloadedFragment()
                    .setActionModeEnabled(false);
        }

        super.onSupportActionModeFinished(mode);
    }

    @Override
    public void onBackStackChanged() {
        updateUiAccordinglyWithFragment(null);
    }

    /**
     * Updates the UI according with fragment specific values
     */
    private void updateUiAccordinglyWithFragment(MainContent currentFragment) {

        if (currentFragment == null) {
            mFragment = (MainContent) getSupportFragmentManager()
                    .findFragmentById(R.id.main_content);
        }

        if (mFragment != null) {
            // Always expand the AppBar
            mAppBarLayout.setExpanded(true);

            // We displays the elevation only in the fragments that requires
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAppBarLayout.setElevation(mFragment.shouldDisplayElevationOnAppBar() ?
                        AndroidUtilities.dp(MainActivity.this, 4) : 0);
            }

            // We enable AppBar collapse on scroll only in fragments that requires
            ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams()).setScrollFlags(
                    mFragment.shouldCollapseAppBarOnScroll() ?
                            (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS) : 0
            );

            // We displays the correct title for the current fragment
            setupToolbar(mFragment.getTitle());

            // Highlight the current menu item in the bottom nav
            Menu bottomMenu = mBottomNavigationView.getMenu();
            MenuItem item = bottomMenu.findItem(mFragment.getMenuId());
            if (item != null) {
                item.setChecked(true);
            }

            supportInvalidateOptionsMenu();
        }

    }

    public interface MainContent {

        /**
         * Get menu item id for the fragment.
         */
        int getMenuId();

        /**
         * Gets the title for the fragment displayed.
         */
        String getTitle();

        /**
         * Whether the fragment requires elevation on the appbar or not
         */
        boolean shouldDisplayElevationOnAppBar();

        /**
         * Whether the fragment requires collapsible AppBar or not
         */
        boolean shouldCollapseAppBarOnScroll();
    }
}
