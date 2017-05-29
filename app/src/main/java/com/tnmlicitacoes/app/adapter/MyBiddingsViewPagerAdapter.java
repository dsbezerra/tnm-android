package com.tnmlicitacoes.app.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.fragment.BiddingsTabFragment;
import com.tnmlicitacoes.app.ui.fragment.FilesTabFragment;

public class MyBiddingsViewPagerAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;

    private final int mTabsCount = 2;

    private final int[] mTabTitles = {
            R.string.tab_biddings,
            R.string.tab_files
    };

    public MyBiddingsViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        if(position == 0) {
            fragment = new BiddingsTabFragment();
        }
        else if(position == 1) {
            fragment = new FilesTabFragment();
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return mTabsCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(mTabTitles[position]);
    }
}
