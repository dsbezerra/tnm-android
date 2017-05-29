package com.tnmlicitacoes.app.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.materialize.util.UIUtils;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.adapter.MyBiddingsViewPagerAdapter;
import com.tnmlicitacoes.app.interfaces.FileViewListener;
import com.tnmlicitacoes.app.ui.fragment.BiddingsTabFragment;
import com.tnmlicitacoes.app.ui.fragment.FilesTabFragment;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 *  MyBiddingsActivity
 *  Show the downloaded files in my notices view
 */

public class MyBiddingsActivity extends BaseBottomNavigationActivity implements FileViewListener {

    private static final String TAG = "MyBiddingsActivity";

    private AppBarLayout mAppBarLayout;

    private TabLayout mTabs;

    private ViewPager mViewPager;

    private MyBiddingsViewPagerAdapter mViewPagerAdapter;

    private ActionMode mActionMode;

    private int mSelectedCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // We need  to put this before the super because the super gets a reference
        // from the xml layout
        setContentView(R.layout.activity_my_biddings);
        super.onCreate(savedInstanceState);
        setupToolbar();
        initViews();
    }

    private void initViews() {
        mTabs = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

        mViewPagerAdapter = new MyBiddingsViewPagerAdapter(getSupportFragmentManager(), this);
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
                    if (mViewPager.getCurrentItem() != 1) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                    }

                    expandToolbar(true);

                    Fragment currentFragment = getCurrentFragment();
                    if(currentFragment instanceof BiddingsTabFragment) {
                        if(BuildConfig.DEBUG)
                            LOG_DEBUG(TAG, "BiddingsTab");
                    }
                    else if (currentFragment instanceof FilesTabFragment) {
                        if(BuildConfig.DEBUG)
                            LOG_DEBUG(TAG, "FilesTab");
                        FilesTabFragment fragment = (FilesTabFragment) currentFragment;
                        fragment.loadFiles();
                    }

                }
            }
        });

        mTabs.setupWithViewPager(mViewPager);
        mTabs.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabs.setTabMode(TabLayout.MODE_FIXED);
    }

    public void expandToolbar(boolean expanded) {
        mAppBarLayout.setExpanded(expanded, true);
    }

    private Fragment getCurrentFragment() {
        return (Fragment) mViewPagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onActionModeListener(boolean actionMode) {
        if(actionMode && mViewPager.getCurrentItem() == 1) {
            expandToolbar(true);
            mActionMode = startSupportActionMode(new ActionBarCallBack());
        } else {
            if(mActionMode != null) {
                mActionMode.finish();
            }
        }
    }

    @Override
    public void onUpdateActionModeTitle(int selectedSize) {
        mSelectedCount = selectedSize;
        if(selectedSize == 0) {
            updateActionModeTitle("");
        } else {
            updateActionModeTitle(selectedSize + " selecionado(s)");
        }

        if(mActionMode != null) {
            mActionMode.invalidate();
        }
    }

    private void updateActionModeTitle(String title) {
        if(mActionMode != null) {
            mActionMode.setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if(mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        Fragment currentFragment = getCurrentFragment();
        if(currentFragment instanceof FilesTabFragment) {
            FilesTabFragment fragment = (FilesTabFragment) currentFragment;
            fragment.setActionModeEnabled(false);
        }

        super.onSupportActionModeFinished(mode);
    }

    private class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            int currentItem = mViewPager.getCurrentItem();
            if(currentItem == 1) {
                FilesTabFragment fragment = (FilesTabFragment) mViewPagerAdapter.instantiateItem(mViewPager, currentItem);
                if(item.getItemId() == R.id.item_delete) {
                    fragment.startDeleteTask();
                    return true;
                } else if (item.getItemId() == R.id.item_select_all) {
                    fragment.setAllSelected();
                    return true;
                } else if (item.getItemId() == R.id.item_select_none){
                    fragment.deselectAll();
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.my_files_action_mode, menu);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(UIUtils.getThemeColorFromAttrOrRes(MyBiddingsActivity.this, R.attr.colorActionModePrimaryDark, R.color.colorActionModePrimaryDark));
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(UIUtils.getThemeColorFromAttrOrRes(MyBiddingsActivity.this, R.attr.colorPrimaryDark, R.color.material_drawer_primary_dark));
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            MenuItem deleteItem = menu.getItem(0);
            MenuItem selectAllItem = menu.getItem(1);
            MenuItem selectNoneItem = menu.getItem(2);

            if(mSelectedCount == 0) {
                // Hide delete when none is selected
                deleteItem.setVisible(false).setEnabled(false);

                // Show select all when none is selected as action never
                selectAllItem.setVisible(true).setEnabled(true);
                MenuItemCompat.setShowAsAction(selectAllItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);

                // Hide select none when none is selected
                selectNoneItem.setVisible(false).setEnabled(false);
            } else if (mSelectedCount > 0) {

                // Show delete when at least one is selected
                deleteItem.setVisible(true).setEnabled(true);

                // Show select all when more than 0 is selected
                selectAllItem.setVisible(true).setEnabled(true);

                // Show select none when more than 0 is selected
                selectNoneItem.setVisible(true).setEnabled(true);
            }

            return false;
        }

    }
}
