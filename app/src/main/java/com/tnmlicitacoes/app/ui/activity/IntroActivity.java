package com.tnmlicitacoes.app.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.billing.BillingActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

public class IntroActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ViewGroup mSliderPages;
    private TextView mStartUsingText;
    private ImageView mIntroImage;

    private int[] mImageRes = {
            R.drawable.intro_image0,
            R.drawable.intro_image1,
            R.drawable.intro_image2,
            R.drawable.intro_image3,
            R.drawable.intro_image4
    };

    private int[] mHeaderTexts = {
            R.string.introHeader0,
            R.string.introHeader1,
            R.string.introHeader2,
            R.string.introHeader3,
            R.string.introHeader4
    };

    private int[] mContentTexts = {
            R.string.introText0,
            R.string.introText1,
            R.string.introText2,
            R.string.introText3,
            R.string.introText4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLoggedIn
                = preferences.getBoolean(SettingsUtils.PREF_USER_IS_LOGGED, false);
        boolean isInitialConfigFinished
                = preferences.getBoolean(SettingsUtils.PREF_INITIAL_CONFIG_IS_FINISHED, false);
        boolean isTrialExpired
                = preferences.getBoolean(SettingsUtils.PREF_IS_TRIAL_EXPIRED, false);

        if(isLoggedIn && isInitialConfigFinished) {
            goToActivity(MainActivity.class);
            return;
        }
        else if (isLoggedIn && !isTrialExpired) {
            BillingUtils.sIsTrialActive = true;
            goToActivity(BillingActivity.class);
            return;
        }
        else if (isLoggedIn) {
            goToActivity(AccountConfigurationActivity.class);
            return;
        }

        setContentView(R.layout.activity_intro);

        // Only portrait screen mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        IntroPagerAdapter introPager =  new IntroPagerAdapter();
        mViewPager = (ViewPager) findViewById(R.id.introViewPager);
        mSliderPages = (ViewGroup) findViewById(R.id.botSlidePages);
        mStartUsingText = (TextView) findViewById(R.id.startUsingText);
        mIntroImage = (ImageView) findViewById(R.id.introImage);

        mViewPager.setAdapter(introPager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
                    int currentPage = mViewPager.getCurrentItem();
                    mIntroImage.setImageResource(mImageRes[currentPage]);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(mStartUsingText, "translationZ", 4, 4).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(mStartUsingText, "translationZ", 4, 2).setDuration(200));
            mStartUsingText.setStateListAnimator(animator);
        }

        mStartUsingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, VerifyNumberActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
            }
        });
    }

    private void goToActivity(Class<?> clazz) {

        SettingsUtils.putBoolean(this, SettingsUtils.PREF_INTRO_VIEWED, true);

        Intent i = AndroidUtilities.getInstance(this).createClearStackIntent(clazz);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
    }

    private class IntroPagerAdapter extends PagerAdapter {

        public IntroPagerAdapter() {

        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(container.getContext(), R.layout.view_intro, null);
            TextView tvHeader = (TextView) view.findViewById(R.id.headerText);
            TextView tvContent = (TextView) view.findViewById(R.id.contentText);
            container.addView(view, 0);

            tvHeader.setText(getString(mHeaderTexts[position]));
            tvContent.setText(getString(mContentTexts[position]));

            return view;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            int pages = mSliderPages.getChildCount();
            for (int p = 0; p < pages; p++) {
                View childLayout = mSliderPages.getChildAt(p);
                if (p == position) {
                    childLayout.setBackgroundResource(R.drawable.active_page);
                } else {
                    childLayout.setBackgroundResource(R.drawable.inactive_page);
                }
            }
        }
    }
}
