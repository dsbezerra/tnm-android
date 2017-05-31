package com.tnmlicitacoes.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evernote.android.state.State;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.fcm.RegistrationIntentService;
import com.tnmlicitacoes.app.interfaces.AuthStateListener;
import com.tnmlicitacoes.app.model.Segment;
import com.tnmlicitacoes.app.ui.adapter.NoticeViewPagerAdapter;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.ui.fragment.NoticeTabFragment;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class NoticesFragment extends BaseFragment implements AuthStateListener {

    public static final String TAG = "NoticesFragment";

    private static final String STATE_ACTIVE_TAB = "activeTab";

    private NoticeViewPagerAdapter mViewPagerAdapter;

    private List<Segment> mSegments = new ArrayList<>();

    private TabLayout mTabs;

    private ViewPager mViewPager;

    private Realm mRealm;

    @State
    public int mCurrentItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        initViews(v);
        setupDatabaseData(savedInstanceState);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SettingsUtils.needToUpdateTopics(getContext())) {
            setupDatabaseData(null);
            initFCM();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    /**
     * Initialization of views
     */
    private void initViews(View v) {
        mViewPager = (ViewPager) v.findViewById(R.id.viewPager);
        mTabs = (TabLayout) v.findViewById(R.id.tabLayout);
    }

    /**
     * Setup categories tabs and locations
     */
    private void setupDatabaseData(Bundle savedInstanceState) {
        setupSegmentTabs();
        setupTabsAndViewPager();
    }

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

        if (mSegments.size() == 1) {
            mTabs.setTabMode(TabLayout.MODE_FIXED);
            mTabs.setTabGravity(TabLayout.GRAVITY_FILL);
        } else {
            mTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            mTabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        LOG_DEBUG(TAG, "Finished setting up segments tabs.");
    }

    private void setupTabsAndViewPager() {
        mViewPagerAdapter = new NoticeViewPagerAdapter(getChildFragmentManager(), mSegments);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(Math.min(mSegments.size(), 4));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mCurrentItem = mViewPager.getCurrentItem();
                LOG_DEBUG(TAG, "Currrent tab: " + mCurrentItem);
            }
        });

        mTabs.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(mCurrentItem);
        LOG_DEBUG(TAG, "Currrent tab: " + mCurrentItem);
    }

    private void initFCM() {
        if (!SettingsUtils.isTokenInServer(getContext())) {
            getActivity().startService(new Intent(getContext(), RegistrationIntentService.class));
        } else if (SettingsUtils.needToUpdateTopics(getContext())) {
            getActivity().startService(new Intent(getContext(), RegistrationIntentService.class));
        }
    }

    public NoticeTabFragment getCurrentTabFragment() {
        return (NoticeTabFragment) mViewPagerAdapter.instantiateItem(mViewPager,
                mViewPager.getCurrentItem());
    }

    @Override
    public void onAuthChanged() {
        NoticeTabFragment fragment = getCurrentTabFragment();
        if (fragment != null) {
            fragment.refreshData();
        }

        LOG_DEBUG(TAG, "onAuthChanged");
    }
}
