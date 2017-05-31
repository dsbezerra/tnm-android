package com.tnmlicitacoes.app.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;

import com.evernote.android.state.State;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.adapter.MyBiddingsViewPagerAdapter;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.ui.fragment.BiddingsTabFragment;
import com.tnmlicitacoes.app.ui.fragment.FilesTabFragment;

import android.view.View;
import android.view.ViewGroup;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 *  MyNoticesFragment
 *  Show the downloaded files in my notices view
 */

public class MyNoticesFragment extends BaseFragment {

    public static final String TAG = "MyNoticesFragment";

    private TabLayout mTabs;

    private ViewPager mViewPager;

    private MyBiddingsViewPagerAdapter mViewPagerAdapter;

    @State
    int mCurrentItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_notices, container, false);
        initViews(v);
        return v;
    }

    private void initViews(View v) {
        mTabs = (TabLayout) v.findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) v.findViewById(R.id.viewPager);

        mViewPagerAdapter = new MyBiddingsViewPagerAdapter(getChildFragmentManager(), getContext());
        mViewPager.setAdapter(mViewPagerAdapter);
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
                    mCurrentItem = mViewPager.getCurrentItem();
                    Fragment currentFragment = getCurrentFragment();
                    if (currentFragment instanceof BiddingsTabFragment) {
                        // Empty
                    }
                    else if (currentFragment instanceof FilesTabFragment) {
                        FilesTabFragment fragment = (FilesTabFragment) currentFragment;
                        fragment.loadFiles();
                    }

                }
            }
        });

        mViewPager.setCurrentItem(mCurrentItem);

        mTabs.setupWithViewPager(mViewPager);
        mTabs.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabs.setTabMode(TabLayout.MODE_FIXED);
    }

    private Fragment getCurrentFragment() {
        return (Fragment) mViewPagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }
}
