package com.tnmlicitacoes.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.adapter.MyBiddingsViewPagerAdapter;
import com.tnmlicitacoes.app.ui.fragment.BiddingsTabFragment;
import com.tnmlicitacoes.app.ui.fragment.FilesTabFragment;

import android.view.View;
import android.view.ViewGroup;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 *  MyBiddingsFragment
 *  Show the downloaded files in my notices view
 */

public class MyBiddingsFragment extends Fragment {

    private static final String TAG = "MyBiddingsFragment";

    private TabLayout mTabs;

    private ViewPager mViewPager;

    private MyBiddingsViewPagerAdapter mViewPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_biddings, container, false);
        initViews(v);
        return v;
    }

    private void initViews(View v) {
        mTabs = (TabLayout) v.findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) v.findViewById(R.id.viewPager);

        mViewPagerAdapter = new MyBiddingsViewPagerAdapter(getFragmentManager(), getContext());
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

                    }

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

    private Fragment getCurrentFragment() {
        return (Fragment) mViewPagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
