package com.tnmlicitacoes.app.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.ui.fragment.NoticeTabFragment;

import java.util.List;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class NoticeViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "NoticeViewPagerAdapter";

    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

    private List<PickedSegment> mSegments;

    private int mTabsNum;

    public NoticeViewPagerAdapter(FragmentManager fragmentManager, List<PickedSegment> segments) {
        super(fragmentManager);
        this.mSegments = segments;
        this.mTabsNum = segments.size();
    }

    @Override
    public Fragment getItem(int position) {
        PickedSegment segment = mSegments.get(position);

        NoticeTabFragment noticeTab = NoticeTabFragment.newInstance(segment);
        mRegisteredFragments.put(position, noticeTab);
        LOG_DEBUG(TAG, "Added tab " + segment.getName() + " at position " + position);
        return noticeTab;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position >= 0 && position < mRegisteredFragments.size()) {
            mRegisteredFragments.remove(position);
            LOG_DEBUG(TAG, "Removed tab at position " + position);
        }

        super.destroyItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position >= 0 && position < mSegments.size()) {
            return mSegments.get(position).getName();
        }

        return null;
    }

    @Override
    public int getCount() {
        return mTabsNum;
    }

    public Fragment getFragment(int position) {
        if (position >= 0 && position < mRegisteredFragments.size()) {
            return mRegisteredFragments.get(position);
        }

        return null;
    }

    public SparseArray<Fragment> getFragments() {
        return mRegisteredFragments;
    }
}
