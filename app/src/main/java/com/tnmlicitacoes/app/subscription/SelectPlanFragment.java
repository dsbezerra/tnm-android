package com.tnmlicitacoes.app.subscription;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.ui.adapter.SubscriptionPlanAdapter;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;

import static com.tnmlicitacoes.app.subscription.SubscriptionActivity.sSubscriptionProcess;

public class SelectPlanFragment extends SubscriptionFragment implements
        SubscriptionActivity.SubscriptionFragmentContent, OnClickListenerRecyclerView {

    private static final String TAG = "SelectPlanFragment";

    /* Displays the plan list */
    private RecyclerView mRecyclerView;

    /* The subscription plan adapter */
    private SubscriptionPlanAdapter mAdapter;

    /* The selected plan */
    private SubscriptionPlan mSelectedPlan;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_plan, container, false);
        initViews(v);
        return v;
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    private void initViews(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(),
                R.drawable.segment_item_divider));

        mAdapter = new SubscriptionPlanAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_activity_subscription_select_plan);
    }

    @Override
    public String getBottomText() {
        if (mAdapter != null && mAdapter.getSelected() != null) {
            return "Plano: " + mAdapter.getSelected().getName();
        }
        return "Selecione um plano para continuar";
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return !sSubscriptionProcess.hasPickedPlan();
    }

    @Override
    public String getButtonText() {
        return getString(R.string.advance_button_text);
    }

    @Override
    public void onAdvanceButtonClick() {
        if (mSubscriptionListener != null) {
            sSubscriptionProcess.setPlan(mSelectedPlan);
            mSubscriptionListener.onAdvanceButtonClicked();
        }
    }

    @Override
    public void OnClickListener(View v, int position) {
        SubscriptionPlan plan = mAdapter.getItem(position);
        if (plan != null) {
            if (mAdapter.setSelected(plan)) {
                if (mSubscriptionListener != null) {
                    mSelectedPlan = mAdapter.getSelected();
                    mSubscriptionListener.onPlanSelected(plan);
                }
                if (plan.isCustom()) {
                    mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                }

            } else {
                if (mSubscriptionListener != null) {
                    mSelectedPlan = null;
                    mSubscriptionListener.onPlanSelected(null);
                }
            }
        }
    }
}
