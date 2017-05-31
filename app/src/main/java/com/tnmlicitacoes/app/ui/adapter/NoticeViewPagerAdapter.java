package com.tnmlicitacoes.app.ui.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.tnmlicitacoes.app.model.Segment;
import com.tnmlicitacoes.app.ui.fragment.NoticeTabFragment;

import java.util.List;

public class NoticeViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "NoticeViewPagerAdapter";

    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

    private List<Segment> mSegments;

    private int mTabsNum;

    public NoticeViewPagerAdapter(FragmentManager fm, List<Segment> segments) {
        super(fm);
        this.mSegments = segments;
        this.mTabsNum = segments.size();
    }

    @Override
    public Fragment getItem(int position) {
        NoticeTabFragment noticeTab = new NoticeTabFragment();
        Bundle b = new Bundle();
        b.putString("tabName", mSegments.get(position).getName());
        b.putString("segId", mSegments.get(position).getId());
        noticeTab.setArguments(b);
        mRegisteredFragments.put(position, noticeTab);
        return noticeTab;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mSegments.get(position).getName();
    }

    @Override
    public int getCount() {
        return mTabsNum;
    }

    public Fragment getFragment(int position) {
        return mRegisteredFragments.get(position);
    }
}
