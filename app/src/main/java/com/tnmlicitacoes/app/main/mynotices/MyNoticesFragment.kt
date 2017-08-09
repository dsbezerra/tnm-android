package com.tnmlicitacoes.app.main.mynotices

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tnmlicitacoes.app.R
import com.tnmlicitacoes.app.adapter.MyNoticesViewPagerAdapter
import com.tnmlicitacoes.app.interfaces.DownloadedFilesListener
import com.tnmlicitacoes.app.main.MainActivity
import com.tnmlicitacoes.app.ui.base.BaseFragment

class MyNoticesFragment : BaseFragment(), MainActivity.MainContent {
    override fun canScrollToTop(): Boolean {
        return false
    }

    override fun scrollToTop() {
        // TODO(diego): Get the current tab fragment list item and scroll to 0 position
    }

    override fun getLogTag(): String {
        return TAG
    }

    /* The tab layout instance */
    private lateinit var mTabLayout: TabLayout

    /* The tab layout view pager */
    private lateinit var mViewPager: ViewPager

    /* The view pager adapter */
    private lateinit var mViewPagerAdapter: MyNoticesViewPagerAdapter

    /* Tracks the current visible tab */
    private var mCurrentItem: Int = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_my_notices, container, false)

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt("current")
        }

        initViews(view)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("current", mCurrentItem)
    }

    private fun initViews(view: View?) {
        mTabLayout = view?.findViewById(R.id.tabLayout) as TabLayout
        mViewPager = view.findViewById(R.id.viewPager) as ViewPager

        mViewPagerAdapter = MyNoticesViewPagerAdapter(childFragmentManager, context)
        mViewPager.adapter = mViewPagerAdapter
        mViewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }

            override fun onPageSelected(position: Int) { }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
                    mCurrentItem = mViewPager.currentItem
                    val currentFragment = getCurrentFragment()
                    if (currentFragment is BiddingsTabFragment) {
                        // Empty
                    } else if (currentFragment is DownloadedFragment) {
                        val fragment = currentFragment
                        fragment.loadFiles()
                    }

                }
            }
        })

        mViewPager.currentItem = mCurrentItem

        mTabLayout.setupWithViewPager(mViewPager)
        mTabLayout.tabGravity = TabLayout.GRAVITY_FILL
        mTabLayout.tabMode = TabLayout.MODE_FIXED
    }

    private fun getCurrentFragment(): Fragment {
        return mViewPagerAdapter.instantiateItem(mViewPager, mCurrentItem) as Fragment
    }

    fun getDownloadedFragment(): DownloadedFragment {
        return mViewPagerAdapter.instantiateItem(mViewPager, DOWNLOADED_NOTICES) as DownloadedFragment
    }

    override fun getMenuId(): Int {
        return R.id.action_my_notices
    }

    override fun getTitle(): String? {
        if (isAdded) {
            return getString(R.string.title_activity_main_my_notices)
        }

        return null
    }

    override fun shouldDisplayElevationOnAppBar(): Boolean {
        return false
    }

    override fun shouldCollapseAppBarOnScroll(): Boolean {
        return true
    }

    companion object {

        /* The logging tag */
        val TAG = "MyNoticesFragment"

        /* The saved notices tab fragment */
        val SAVED_NOTICES = 0;

        /* The downloaded notices tab fragment */
        val DOWNLOADED_NOTICES = 1;
    }
}