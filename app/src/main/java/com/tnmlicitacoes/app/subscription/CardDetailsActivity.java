package com.tnmlicitacoes.app.subscription;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.stripe.android.util.DateUtils;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.apollo.RemoveCardMutation;
import com.tnmlicitacoes.app.apollo.UpdateDefaultCardMutation;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;

import javax.annotation.Nonnull;

import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_BRAND;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_EXPIRY_MONTH;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_EXPIRY_YEAR;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_ID;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_IS_DEFAULT;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.CARD_LAST4;
import static com.tnmlicitacoes.app.subscription.SaveCardActivity.EDIT_CARD_MODE;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class CardDetailsActivity extends BaseAuthenticatedActivity {

    /* The logging tag */
    private static final String TAG = "CardDetailsActivity";

    /* Whether the card is the default or not */
    private boolean mIsDefault;

    /* The card id */
    private String mId;

    /* The card brand */
    private String mBrand;

    /* The last 4 digits of the card number */
    private String mLast4;

    /* The month of the expiry date */
    private int mExpiryMonth;

    /* The year of the expiry date */
    private int mExpiryYear;

    /* The progress dialog */
    private ProgressDialog mProgressDialog;

    /* Root view for Snackbar */
    private View mRootView;

    /* Displays the card last 4 digits */
    private TextView mCardNumber;

    /* Displays the card expiration date */
    private TextView mExpirationDate;

    /* The application singleton */
    private TnmApplication mApplication;

    /* Update default card API call */
    private ApolloMutationCall<UpdateDefaultCardMutation.Data> mUpdateDefaultCardCall;

    /* Delete card API call */
    private ApolloMutationCall<RemoveCardMutation.Data> mRemoveCardCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (TnmApplication) getApplication();
        setContentView(R.layout.activity_card_details);

        if (getIntent() == null || getIntent().getExtras() == null) {
            Toast.makeText(this, "Ocorreu um erro inesperado.", Toast.LENGTH_SHORT).show();
            finish();
        }

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateDefaultCardCall != null) {
            mUpdateDefaultCardCall.cancel();
        }
        if (mRemoveCardCall != null) {
            mRemoveCardCall.cancel();
        }
    }

    /**
     * Initialize the views
     */
    private void initViews() {
        mRootView = findViewById(R.id.root);
        mCardNumber = (TextView) findViewById(R.id.card_number);
        mExpirationDate = (TextView) findViewById(R.id.expiration_date);

        boolean cancel = false;

        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            mBrand = extras.getString(CARD_BRAND);
            if (!TextUtils.isEmpty(mBrand)) {
                setupToolbar(mBrand);
            } else {
                setupToolbar(getString(R.string.title_activity_card_details));
            }

            mIsDefault = extras.getBoolean(CARD_IS_DEFAULT);
            mId = extras.getString(CARD_ID);
            mLast4 = extras.getString(CARD_LAST4);
            mExpiryMonth = extras.getInt(CARD_EXPIRY_MONTH);
            mExpiryYear = extras.getInt(CARD_EXPIRY_YEAR);

            if (!TextUtils.isEmpty(mLast4) && SaveCardActivity.isValidMonth(mExpiryMonth) &&
                    SaveCardActivity.isValidYear(mExpiryYear)) {
                mCardNumber.setText(getString(R.string.card_number_last_digits, mLast4));

                StringBuilder formattedDate = new StringBuilder();
                String date = DateUtils.createDateStringFromIntegerInput(mExpiryMonth, mExpiryYear);
                String month = date.substring(0, 2);
                String year = date.substring(2);
                formattedDate.append(month);
                formattedDate.append("/");
                formattedDate.append(year);
                mExpirationDate.setText(formattedDate);
            } else {
                cancel = true;
            }
        } else {
            cancel = true;
        }

        if (cancel) {
            Toast.makeText(this, "Ocorreu um erro inesperado.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the action bar menu
        getMenuInflater().inflate(R.menu.menu_card_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem makeDefault = menu.findItem(R.id.action_make_default);
        makeDefault.setVisible(!mIsDefault);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.action_make_default:
                makeDefault();
                return true;

            case R.id.action_edit:
                editCard();
                return true;

            case R.id.action_delete:
                deleteCard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    /**
     * Sets the card as default
     */
    private void makeDefault() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.card_make_default_dialog_title)
                .setMessage(R.string.card_make_default_dialog_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpdateDefaultCardMutation updateDefaultCard = UpdateDefaultCardMutation.builder()
                                .cardId(mId)
                                .build();
                        mUpdateDefaultCardCall = mApplication.getApolloClient()
                                .mutate(updateDefaultCard);
                        mUpdateDefaultCardCall.enqueue(updateDefaultCardCallback);

                        mProgressDialog = AndroidUtilities.createProgressDialog(
                                CardDetailsActivity.this,
                                getString(R.string.card_make_default_progress_message),
                                true, false);
                        mProgressDialog.show();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        builder.show();
    }

    /**
     * Go to the edit activity
     */
    private void editCard() {
        Intent intent = new Intent(this, SaveCardActivity.class);
        intent.putExtra(EDIT_CARD_MODE, true);
        intent.putExtra(CARD_ID, mId);
        intent.putExtra(CARD_LAST4, mLast4);
        intent.putExtra(CARD_BRAND, mBrand);
        intent.putExtra(CARD_EXPIRY_MONTH, mExpiryMonth);
        intent.putExtra(CARD_EXPIRY_YEAR, mExpiryYear);
        startActivity(intent);
    }

    /**
     * Shows a alert dialog asking for assurance
     */
    private void deleteCard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.card_remove_dialog_title)
                .setMessage(R.string.card_remove_dialog_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoveCardMutation removeCard = RemoveCardMutation.builder()
                                .cardId(mId)
                                .build();
                        mRemoveCardCall = mApplication.getApolloClient()
                                .mutate(removeCard);
                        mRemoveCardCall.enqueue(removeCardCallback);

                        mProgressDialog = AndroidUtilities.createProgressDialog(
                                CardDetailsActivity.this,
                                getString(R.string.card_remove_progress_message),
                                true, false);
                        mProgressDialog.show();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        builder.show();
    }

    /**
     * updateDefaultCard mutation API call callback
     */
    private ApolloCall.Callback<UpdateDefaultCardMutation.Data> updateDefaultCardCallback =
            new ApolloCall.Callback<UpdateDefaultCardMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateDefaultCardMutation.Data> response) {
            dismissProgressDialog();
            if (!response.hasErrors()) {
                if (response.data() != null && response.data().updateDefaultCard()) {
                    PaymentsActivity.sShouldRefetch = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRootView, R.string.card_defined_as_default,
                                    Snackbar.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1000);
                        }
                    });
                } else {
                    Snackbar.make(mRootView, R.string.card_fail_to_set_as_default,
                            Snackbar.LENGTH_SHORT).show();
                }
            } else {
                for (Error error : response.errors()) {
                    LOG_DEBUG(TAG, error.message());
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            LOG_DEBUG(TAG, e.getMessage());
            dismissProgressDialog();
        }
    };

    /**
     * removeCard mutation API call callback
     */
    private ApolloCall.Callback<RemoveCardMutation.Data> removeCardCallback = new ApolloCall.Callback<RemoveCardMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<RemoveCardMutation.Data> response) {
            dismissProgressDialog();
            if (!response.hasErrors()) {
                if (response.data() != null && response.data().removeCard()) {
                    PaymentsActivity.sShouldRefetch = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRootView, R.string.card_removed, Snackbar.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1000);
                        }
                    });
                } else {
                    Snackbar.make(mRootView, R.string.card_fail_to_remove, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                for (Error error : response.errors()) {
                    LOG_DEBUG(TAG, error.message());
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            LOG_DEBUG(TAG, e.getMessage());
            dismissProgressDialog();
        }
    };

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
