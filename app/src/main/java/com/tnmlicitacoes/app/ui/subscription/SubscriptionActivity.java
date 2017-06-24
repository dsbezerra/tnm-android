package com.tnmlicitacoes.app.ui.subscription;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.stripe.android.model.Card;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SubscribeMutation;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.OnSubscriptionListener;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.model.SubscriptionProcess;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.ApiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class SubscriptionActivity extends BaseAuthenticatedActivity implements OnSubscriptionListener {

    private static final String TAG = "SubscriptionActivity";

    /* Holds a list with the subscription content fragments */
    private List<SubscriptionFragmentContent> mFragments;

    /* Keeps track of the current fragment displayed */
    private SubscriptionFragmentContent mCurrentFragment = null;

    /* The subscription process object */
    public static SubscriptionProcess sSubscriptionProcess;

    /* Advance button */
    private Button mAdvanceButton;

    /* Bottom bar text */
    private TextView mBottomText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        if (savedInstanceState == null) {
            sSubscriptionProcess = new SubscriptionProcess();
        }

        mAdvanceButton = (Button) findViewById(R.id.advance_btn);
        mBottomText = (TextView) findViewById(R.id.text);

        showNextFragment();
        mAdvanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentFragment.onAdvanceButtonClick();
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (sSubscriptionProcess.isConfirming() || sSubscriptionProcess.hasPickedCard()) {
            sSubscriptionProcess.setConfirming(false);
            sSubscriptionProcess.clearCard();
        } else if (sSubscriptionProcess.hasPickedPlan()) {
            sSubscriptionProcess.clearPlan();
        } else {
            super.onBackPressed();
            return;
        }

        showNextFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the next fragment
     */
    private void showNextFragment() {
        mCurrentFragment = getCurrentFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, (Fragment) mCurrentFragment, "subscription_tag")
                .commitNow();

        // Only the ConfirmFragment that has the advance button enabled as default
        mAdvanceButton.setEnabled(mCurrentFragment instanceof ConfirmFragment);

        updateUi(false);
    }

    /**
     * Get the current fragment to display
     *
     * @return the current fragment
     */
    private SubscriptionFragmentContent getCurrentFragment() {
        if (mFragments == null) {
            mFragments = getFragments();
        }

        for (SubscriptionFragmentContent fragment : mFragments) {
            if (fragment.shouldDisplay(this)) {
                return fragment;
            }
        }

        return null;
    }

    /**
     * Get the two fragments used in SubscriptionActivity
     *
     * @return the List of fragments.
     */
    private List<SubscriptionFragmentContent> getFragments() {
        return new ArrayList<SubscriptionFragmentContent>(Arrays.asList(
                new SelectPlanFragment(),
                new SelectCardFragment(),
                new ConfirmFragment()
        ));
    }

    @Override
    public void onAdvanceButtonClicked() {
        if (mCurrentFragment instanceof SelectCardFragment) {
            sSubscriptionProcess.setConfirming(true);
        }

        showNextFragment();
    }

    @Override
    public void onCardSelected(Card card) {
        if (card != null) {
            mAdvanceButton.setEnabled(true);
            sSubscriptionProcess.setCard(card.getId(), card.getBrand(), card.getLast4());
        } else {
            mAdvanceButton.setEnabled(false);
        }
        updateUi(true);
    }

    @Override
    public void onPlanSelected(SubscriptionPlan plan) {
        if (plan != null) {
            if (plan.isCustom()) {
                boolean enabled = plan.getCityQuantity() != SubscriptionPlan.INVALID_QUANTITY &&
                        plan.getSegmentQuantity() != SubscriptionPlan.INVALID_QUANTITY;
                mAdvanceButton.setEnabled(enabled);
            } else {
                mAdvanceButton.setEnabled(true);
            }
        } else {
            mAdvanceButton.setEnabled(false);
        }
        updateUi(true);
    }

    public void updateUi(boolean onlyBottom) {
        mBottomText.setText(mCurrentFragment.getBottomText());
        if (onlyBottom) {
            return;
        }
        mAdvanceButton.setText(mCurrentFragment.getButtonText());
        setupToolbar(mCurrentFragment.getTitle());
    }

    public interface SubscriptionFragmentContent {

        /**
         * Return the title for the activity
         */
        String getTitle();

        /**
         * Get bottom text
         */
        String getBottomText();

        /**
         * Return true if the fragment should be displayed
         * @param context
         */
        boolean shouldDisplay(Context context);


        /**
         * Get advance button text
         */
        String getButtonText();

        /**
         * On advanced click action
         */
        void onAdvanceButtonClick();
    }
}
