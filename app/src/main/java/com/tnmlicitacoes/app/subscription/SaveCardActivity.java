package com.tnmlicitacoes.app.subscription;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.tnmlicitacoes.app.AddCardMutation;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.ui.ShowErrorDialog;
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity;
import com.tnmlicitacoes.app.ui.base.BaseDialogFragment;
import com.tnmlicitacoes.app.ui.widget.TnmCardInputWidget;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.ApiUtils;

import static com.tnmlicitacoes.app.ui.widget.TnmCardInputWidget.HELP_CVC;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class SaveCardActivity extends BaseAuthenticatedActivity implements View.OnClickListener {

    @Override
    public String getLogTag() {
        return TAG;
    }

    /* The logging tag */
    private static final String TAG = "SaveCardActivity";

    /* Request code used in the intent to get the card */
    static final int SAVE_CARD_REQUEST = 1;

    /* Key to get the editing state from the bundle */
    public static final String EDIT_CARD_MODE  = "edit_card";

    /* Key to get the card metadata */
    public static final String CARD_IS_DEFAULT = "card_is_default";
    public static final String CARD_ID = "card_id";
    public static final String CARD_BRAND = "card_brand";
    public static final String CARD_NUMBER = "card_number";
    public static final String CARD_LAST4 = "card_last4";
    public static final String CARD_EXPIRY_MONTH = "card_expiry_month";
    public static final String CARD_EXPIRY_YEAR = "card_expiry_year";
    public static final String CARD_CVC = "card_cvc";

    /* The application singleton */
    private TnmApplication mApplication;

    /* The Stripe object instance */
    private Stripe mStripe;

    /* The card to save */
    private Card mCardToSave;

    /* Displays the card input layout */
    private TnmCardInputWidget mTnmCardInputWidget;

    /* The progress dialog */
    private ProgressDialog mProgressDialog;

    /* Displays the save button */
    private Button mSaveButton;

    /* Indicate whether the activity is in edit mode or not */
    private boolean mIsEditing = false;

    /* Add card call */
    private ApolloMutationCall<AddCardMutation.Data> mAddCardCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (TnmApplication) getApplication();
        setContentView(R.layout.activity_save_card);
        initViews();

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            mIsEditing = extras.getBoolean(EDIT_CARD_MODE);
        }

        mTnmCardInputWidget.setEditingMode(mIsEditing);
        if (mIsEditing) {
            setupToolbar(getString(R.string.title_activity_save_card_edit_mode));
            fillCardFields();
        } else {
            setupToolbar(getString(R.string.title_activity_save_card));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        enableButtonIfValid();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAddCardCall != null) {
            mAddCardCall.cancel();
        }
    }

    /**
     * Initialize views
     */
    private void initViews() {
        mTnmCardInputWidget = (TnmCardInputWidget) findViewById(R.id.card_input_widget);
        mSaveButton = (Button) findViewById(R.id.save_btn);
        mTnmCardInputWidget.setCardInputListener(new TnmCardInputWidget.CardInputListener() {
            @Override
            public void onFocusChange(@CardInputWidget.FocusField String focusField) {
                enableButtonIfValid();
            }

            @Override
            public void onCardComplete() {
                enableButtonIfValid();
            }

            @Override
            public void onExpirationComplete() {
                enableButtonIfValid();
            }

            @Override
            public void onCvcComplete() {
                enableButtonIfValid();
            }

            @Override
            public void onHelpClick(@TnmCardInputWidget.HelpIcon String helpIcon) {
                showHelpDialog(helpIcon);
            }

            @Override
            public void onFieldChange(boolean isFieldValid) {
                enableButtonIfValid();
            }
        });
        mSaveButton.setOnClickListener(this);
    }

    /**
     * Fill card fields with card to be edited info
     */
    private void fillCardFields() {

        boolean cancel = false;

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null && !extras.isEmpty()) {
                String brand = extras.getString(CARD_BRAND);
                String last4 = extras.getString(CARD_LAST4);
                int month = extras.getInt(CARD_EXPIRY_MONTH);
                int year = extras.getInt(CARD_EXPIRY_YEAR);

                if (!TextUtils.isEmpty(brand) && !TextUtils.isEmpty(last4) &&
                        isValidMonth(month) && isValidYear(year)) {
                    mTnmCardInputWidget.setCardNumber(getString(R.string.card_number_last_digits, last4));
                    mTnmCardInputWidget.setExpiryDate(month, year);
                    mTnmCardInputWidget.setCvcCode(getCvcPlaceholder(brand));
                    mTnmCardInputWidget.updateBrandIcon(brand);
                    mSaveButton.setText(getString(R.string.save_text));
                } else {
                    cancel = true;
                }
            } else {
                cancel = true;
            }
        } else {
            cancel = true;
        }

        if (cancel) {
            // Finish activity
            Toast.makeText(this, "Ocorreu um erro inesperado.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    /**
     * Enables the button if the card is valid
     */
    private void enableButtonIfValid() {
        boolean enabled = false;

        mCardToSave = mTnmCardInputWidget.getCard();
        if (mCardToSave != null) {
            if (mIsEditing) {
                enabled = mCardToSave.validateExpiryDate();
            } else {
                enabled = mCardToSave.validateCard();
            }
        }

        AndroidUtilities.setButtonEnabled(mSaveButton, enabled);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.save_btn:

                if (mTnmCardInputWidget.hasFocus()) {
                    AndroidUtilities.hideKeyboard(mTnmCardInputWidget);
                }

                if (mIsEditing) {
                    editCard();
                } else {
                    saveCard();
                }
                break;
        }
    }

    /**
     * Send the card to the activity that called this
     */
    private void saveCard() {
        mCardToSave = mTnmCardInputWidget.getCard();
        if (mCardToSave != null && mCardToSave.validateCard()) {
            if (mStripe == null) {
                mStripe = new Stripe(this, PaymentsActivity.STRIPE_PUBLISHABLE_KEY);
            }

            mProgressDialog = AndroidUtilities.createProgressDialog(this,
                    getString(R.string.add_card_dialog_progress_message),
                    true, false);
            mProgressDialog.show();

            mStripe.createToken(mCardToSave, new TokenCallback() {
                @Override
                public void onError(Exception error) {
                    Toast.makeText(SaveCardActivity.this, R.string.add_card_fail,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(Token token) {
                    AddCardMutation addCard = AddCardMutation.builder()
                            .source(token.getId())
                            .build();
                    mAddCardCall = mApplication.getApolloClient()
                            .mutate(addCard);
                    mAddCardCall.enqueue(addCardDataCallback);
                }
            });
        }
    }

    /**
     * Callback for the addCard mutation API call
     */
    private ApolloCall.Callback<AddCardMutation.Data> addCardDataCallback = new ApolloCall.Callback<AddCardMutation.Data>() {
        @Override
        public void onResponse(Response<AddCardMutation.Data> response) {

            dismissProgressDialog();

            if (!response.hasErrors()) {
                if (response.data() != null && response.data().addCard() != null) {
                    AddCardMutation.AddCard data = response.data().addCard();
                    Intent intent = new Intent();
                    intent.putExtra(CARD_ID, data.id());
                    intent.putExtra(CARD_BRAND, data.brand());
                    intent.putExtra(CARD_LAST4, data.last4());
                    intent.putExtra(CARD_EXPIRY_MONTH, data.exp_month());
                    intent.putExtra(CARD_EXPIRY_YEAR, data.exp_year());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else {
                ApiUtils.ApiError error = ApiUtils.getFirstValidError(SaveCardActivity.this,
                        response.errors());
                if (error != null) {
                    showErrorDialog(error);
                }
            }
        }

        @Override
        public void onFailure(ApolloException e) {
            LOG_DEBUG(TAG, e.getMessage());
        }
    };

    /**
     * Sends a api call to edit the card
     * TODO(diego): Replace with actual call to API
     */
    private void editCard() {
        mProgressDialog = AndroidUtilities.createProgressDialog(this, "Atualizando cartão...", true,
                false);
        mProgressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
            }
        }, 5000);
    }

    /**
     * Gets the placeholder for the CVV field of the given brand
     */
    public String getCvcPlaceholder(String brand) {
        final int size = brand.equals(Card.AMERICAN_EXPRESS) ? 4 : 3;
        StringBuilder placeholderBuilder = new StringBuilder(size);
        for(int i = 0; i < size; i++) {
            placeholderBuilder.append("•");
        }

        return placeholderBuilder.toString();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * Shows a error dialog
     */
    private void showErrorDialog(ApiUtils.ApiError error) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(ShowErrorDialog.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        boolean fromResources = error.isFromResources();
        String title = fromResources ? getString(error.getTitleRes()) : error.getTitle();
        String message = fromResources ? getString(error.getMessageRes()) : error.getMessage();
        DialogFragment fragment = ShowErrorDialog.newInstance(title, message);
        fragment.show(getSupportFragmentManager(), ShowErrorDialog.TAG);
    }

    /**
     * Shows a help dialog for the given field
     */
    private void showHelpDialog(@TnmCardInputWidget.HelpIcon String helpIcon) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(CardHelpDialog.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment fragment = CardHelpDialog.newInstance(helpIcon);
        fragment.show(getSupportFragmentManager(), CardHelpDialog.TAG);
    }

    /* Matches stripe accepted values */
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    /* Matches stripe accepted values */
    public static boolean isValidYear(int year) {
        return year >= 0 && year <= 9999;
    }

    public static class CardHelpDialog extends BaseDialogFragment {

        /* The logging and dialog tag */
        private static final String TAG = "CardHelpDialog";

        /* Help icon tag key */
        private static String HELP_ICON = "help_icon";

        /* Displays the dialog title */
        private TextView mTitle;

        /* Displays the dialog message */
        private TextView mMessage;

        /* Displays the dialog image */
        private ImageView mImage;

        /* Displays the ok button */
        private Button mOkButton;

        public static CardHelpDialog newInstance(@TnmCardInputWidget.HelpIcon String helpIcon) {
            Bundle args = new Bundle();
            args.putString(HELP_ICON, helpIcon);

            CardHelpDialog fragment = new CardHelpDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_DefaultDialog);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_help_card, container, false);
            initViews(view);
            fill();
            return view;
        }

        private void initViews(View view) {
            mTitle = (TextView) view.findViewById(R.id.title);
            mMessage = (TextView) view.findViewById(R.id.message);
            mImage = (ImageView) view.findViewById(R.id.image);
            mOkButton = (Button) view.findViewById(R.id.ok_btn);

            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        private void fill() {
            Bundle args = getArguments();

            String helpIcon = args.getString(HELP_ICON);
            if (helpIcon.equals(TnmCardInputWidget.HELP_EXPIRY)) {
                mTitle.setText(R.string.card_help_expiry_date_title);
                mMessage.setText(R.string.card_help_expiry_date_message);
                mImage.setImageResource(R.drawable.expiry_date_location);
            } else if (helpIcon.equals(HELP_CVC)) {
                mTitle.setText(R.string.card_help_cvc_title);
                mMessage.setText(R.string.card_help_cvc_message);
                mImage.setImageResource(R.drawable.cvc_location);
            }
        }
    }
 }
