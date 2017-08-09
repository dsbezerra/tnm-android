package com.tnmlicitacoes.app.registration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Patterns
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCallback
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.evernote.android.state.StateSaver
import com.squareup.haha.perflib.Main
import com.tnmlicitacoes.app.R
import com.tnmlicitacoes.app.accountconfiguration.SelectCityFragment
import com.tnmlicitacoes.app.accountconfiguration.SelectSegmentFragment
import com.tnmlicitacoes.app.apollo.*
import com.tnmlicitacoes.app.apollo.type.SupplierInput
import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener
import com.tnmlicitacoes.app.main.MainActivity
import com.tnmlicitacoes.app.model.SubscriptionPlan
import com.tnmlicitacoes.app.model.realm.LocalSupplier
import com.tnmlicitacoes.app.model.realm.PickedCity
import com.tnmlicitacoes.app.model.realm.PickedSegment
import com.tnmlicitacoes.app.registration.RegistrationFragment.OnRegistrationListener
import com.tnmlicitacoes.app.search.SearchActivity
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity
import com.tnmlicitacoes.app.utils.*
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionManager
import io.realm.Realm
import java.util.*
import javax.annotation.Nonnull

class RegistrationActivity : BaseAuthenticatedActivity(), OnRegistrationListener, OnAccountConfigurationListener {

    override fun getLogTag(): String {
        return SearchActivity.TAG
    }

    override fun onFinishRegistration(status: Boolean) {
        setNextButtonEnabled(status)

        // TODO(diego): Update key
        SettingsUtils.putBoolean(this, SettingsUtils.PREF_INITIAL_CONFIG_IS_FINISHED, true)

        updateUiAccordinglyWithFragment(getFragmentFromTag(ConclusionFragment.TAG))
    }

    override fun onStartRegistration() {
        setNextButtonEnabled(false)

        // Start request
        val supplier = SupplierInput.builder()
        if (mUserName != null) {
            supplier.name(mUserName)
        }
        val updateSupplier = UpdateSupplierMutation.builder()
                .supplier(supplier
                        .deviceId(AndroidUtilities.getDeviceToken())
                        .build())
                .build()
        mUpdateSupplierCall = mApplication.apolloClient
                .mutate(updateSupplier)
        mUpdateSupplierCall.enqueue(updateSupplierCallback)
    }

    override fun onCompleteInitialisation(tag: String) {
        val fragment = getFragmentFromTag(tag)
        if (fragment != null) {
            updateBottomText(fragment as RegistrationContent)
        }
    }

    /**
     * Callback for when a city is selected
     * Updates the selected text on success and shou a dialog on failure
     * Failure only happens when the user cannot add more cities because of his subscription limit
     * @param newCount count of selected cities and -1 on failure
     */
    override fun onCitySelected(newCount: Int, city: CitiesQuery.Node?) {
        // Let's update the selected text in the bottom of view
        // and add to database
        if (newCount >= 0 && city != null) {
            // Persist if is a new selected. Remove if is already persisted
            val resultCity = mRealm.where(PickedCity::class.java).equalTo("id", city.id()).findFirst()
            if (resultCity == null) {
                // Create realm object
                val pickedCity = PickedCity(city.id(), city.name(), city.state()!!.name)
                // Persist realm object
                mRealm.beginTransaction()
                mRealm.copyToRealm(pickedCity)
                mRealm.commitTransaction()
                setNextButtonEnabled(true)
            } else {
                mRealm.beginTransaction()
                resultCity.deleteFromRealm()
                mRealm.commitTransaction()
            }
        } else if (newCount < 0) {
            // If we fall here, it means the user can't select more cities
            // let's show a dialog explaining why he can't add more cities
            // TODO(diego): Explaining dialog
            Toast.makeText(this, "Limite excedido!", Toast.LENGTH_SHORT).show()
        }

        setNextButtonEnabled(newCount > 0)

        val fragment = getFragmentFromTag(SelectCityFragment.TAG)
        if (fragment != null) {
            updateBottomText(fragment as RegistrationContent)
        }
    }

