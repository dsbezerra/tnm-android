package com.tnmlicitacoes.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.fcm.RegistrationIntentService;
import com.tnmlicitacoes.app.model.Segment;
import com.tnmlicitacoes.app.ui.adapter.NoticeViewPagerAdapter;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String STATE_ACTIVE_TAB = "activeTab";

    private NoticeViewPagerAdapter mViewPagerAdapter;

    private List<Segment> mSegments = new ArrayList<>();

    private TabLayout mTabs;

    private ViewPager mViewPager;

    private Realm mRealm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        initViews(v);
        setupDatabaseData(savedInstanceState);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save active tab so it can be restored
        savedInstanceState.putInt(STATE_ACTIVE_TAB, mViewPager.getCurrentItem());

        LOG_DEBUG(TAG, "Saved tab: " + mViewPager.getCurrentItem());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SettingsUtils.needToUpdateTopics(getContext())) {
            setupDatabaseData(null);
            initGcm();
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
        setupTabsAndViewPager(savedInstanceState);
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

    private void setupTabsAndViewPager(Bundle savedInstanceState) {
        mViewPagerAdapter = new NoticeViewPagerAdapter(getFragmentManager(), mSegments);
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
                    //mAppBarLayout.setExpanded(true, true);
                }
            }
        });
        mTabs.setupWithViewPager(mViewPager);

        if (savedInstanceState != null) {
            final int lastActiveTab = savedInstanceState.getInt(STATE_ACTIVE_TAB);
            LOG_DEBUG(TAG, "Restored tab: " + lastActiveTab);
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(lastActiveTab);
                }
            });
        }
    }



    private void initGcm() {
        if (!SettingsUtils.isTokenInServer(getContext())) {
            getActivity().startService(new Intent(getContext(), RegistrationIntentService.class));
        } else if (SettingsUtils.needToUpdateTopics(getContext())) {
            getActivity().startService(new Intent(getContext(), RegistrationIntentService.class));
        }
    }
}
