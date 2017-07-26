package com.tnmlicitacoes.app.subscription;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

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
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_BRAND;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_EXPIRY_MONTH;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_EXPIRY_YEAR;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_ID;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_IS_DEFAULT;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_LAST4;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.SAVE_CARD_REQUEST;

public class PaymentsActivity extends BaseAuthenticatedActivity implements OnClickListenerRecyclerView {

    @Override
    public String getLogTag() {
        return TAG;
    }

    private static final String TAG = "PaymentsActivity";

    /** The stripe publishable key used to create tokens for the cards
     *  TODO(diego): Don't forget to change to the LIVE KEY
     * */
    public static String STRIPE_PUBLISHABLE_KEY = "pk_test_B3xQ0W4SDhptORvW5CbSIeVL";

    /** Whether we need to refetch the cards or not */
    public static boolean sShouldRefetch = false;

    /** Displays the payment list */
    private RecyclerView mRecyclerView;

    /** Displays the progress bar */
    private ProgressBar mProgressBar;

    /** The adapter */
    private PaymentAdapter mPaymentAdapter;

    /** The application singleton */
    private TnmApplication mApplication;

    /** The supplier cards call */
    private ApolloQueryCall<SupplierCardsQuery.Data> mSupplierCardsCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (TnmApplication) getApplication();
        setContentView(R.layout.activity_payments);
        initViews();
        fetchCards();
        setupToolbar("Pagamento");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sShouldRefetch) {
            fetchCards();
        }
    }

    private void initViews() {
        mPaymentAdapter = new PaymentAdapter();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.payments_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, R.drawable.notice_item_divider));
        mRecyclerView.setAdapter(mPaymentAdapter);
        mPaymentAdapter.setOnItemClickListener(this);
    }

    private void fetchCards() {
        setIsLoading(true);
        SupplierCardsQuery supplierCards = SupplierCardsQuery.builder().build();
        mSupplierCardsCall = mApplication.getApolloClient()
                .query(supplierCards)
                .cacheControl(CacheControl.NETWORK_FIRST);
        mSupplierCardsCall.enqueue(cardsDataCallback);
    }

    /* The supplier cards callback */
    private ApolloCall.Callback<SupplierCardsQuery.Data> cardsDataCallback = new ApolloCall.Callback<SupplierCardsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<SupplierCardsQuery.Data> response) {
            if (!response.hasErrors() && response.data() != null
                    && response.data().supplier() != null) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUi(response.data().supplier());
                        setIsLoading(false);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO(diego): Show error
                        setIsLoading(false);
                    }
                });
            }

            sShouldRefetch = false;
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            sShouldRefetch = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setIsLoading(false);
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        if (mPaymentAdapter.getItem(position) != null) {
            Card card = mPaymentAdapter.getItem(position);
            Intent intent = new Intent(this, CardDetailsActivity.class);
            intent.putExtra(CARD_IS_DEFAULT, mPaymentAdapter.getDefaultCard().equals(card.getId()));
            intent.putExtra(CARD_ID, card.getId());
            intent.putExtra(CARD_LAST4, card.getLast4());
            intent.putExtra(CARD_BRAND, card.getBrand());
            intent.putExtra(CARD_EXPIRY_MONTH, card.getExpMonth());
            intent.putExtra(CARD_EXPIRY_YEAR, card.getExpYear());
            startActivity(intent);
        } else {
            startActivityForResult(new Intent(this, SaveCardActivity.class), SAVE_CARD_REQUEST);
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

    private void setIsLoading(boolean value) {
        mProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(value ? View.GONE : View.VISIBLE);
    }
}