    /**
     * Callback for when a segment is selected
     * Updates the selected text on success and shou a dialog on failure
     * Failure only happens when the user cannot add more segments because of his subscription limit
     * @param newCount count of selected segments and -1 on failure
     */
    override fun onSegmentSelected(newCount: Int, segment: SegmentsQuery.Node?) {
        // Let's update the selected text in the bottom of view
        // and add to database
        if (newCount >= 0 && segment != null) {
            // Persist if is a new selected. Remove if is already persisted
            val resultSegment = mRealm.where(PickedSegment::class.java).equalTo("id", segment.id()).findFirst()
            if (resultSegment == null) {
                // Create realm object
                val pickedSegment = PickedSegment(segment.id(), segment.name())
                // Persist realm object
                mRealm.beginTransaction()
                mRealm.copyToRealm(pickedSegment)
                mRealm.commitTransaction()
            } else {
                mRealm.beginTransaction()
                resultSegment.deleteFromRealm()
                mRealm.commitTransaction()
            }
        } else if (newCount < 0) {
            // If we fall here, it means the user can't select more segments
            // let's show a dialog explaining why he can't add more segments
            // TODO(diego): Explaining dialog
            Toast.makeText(this, "Limite excedido!", Toast.LENGTH_SHORT).show()
        }

        setNextButtonEnabled(newCount > 0)

        val fragment = getFragmentFromTag(SelectSegmentFragment.TAG)
        if (fragment != null) {
            updateBottomText(fragment as RegistrationContent)
        }
    }

    /** The realm instance */
    private lateinit var mRealm: Realm

    /** Container */
    private var mCoordinatorLayout: CoordinatorLayout? = null

    /** Current fragment indicator */
    private var mCurrent: String? = null
    private var mCurrentIndex: Int = 0;
    /** List of fragments */
    private var mFragments: List<RegistrationContent>? = null;
    private var mFragmentsToDisplay: List<String> = ArrayList<String>();

    /** The bottom container views */
    private var mBottomContainer: LinearLayout? = null
    private var mInfoText: TextView? = null
    private var mAdvanceButton: Button? = null

    /** The collapsible toolbar */
    private var mCollapsibleToolbar: CollapsingToolbarLayout? = null

    /** The app bar */
    private var mAppBar: AppBarLayout? = null

    /** The view description */
    private var mDescription: TextView? = null

    /** Data */
    private var mUserName: String? = null
    private var mUserEmail: String? = null

    private var mSupplier: LocalSupplier? = null
    private var mSupplierCall: ApolloQueryCall<SupplierQuery.Data>? = null

    /** UI handler */
    private var mUiHandler = Handler(Looper.getMainLooper())

    /** The apollo update supplier calls */
    private lateinit var mUpdateSupplierCall: ApolloCall<UpdateSupplierMutation.Data>
    private lateinit var mUpdateSupplierEmailCall: ApolloCall<UpdateSupplierEmailMutation.Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRealm = Realm.getDefaultInstance()
        StateSaver.restoreInstanceState(this, savedInstanceState)
        setContentView(R.layout.activity_registration)
        initViews()

        setupToolbar(null)

        try {
            mSupplier = mRealm.where(LocalSupplier::class.java).findFirst()
        } catch (e: Exception) {
            // Ignore
        }

