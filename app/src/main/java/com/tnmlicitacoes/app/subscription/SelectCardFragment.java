package com.tnmlicitacoes.app.subscription;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.stripe.android.model.Card;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SupplierCardsQuery;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.ui.adapter.PaymentAdapter;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import static android.app.Activity.RESULT_OK;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.SAVE_CARD_REQUEST;
import static com.tnmlicitacoes.app.subscription.SubscriptionActivity.sSubscriptionProcess;

public class SelectCardFragment extends SubscriptionFragment implements
        SubscriptionActivity.SubscriptionFragmentContent, OnClickListenerRecyclerView {

    private static final String TAG = "SelectCardFragment";

    /* Displays the payment list */
    private RecyclerView mRecyclerView;

    /* The adapter */
    private PaymentAdapter mPaymentAdapter;

    /* The application singleton */
    private TnmApplication mApplication;

    /* The supplier cards call */
    private ApolloQueryCall<SupplierCardsQuery.Data> mSupplierCardsCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mApplication = (TnmApplication) mActivity.getApplication();
        View v = inflater.inflate(R.layout.fragment_select_card, container, false);
        initViews(v);
        fetchCards();
        return v;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSupplierCardsCall != null) {
            mSupplierCardsCall.cancel();
        }
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    private void initViews(View view) {
        mPaymentAdapter = new PaymentAdapter();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(),
                R.drawable.notice_item_divider));
        mRecyclerView.setAdapter(mPaymentAdapter);
        mPaymentAdapter.setOnItemClickListener(this);
    }

    private void fetchCards() {
        SupplierCardsQuery supplierCards = SupplierCardsQuery.builder().build();
        mSupplierCardsCall = mApplication.getApolloClient()
                .query(supplierCards)
                .cacheControl(CacheControl.CACHE_FIRST);
        mSupplierCardsCall.enqueue(cardsDataCallback);
    }

    /* The supplier cards callback */
    private ApolloCall.Callback<SupplierCardsQuery.Data> cardsDataCallback = new ApolloCall.Callback<SupplierCardsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<SupplierCardsQuery.Data> response) {
            if (!response.hasErrors() && response.data() != null
                    && response.data().supplier() != null) {
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUi(response.data().supplier());
                        }
                    });
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {

        }
    };

    @Override
    public String getTitle() {
        return getString(R.string.title_activity_subscription_select_card);
    }

    @Override
    public String getBottomText() {
        if (sSubscriptionProcess.hasPickedCard()) {
            return "Cartão: " + sSubscriptionProcess.getCardLast4();
        }
        return "Selecione um cartão para continuar";
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return !sSubscriptionProcess.hasPickedCard();
    }

    @Override
    public String getButtonText() {
        return getString(R.string.advance_button_text);
    }

    @Override
    public void onAdvanceButtonClick() {
        if (mSubscriptionListener != null) {
            mSubscriptionListener.onAdvanceButtonClicked();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SAVE_CARD_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                if (extras != null & !extras.isEmpty()) {
                    String id = extras.getString(SaveCardActivity.CARD_ID);
                    String brand = extras.getString(SaveCardActivity.CARD_BRAND);
                    String last4 = extras.getString(SaveCardActivity.CARD_LAST4);
                    String expMonth = extras.getString(SaveCardActivity.CARD_EXPIRY_MONTH);
                    String expYear = extras.getString(SaveCardActivity.CARD_EXPIRY_YEAR);

                    // Create card
                    Card card = new Card(null, Integer.parseInt(expMonth), Integer.parseInt(expYear),
                            null, null, null, null, null, null, null, null, brand, last4, null, null,
                            null, null, id);

                    mPaymentAdapter.add(card, mPaymentAdapter.getItemCount());
                    mPaymentAdapter.setDefaultCard(id);
                }
            }
        }
    }

    @Override
    public void OnClickListener(View v, int position) {
        Card card = mPaymentAdapter.getItem(position);
        if (card != null) {
            if (mSubscriptionListener != null) {
                mSubscriptionListener.onCardSelected(card);
            }
        } else {
            startActivityForResult(new Intent(mActivity, SaveCardActivity.class),
                    SAVE_CARD_REQUEST);
        }
    }

    private void updateUi(SupplierCardsQuery.Supplier supplier) {
        List<Card> cardList = new ArrayList<>();
        for (SupplierCardsQuery.Card card : supplier.cards()) {
            int expMonth = Integer.parseInt(card.exp_month());
            int expYear = Integer.parseInt(card.exp_year());
            cardList.add(new Card(null, expMonth, expYear, null, null, null, null,
                    null, null, null, null, card.brand(), card.last4(), null, null, null, null,
                    card.id()));
        }
        mPaymentAdapter.setDefaultCard(supplier.defaultCard());
        mPaymentAdapter.setPaymentList(cardList);
    }
}
