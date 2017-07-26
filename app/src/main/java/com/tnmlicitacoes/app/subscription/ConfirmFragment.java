package com.tnmlicitacoes.app.subscription;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SubscribeMutation;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.ui.widget.TnmCardInputWidget;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.ApiUtils;
import com.tnmlicitacoes.app.utils.StringUtils;

import javax.annotation.Nonnull;

import static com.tnmlicitacoes.app.model.SubscriptionPlan.ITEM_PRICE;
import static com.tnmlicitacoes.app.subscription.SubscriptionActivity.sSubscriptionProcess;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class ConfirmFragment extends SubscriptionFragment implements
        SubscriptionActivity.SubscriptionFragmentContent {

    /** The subscribe API call */
    private ApolloCall<SubscribeMutation.Data> mSubscribeCall;

    /** The progress dialog */
    private ProgressDialog mProgressDialog;

    /** The singleton application */
    private TnmApplication mApplication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mApplication = (TnmApplication) mActivity.getApplication();
        View view = inflater.inflate(R.layout.fragment_confirm, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscribeCall != null) {
            mSubscribeCall.cancel();
        }
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    private void initViews(View view) {
        if (sSubscriptionProcess == null) {
            return;
        }

        SubscriptionPlan plan = sSubscriptionProcess.getPlan();
        if (plan == null) {
            return;
        }

        // --------- Chosen plan section ----- //

        TextView chosenPlan = (TextView) view.findViewById(R.id.plan_name);
        TextView chosenDescription = (TextView) view.findViewById(R.id.plan_description);

        chosenPlan.setText(plan.getName());

        if (!plan.isCustom()) {
            chosenDescription.setText(plan.getDescription());
        } else {
            int cityQuantity = plan.getCityQuantity();
            int segmentQuantity = plan.getSegmentQuantity();
            if (cityQuantity > 1 && segmentQuantity > 1) {
                chosenDescription.setText(getString(R.string.plan_custom_both_plural_description,
                        segmentQuantity, cityQuantity));
            } else if (cityQuantity > 1) {
                chosenDescription.setText(getString(R.string.plan_custom_city_plural_description,
                        cityQuantity));
            } else if (segmentQuantity > 1) {
                chosenDescription.setText(getString(R.string.plan_custom_segment_plural_description,
                        segmentQuantity));
            } else {
                chosenDescription.setText(getString(R.string.plan_basic_description));
            }
        }

        // --------- Payment method section ----- //

        String brand = sSubscriptionProcess.getCardBrand();
        String last4 = sSubscriptionProcess.getCardLast4();
        View paymentMethod = view.findViewById(R.id.payment_method);
        ImageView brandIv = (ImageView) paymentMethod.findViewById(R.id.card_brand);
        TextView numberTv = (TextView) paymentMethod.findViewById(R.id.card_number);

        brandIv.setImageResource(TnmCardInputWidget.BRAND_RESOURCE_MAP.get(brand));
        numberTv.setText(getString(R.string.card_number_last_digits, last4));

        // --------- Total section ----- //

        TextView segmentQuantityTv = (TextView) view.findViewById(R.id.segment_quantity);
        TextView cityQuantityTv = (TextView) view.findViewById(R.id.city_quantity);
        TextView itemPriceXMultiplier = (TextView) view.findViewById(R.id.item_price_x_total);
        TextView planPrice = (TextView) view.findViewById(R.id.plan_price);

        int cityQuantity = plan.getCityQuantity();
        int segmentQuantity = plan.getSegmentQuantity();
        segmentQuantityTv.setText(String.valueOf(segmentQuantity));
        cityQuantityTv.setText(String.valueOf(cityQuantity));

        itemPriceXMultiplier.setText(getString(R.string.item_price_x_total,
                StringUtils.getPriceInBrazil(ITEM_PRICE), segmentQuantity + cityQuantity));
        planPrice.setText(getString(R.string.plan_price,
                StringUtils.getPriceInBrazil(plan.getPrice())));
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_activity_subscription_confirm);
    }

    @Override
    public String getBottomText() {
        return null;
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return sSubscriptionProcess.isConfirming();
    }

    @Override
    public String getButtonText() {
        return getString(R.string.confirm_button_text);
    }

    @Override
    public void onAdvanceButtonClick() {
        SubscriptionPlan plan = sSubscriptionProcess.getPlan();
        String cardId = sSubscriptionProcess.getCardId();
        if (plan != null && cardId != null) {
            mProgressDialog = AndroidUtilities.createProgressDialog(mActivity,
                    "Aguarde um momento...", true, false);
            mProgressDialog.show();

            SubscribeMutation subscribe = SubscribeMutation.builder()
                    .segmentsQuantity(plan.getSegmentQuantity())
                    .citiesQuantity(plan.getCityQuantity())
                    .cardId(cardId)
                    .build();

            mSubscribeCall = mApplication.getApolloClient()
                    .mutate(subscribe);
            mSubscribeCall.enqueue(new ApolloCall.Callback<SubscribeMutation.Data>() {

                @Override
                public void onResponse(@Nonnull Response<SubscribeMutation.Data> response) {

                    mProgressDialog.dismiss();
                    mProgressDialog = null;

                    if (!response.hasErrors()) {

                        // TODO(diego): Dialog or a more elaborated message
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Assinatura efetuada com sucesso!", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        });

                        LOG_DEBUG(TAG, "Subscribed!");
                    } else {
                        ApiUtils.ApiError error = ApiUtils.getFirstValidError(mActivity,
                                response.errors());

                        if (error != null) {
                            LOG_DEBUG(TAG, error.isFromResources() ?
                                    getString(error.getMessageRes()) : error.getMessage());
                        }

                    }
                }

                @Override
                public void onFailure(@Nonnull ApolloException e) {
                    LOG_DEBUG(TAG, e.getMessage());
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            });

        }
    }
}