        // Add first fragment if we are starting for the first time
        if (savedInstanceState == null) {
            setNextButtonEnabled(false)

            //
            // Fetch supplier remote information if we don't have one in database
            // so we can know which views we should display or not.
            //
            if (mSupplier == null) {
                val supplierQuery = SupplierQuery.builder()
                        .build()

                mSupplierCall = mApplication.apolloClient
                        .query(supplierQuery)

                mSupplierCall!!.enqueue(ApolloCallback(object:ApolloCall.Callback<SupplierQuery.Data>() {
                    override fun onResponse(response: Response<SupplierQuery.Data>) {
                        if (!response.hasErrors()) {
                            updateLocalSupplier(response.data()?.supplier())
                            mFragmentsToDisplay = getFragmentsToDisplay(response.data()?.supplier());
                            if (mFragmentsToDisplay.isNotEmpty()) {
                                mCurrent = mFragmentsToDisplay[mCurrentIndex]
                                supportFragmentManager
                                        .beginTransaction()
                                        .add(R.id.container, getFragmentFromTag(mCurrent), mCurrent)
                                        .commit()
                                updateUiAccordinglyWithFragment(getFragmentFromTag(mCurrent))
                            } else {
                                val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            }

                        } else {
                            finish()
                        }
                    }
                    override fun onFailure(@Nonnull e:ApolloException) {
                    }

                }, mUiHandler))
            } else {
                mFragmentsToDisplay = getFragmentsToDisplay(mSupplier)
                if (mFragmentsToDisplay.isNotEmpty()) {
                    mCurrent = mFragmentsToDisplay[mCurrentIndex]
                    supportFragmentManager
                            .beginTransaction()
                            .add(R.id.container, getFragmentFromTag(mCurrent), mCurrent)
                            .commit()
                    updateUiAccordinglyWithFragment(getFragmentFromTag(mCurrent))
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }

        } else {
            mCurrent = savedInstanceState.getString(CURRENT)
            mCurrentIndex = getIndexFromTag(mCurrent)

            updateUiAccordinglyWithFragment(getFragmentFromTag(mCurrent))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    override fun onBackPressed() {
        val previous = getPreviousFragment()
        if (previous != null && mCurrent != ConclusionFragment.TAG) {
            showPreviousFragment(previous)
        } else if (mCurrent == ConclusionFragment.TAG){
            // Do nothing
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(CURRENT, mCurrent)
    }

    private fun getFragmentsToDisplay(supplier: Any?): List<String> {
        val result = ArrayList<String>();
        // This order here doesn't really matter, but let's keep
        // the registration process as Name -> Email -> Pick Cities -> Pick Segments
        if (supplier is LocalSupplier) {
            if (supplier.name == null) {
                result.add(InputNameFragment.TAG)
            }

            if (supplier.email == null) {
                result.add(InputEmailFragment.TAG)
            }

            if (supplier.cities == null || supplier.cities.isEmpty()) {
                result.add(SelectCityFragment.TAG)
            }

            if (supplier.segments == null || supplier.segments.isEmpty()) {
                result.add(SelectSegmentFragment.TAG)
            }
        } else if (supplier is ConfirmCodeMutation.Supplier) {
            if (supplier.name() == null) {
                result.add(InputNameFragment.TAG)
            }

            if (supplier.email() == null) {
                result.add(InputEmailFragment.TAG)
            }

            if (supplier.cities() == null || supplier.cities()?.edges()?.size == 0) {
                result.add(SelectCityFragment.TAG)
            }

            if (supplier.segments() == null || supplier.segments()?.edges()?.size == 0) {
                result.add(SelectSegmentFragment.TAG)
            }
        }

        // If we show at least one fragment let's show the conclusion fragment...
        if (result.size != 0) {
            result.add(ConclusionFragment.TAG)
        }

        return result;
    }

    private fun initViews() {
        mCoordinatorLayout = findViewById(R.id.root) as CoordinatorLayout
        mBottomContainer = findViewById(R.id.bottom_container) as LinearLayout
        mInfoText = findViewById(R.id.info_text) as TextView
        mAdvanceButton = findViewById(R.id.next_button) as Button
        mAppBar = findViewById(R.id.app_bar_layout) as AppBarLayout
        mCollapsibleToolbar = findViewById(R.id.collapse_toolbar) as CollapsingToolbarLayout
        mDescription = findViewById(R.id.description) as TextView
        mAdvanceButton?.setOnClickListener { handleAdvanceClick() }
    }

    private fun handleAdvanceClick() {

        val previous = mCurrent
        val currentFragment = getFragmentFromTag(mCurrent) as RegistrationContent;

        // Hide keyboard if we have a view with using it
        AndroidUtilities.hideKeyboard(currentFragment.getFocusView())

        when (mCurrent) {

            InputNameFragment.TAG -> {
                SettingsUtils.putString(this, SettingsUtils.PREF_USER_NAME, mUserName)
                mCurrent = getNextFragment()
            }

            InputEmailFragment.TAG -> {
                if (mSupplier?.email != mUserEmail) {
                    setNextButtonEnabled(false)
                    val updateSupplierEmail = UpdateSupplierEmailMutation.builder()
                            .email(mUserEmail!!)
                            .build()
                    mUpdateSupplierEmailCall = mApplication.apolloClient
                            .mutate(updateSupplierEmail)
                    mUpdateSupplierEmailCall.enqueue(updateSupplierEmailCallback)
                } else {
                    SettingsUtils.putString(this, SettingsUtils.PREF_KEY_USER_DEFAULT_EMAIL, mUserEmail)
                    mCurrent = getNextFragment()
                }
            }

            SelectCityFragment.TAG -> {
                // TODO(diego): Save picked cities
                mCurrent = getNextFragment()
            }

            SelectSegmentFragment.TAG -> {
                // TODO(diego): Save picked segments
                mCurrent = getNextFragment()
            }

            ConclusionFragment.TAG -> {

                // Go to main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        if (previous != mCurrent) {
            showNextFragment(previous, mCurrent, false)
        }
    }

    private fun getCurrentFragment(): RegistrationContent? {
        if (mFragments == null) {
            mFragments = getFragments()
        }

        for (i in 0..mFragments!!.size - 1) {
            val fragment = mFragments!![i]
            if (fragment.shouldDisplay(mSupplier)) {
                return fragment
            }
        }

        return null;
    }

    private fun getNextFragment(): String? {
        val next = mCurrentIndex + 1;
        if (next >= 0 && next < mFragmentsToDisplay.size) {
            mCurrentIndex += 1
            return mFragmentsToDisplay[next]
        }

        return null
    }

    private fun getPreviousFragment(): String? {
        val previous = mCurrentIndex - 1;
        if (previous >= 0 && previous < mFragmentsToDisplay.size) {
            mCurrentIndex -= 1
            return mFragmentsToDisplay[previous]
        }
        return null
    }

    private fun getIndexFromTag(tag: String?): Int {
        if (tag == null) {
            return -1
        }

        for ((index, value) in mFragmentsToDisplay.withIndex()) {
            if (value == tag) {
                return index
            }
        }

        return -1
    }

    private fun getFragments(): List<RegistrationContent> {
        return Arrays.asList(
                InputNameFragment(),
                InputEmailFragment(),
                SelectCityFragment(),
                SelectSegmentFragment()
        );
    }

    /**
     * Callback for the updateSupplier mutation call
     */
    private val updateSupplierCallback = ApolloCallback(object:ApolloCall.Callback<UpdateSupplierMutation.Data>() {
        override fun onResponse(@Nonnull response:Response<UpdateSupplierMutation.Data>) {
            val fragment = getFragmentFromTag(ConclusionFragment.TAG) as ConclusionFragment
            if (!response.hasErrors()) {
                updateSupplier(response.data()?.updateSupplier())
                fragment.setIsPreparing(false, true)
            } else {
                // TODO(diego): Handle errors
                LogUtils.LOG_DEBUG(TAG, ApiUtils.getFirstValidError(this@RegistrationActivity,
                        response.errors()).message)
                fragment.setIsPreparing(false, false)
            }
        }
        override fun onFailure(@Nonnull e:ApolloException) {
            // TODO(diego): Handle exception
            LogUtils.LOG_DEBUG(TAG, e.message)
        }
    }, mUiHandler)

    /**
     * Callback for the updateSupplierEmail mutation call
     */
    private val updateSupplierEmailCallback = ApolloCallback(object:ApolloCall.Callback<UpdateSupplierEmailMutation.Data>() {
        override fun onResponse(@Nonnull response:Response<UpdateSupplierEmailMutation.Data>) {
            var currentFragment = supportFragmentManager.findFragmentByTag(mCurrent)
            if (!response.hasErrors()) {
                mSupplier?.email = mUserEmail
                SettingsUtils.putString(this@RegistrationActivity, SettingsUtils.PREF_KEY_USER_DEFAULT_EMAIL, mUserEmail)
                val previous = mCurrent
                mCurrent = SelectCityFragment.TAG
                showNextFragment(previous, mCurrent, false)
            } else {
                val error = ApiUtils.getFirstValidError(this@RegistrationActivity, response.errors())
                when (error.code) {
                    ApiUtils.ErrorCode.INVALID_OWNER -> {
                        (currentFragment as RegistrationContent)
                                .setError("Este e-mail já está em uso!")
                    }
                    else -> {
                        LogUtils.LOG_DEBUG(TAG, error.message)
                    }
                }
            }
        }
        override fun onFailure(@Nonnull e:ApolloException) {
            // TODO(diego): Handle exception
            LogUtils.LOG_DEBUG(TAG, e.message)
        }
    }, mUiHandler)

    /**
     * TODO(diego): Replace these two with only one
     */
    private fun updateSupplier(supplier: UpdateSupplierMutation.UpdateSupplier?) {
        // TODO(diego): Add any missing fields
        var local = LocalSupplier()
        local.id = supplier?.id()
        local.isActivated = supplier?.activated()!!
        if (supplier.cityNum() != null) {
            local.cityNum = supplier.cityNum()!!
        } else {
            local.cityNum = SubscriptionPlan.BASIC_MAX_QUANTITY
        }

        if (supplier.segNum() != null) {
            local.segNum = supplier.segNum()!!
        } else {
            local.segNum = SubscriptionPlan.BASIC_MAX_QUANTITY
        }
        local.phone = supplier.phone()
        local.defaultCard = supplier.defaultCard()

        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(local)
        mRealm.commitTransaction()
    }

    // Go to next fragment
    private fun advance() {
    }

    /**
     * Sets the enabled state of the next button
     */
    private fun setNextButtonEnabled(value: Boolean) {
        mAdvanceButton?.alpha = if (value) 1.0f else 0.5f
        mAdvanceButton?.isClickable = value
    }

    /**
     * Show the next fragment to be displayed
     */
    private fun showNextFragment(current: String?, next: String?, reverseAnimation: Boolean = false) {
        var currentFragment = supportFragmentManager.findFragmentByTag(current)
        var nextFragment = supportFragmentManager.findFragmentByTag(next)

        var hideAndAdd = false
        if (nextFragment == null) {
            nextFragment = getFragmentFromTag(next)
            hideAndAdd = true
        }

        var enter = R.anim.fragment_slide_right_enter;
        var exit = R.anim.fragment_slide_left_exit;
        if (reverseAnimation) {
            enter = R.anim.fragment_slide_left_enter;
            exit = R.anim.fragment_slide_right_exit;
        }

        if (hideAndAdd) {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(enter, exit)
                    .hide(currentFragment)
                    .add(R.id.container, nextFragment, next)
                    .addToBackStack(null)
                    .commit()
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(enter, exit)
                    .hide(currentFragment)
                    .show(nextFragment)
                    .commit()
        }

        mCurrent = next
        updateUiAccordinglyWithFragment(nextFragment)
    }

    private fun updateUiAccordinglyWithFragment(nextFragment: Fragment?) {
        val content = (nextFragment as RegistrationContent)

        TransitionManager.beginDelayedTransition(mToolbar, Slide(Gravity.LEFT))

        // Display back arrow if needed
        supportActionBar?.setDisplayHomeAsUpEnabled(content.shouldDisplayBackArrow() && mCurrentIndex != 0)
        mAppBar?.setExpanded(true, true)
        if (content.shouldCollapseToolbarOnScroll()) {
            (mCollapsibleToolbar?.layoutParams as AppBarLayout.LayoutParams)
                    .scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP)
        } else {
            (mCollapsibleToolbar?.layoutParams as AppBarLayout.LayoutParams).scrollFlags = 0
        }

        val appCollapsibleHeight = content.getToolbarHeight(this)
        mCollapsibleToolbar?.minimumHeight = appCollapsibleHeight
        mCollapsibleToolbar?.title = content.getTitle()
        mDescription?.text = content.getDescription()

        // Force the redraw of the coordinator layout so the scrolling behavior works
        mCoordinatorLayout?.post { mCoordinatorLayout?.requestLayout() }

        updateBottomText(content)
    }

    /**
     * Updates the bottom text with content info
     */
    private fun updateBottomText(content: RegistrationContent) {
        TransitionManager.beginDelayedTransition(mBottomContainer);
        mInfoText?.text = content.getBottomInfoText()
        mInfoText?.visibility = if (content.getBottomInfoText() != null) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Show previous fragment relative to the current one
     */
    private fun showPreviousFragment(previous: String) {
        showNextFragment(mCurrent, previous, true)
    }

    /**
     * Gets a fragment instance for a given tag
     */
    private fun getFragmentFromTag(tag: String?): Fragment? {

        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            return fragment
        }

        when (tag) {
            InputNameFragment.TAG ->
                return InputNameFragment()

            InputEmailFragment.TAG ->
                return InputEmailFragment()

            SelectSegmentFragment.TAG ->
                return SelectSegmentFragment()

            SelectCityFragment.TAG ->
                return SelectCityFragment()

            ConclusionFragment.TAG ->
                return ConclusionFragment()
        }

        return null
    }

    override fun onNameChanged(name: String) {
        mUserName = name
        setNextButtonEnabled(!TextUtils.isEmpty(name))
    }

    override fun onEmailChanged(email: String) {
        mUserEmail = email
        setNextButtonEnabled(Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    fun updateLocalSupplier(supplier: SupplierQuery.Supplier?) {
        if (mSupplier == null) {
            mSupplier = LocalSupplier()
            mSupplier?.id = supplier?.id()
        }

        if (mSupplier?.id == supplier?.id()) {
            mRealm.beginTransaction()
            mSupplier?.createdAt = DateUtils.parse(supplier?.createdAt().toString())
            mSupplier?.name = supplier?.name()
            mSupplier?.email = supplier?.email()

            if (supplier?.cityNum() != null) {
                mSupplier?.cityNum = supplier.cityNum()!!
            } else {
                mSupplier?.cityNum = 1
            }

            if (supplier?.segNum() != null) {
                mSupplier?.segNum = supplier.segNum()!!
            } else {
                mSupplier?.segNum = 1
            }

            mSupplier?.isActivated = supplier?.activated()!!
            mSupplier?.isActiveSubscription = supplier.activeSubscription()!!
            mSupplier?.phone = supplier.phone()
            mSupplier?.defaultCard = supplier.defaultCard()

            if (supplier.currentPeriodStart() != null) {
                mSupplier?.currentPeriodStart = DateUtils.parse(supplier.currentPeriodStart()!!.toString())
            }

            if (supplier.currentPeriodEnd() != null) {
                mSupplier?.currentPeriodEnd = DateUtils.parse(supplier.currentPeriodEnd()!!.toString())
            }

            if (supplier.subscriptionStatus() != null) {
                mSupplier?.subscriptionStatus = supplier.subscriptionStatus()
            }

            if (supplier.cancelAtPeriodEnd() != null) {
                mSupplier?.isCancelAtPeriodEnd = supplier.cancelAtPeriodEnd()!!
            }

            mRealm.copyToRealmOrUpdate(mSupplier)
            mRealm.commitTransaction()
        }
    }


    interface RegistrationContent {

        /**
         * Whether this fragment should be displayed or not
         */
        fun shouldDisplay(localSupplier: LocalSupplier?): Boolean {
            return true
        }

        /**
         * Sets the error to the input field
         */
        fun setError(message: String)

        /**
         * Which view to focus
         */
        fun getFocusView(): View? {
            return null
        }

        /**
         * Whether the fragment should display or not the back arrow
         */
        fun shouldDisplayBackArrow(): Boolean {
            return false
        }

        /**
         * The view title
         */
        fun getTitle(): String? {
            return null
        }

        /**
         * The view description
         */
        fun getDescription(): String? {
            return null
        }

        /**
         * The toolbar height
         */
        fun getToolbarHeight(activity: Activity): Int {
            return 0
        }

        /**
         * Should collapse toolbar on scroll
         */
        fun shouldCollapseToolbarOnScroll(): Boolean {
            return false
        }

        /**
         * The bottom info text
         */
        fun getBottomInfoText(): String? {
            return null
        }
    }

    companion object {

        /* The logging tag */
        val TAG = "RegistrationActivity"

        /** The current fragment key */
        private val CURRENT = "current"
    }
}
