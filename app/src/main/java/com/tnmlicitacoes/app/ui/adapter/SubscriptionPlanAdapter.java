package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.ui.subscription.SelectPlanFragment;
import com.transitionseverywhere.Rotate;
import com.transitionseverywhere.TransitionManager;

import java.util.ArrayList;
import java.util.List;

import me.himanshusoni.quantityview.QuantityView;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 * Created by diegobezerra on 6/16/17.
 */

public class SubscriptionPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* The logging tag */
    private static final String TAG = "SubscriptionPlanAdapter";

    /* Plan count */
    private static final int PLAN_COUNT = 3;

    /* For basic and default plan */
    private static final int VIEW_TYPE_PLAN = 0;

    /* For custom plan */
    private static final int VIEW_TYPE_CUSTOM_PLAN = 1;

    /* Selected plan */
    private SubscriptionPlan mSelected;

    /* Holds the plan list */
    private List<SubscriptionPlan> mPlanList;

    /* Plan list click listener */
    private OnClickListenerRecyclerView mClickListener;

    public SubscriptionPlanAdapter(final SelectPlanFragment fragment) {
        final Context context = fragment.getContext();
        mPlanList = new ArrayList<SubscriptionPlan>() {{
            add(SubscriptionPlan.getBasicPlan(context));
            add(SubscriptionPlan.getDefaultPlan(context));
            add(SubscriptionPlan.getCustomPlan(context));
        }};
        mClickListener = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder VH;

        if (viewType == VIEW_TYPE_CUSTOM_PLAN) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subscription_custom, parent, false);
            VH = new CustomSubscriptionPlanViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subscription, parent, false);
            VH = new PresetSubscriptionPlanViewHolder(v);
        }

        return VH;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SubscriptionPlan plan = getItem(position);
        if (plan == null) {
            return;
        }

        Context context = holder.itemView.getContext();

        // Get the colors
        int colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary);
        int backgroundColor = isSelected(plan) ? colorPrimary : 0xFFF2F2F2;
        int textColor = isSelected(plan) ? 0xFFFFFFFF : colorPrimary;
        int descColor = isSelected(plan) ? 0xFFFFFFFF : 0xFFA6A6A6;

        if (holder instanceof PresetSubscriptionPlanViewHolder) {
            PresetSubscriptionPlanViewHolder presetVH = (PresetSubscriptionPlanViewHolder) holder;
            presetVH.planName.setText(plan.getName());
            presetVH.planDescription.setText(plan.getDescription());

            presetVH.planPrice.setVisibility(plan.isCustom() ? View.GONE : View.VISIBLE);

            presetVH.planName.setTextColor(textColor);
            presetVH.planDescription.setTextColor(descColor);
            presetVH.planPrice.setTextColor(textColor);
            presetVH.planInterval.setTextColor(textColor);
            presetVH.planPrice.setText(context.getString(R.string.plan_price, plan.getPrice()));

            presetVH.itemView.setBackgroundColor(backgroundColor);

        } else if (holder instanceof CustomSubscriptionPlanViewHolder) {
            CustomSubscriptionPlanViewHolder customVH = (CustomSubscriptionPlanViewHolder) holder;
            customVH.planName.setTextColor(textColor);
            customVH.planDescription.setTextColor(descColor);
            customVH.container.setVisibility(isSelected(plan) ? View.VISIBLE : View.GONE);
            customVH.planPrice.setText(context.getString(R.string.plan_price, plan.getPrice()));
            customVH.segmentQuantity.setQuantity(plan.getSegmentQuantity());
            customVH.cityQuantity.setQuantity(plan.getCityQuantity());
            customVH.infoContainer.setBackgroundColor(backgroundColor);

            TransitionManager.beginDelayedTransition(customVH.infoContainer, new Rotate());
            customVH.arrow.setColorFilter(descColor);
            customVH.arrow.setRotation(isSelected(plan) ? 180.0f : 0.0f);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // -1 because we need to exclude the custom from the preset plans
        if (position >= 0 && position < PLAN_COUNT - 1) {
            return VIEW_TYPE_PLAN;
        } else {
            return VIEW_TYPE_CUSTOM_PLAN;
        }
    }

    /**
     * Get a subscription item from list at the given position
     */
    public SubscriptionPlan getItem(int position) {
        if (position >= 0 && position < mPlanList.size()) {
            return mPlanList.get(position);
        }

        return null;
    }

    /**
     * Sets the selected plan
     */
    public boolean setSelected(SubscriptionPlan plan) {
        boolean result = false;
        if (mSelected == null) {
            mSelected = plan;
            result = true;
        } else if (mSelected.getId().equals(plan.getId())) {
            mSelected = null;
        } else {
            mSelected = plan;
            result = true;
        }

        notifyDataSetChanged();

        return result;
    }

    /**
     * Sets the selected plan by position
     */
    public SubscriptionPlan setSelected(int position) {
        SubscriptionPlan plan = getItem(position);
        if (plan != null) {
            if (setSelected(plan)) {
               return plan;
            }
        }
        return null;
    }

    /**
     * Check if a given plan is selected or not
     */
    private boolean isSelected(SubscriptionPlan plan) {
        if (plan == null || mSelected == null) {
            return false;
        }

        return plan.getId().equals(mSelected.getId());
    }

    public boolean isSelected(int position) {
        return isSelected(getItem(position));
    }

    public SubscriptionPlan getSelected() {
        return mSelected;
    }

    @Override
    public int getItemCount() {
        return mPlanList.size();
    }

    public class PresetSubscriptionPlanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View itemView;
        private TextView planName;
        private TextView planDescription;
        private TextView planPrice;
        private TextView planInterval;

        public PresetSubscriptionPlanViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.planName = (TextView) itemView.findViewById(R.id.plan_name);
            this.planDescription = (TextView) itemView.findViewById(R.id.plan_description);
            this.planPrice = (TextView) itemView.findViewById(R.id.plan_price);
            this.planInterval = (TextView) itemView.findViewById(R.id.plan_interval);
            this.itemView.setClickable(true);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                mClickListener.OnClickListener(v, getAdapterPosition());
            }
        }
    }

    public class CustomSubscriptionPlanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View itemView;
        private ImageView arrow;
        private RelativeLayout infoContainer;
        private LinearLayout container;
        private TextView planName;
        private TextView planDescription;
        private TextView planPrice;
        private QuantityView segmentQuantity;
        private QuantityView cityQuantity;

        public CustomSubscriptionPlanViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.planName = (TextView) itemView.findViewById(R.id.plan_name);
            this.planDescription = (TextView) itemView.findViewById(R.id.plan_description);
            this.arrow = (ImageView) itemView.findViewById(R.id.arrow);
            this.infoContainer = (RelativeLayout) itemView.findViewById(R.id.info_container);
            this.container = (LinearLayout) itemView.findViewById(R.id.custom_container);
            this.planPrice = (TextView) itemView.findViewById(R.id.plan_price);
            this.segmentQuantity = (QuantityView) itemView.findViewById(R.id.segment_quantity);
            this.cityQuantity = (QuantityView) itemView.findViewById(R.id.city_quantity);
            this.infoContainer.setClickable(true);
            this.infoContainer.setOnClickListener(this);

            this.segmentQuantity.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
                @Override
                public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
                    SubscriptionPlan plan = getItem(getAdapterPosition());
                    if (plan != null && plan.isCustom()) {
                        plan.setSegmentQuantity(newQuantity);
                        notifyItemChanged(getAdapterPosition());
                    }
                }

                @Override
                public void onLimitReached() { }
            });

            this.cityQuantity.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
                @Override
                public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
                    SubscriptionPlan plan = getItem(getAdapterPosition());
                    if (plan != null && plan.isCustom()) {
                        plan.setCityQuantity(newQuantity);
                        notifyItemChanged(getAdapterPosition());
                    }
                }

                @Override
                public void onLimitReached() { }
            });
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                mClickListener.OnClickListener(v, getAdapterPosition());
            }
        }
    }
}
