package com.tnmlicitacoes.app.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.main.mynotices.BiddingsTabFragment;
import com.tnmlicitacoes.app.main.mynotices.DownloadedFragment;
import com.tnmlicitacoes.app.main.mynotices.MyNoticesFragment;

public class MyNoticesViewPagerAdapter extends FragmentStatePagerAdapter {

    /** The application context */
    private final Context mContext;

    /** Number of tabs */
    private static final int TAB_COUNT = 2;

    /** Tab titles */
    private final int[] mTabTitles = {
            R.string.tab_biddings,
            R.string.tab_files
    };

    public MyNoticesViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        if (position == 0) {
            fragment = new BiddingsTabFragment();
        }
        else if (position == 1) {
            fragment = new DownloadedFragment();
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(mTabTitles[position]);
    }
}
