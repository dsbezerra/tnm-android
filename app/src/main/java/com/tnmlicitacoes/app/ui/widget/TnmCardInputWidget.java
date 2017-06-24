package com.tnmlicitacoes.app.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.TextInputLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.stripe.android.model.Card;
import com.stripe.android.util.CardUtils;
import com.stripe.android.util.DateUtils;
import com.stripe.android.util.LoggingUtils;
import com.stripe.android.util.StripeTextUtils;
import com.stripe.android.view.CardNumberEditText;
import com.stripe.android.view.StripeEditText;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.utils.AndroidUtilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import static com.stripe.android.model.Card.CVC_LENGTH_AMERICAN_EXPRESS;
import static com.stripe.android.model.Card.CVC_LENGTH_COMMON;
import static com.stripe.android.model.Card.CardBrand;
import static com.stripe.android.view.CardInputWidget.FOCUS_CARD;
import static com.stripe.android.view.CardInputWidget.FOCUS_CVC;
import static com.stripe.android.view.CardInputWidget.FOCUS_EXPIRY;
import static com.stripe.android.view.CardInputWidget.FocusField;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 * NOTE(diego): This class is a slight modification of the CardInputWidget to use another
 * R.layout and to listen for another changes of user typing
 */
public class TnmCardInputWidget extends LinearLayout {

    /* The logging tag */
    private static final String TAG = "CardInputWidget";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            HELP_EXPIRY,
            HELP_CVC
    })
    public @interface HelpIcon { }
    public static final String HELP_EXPIRY = "help_expiry";
    public static final String HELP_CVC = "help_cvc";

    public static final Map<String , Integer> BRAND_RESOURCE_MAP =
            new HashMap<String , Integer>() {{
                put(Card.AMERICAN_EXPRESS, R.drawable.ic_amex);
                put(Card.DINERS_CLUB, R.drawable.ic_diners);
                put(Card.DISCOVER, R.drawable.ic_discover);
                put(Card.JCB, R.drawable.ic_jcb);
                put(Card.MASTERCARD, R.drawable.ic_mastercard);
                put(Card.VISA, R.drawable.ic_visa);
                put(Card.UNKNOWN, R.drawable.ic_unknown);
            }};

    // This value is used to ensure that onSaveInstanceState is called
    // in the event that the user doesn't give this control an ID.
    private static final @IdRes int DEFAULT_READER_ID = 42424242;

    private ImageView mCardIconImageView;
    @Nullable private CardInputListener mCardInputListener;
    private TnmCardNumberEditText mCardNumberEditText;
    private boolean mCardNumberIsViewed = true;
    private TnmExpiryDateEditText mExpiryDateEditText;
    private StripeEditText mCvcNumberEditText;

    private TextInputLayout mCardNumberInputLayout;
    private TextInputLayout mExpiryDateInputLayout;
    private TextInputLayout mCvcInputLayout;

    /* Added for TNM needs */
    private boolean mIsEditingCard = false;

    private String mCardHintText;
    private @ColorInt int mErrorColorInt;
    private @ColorInt int mTintColorInt;

    private boolean mIsAmEx;

    public TnmCardInputWidget(Context context) {
        super(context);
        initView(null);
    }

    public TnmCardInputWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public TnmCardInputWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        inflate(getContext(), R.layout.tnm_card_input_widget, this);

        mCardIconImageView = (ImageView) findViewById(R.id.iv_card_icon);
        mCardNumberEditText = (TnmCardNumberEditText) findViewById(R.id.numberField);
        mExpiryDateEditText = (TnmExpiryDateEditText) findViewById(R.id.expiryDateField);
        mCvcNumberEditText = (StripeEditText) findViewById(R.id.cvcField);

        mCardNumberInputLayout = (TextInputLayout) findViewById(R.id.numberFieldInputLayout);
        mExpiryDateInputLayout = (TextInputLayout) findViewById(R.id.expiryDateInputLayout);
        mCvcInputLayout = (TextInputLayout) findViewById(R.id.cvcInputLayout);

        mCardNumberIsViewed = true;

        mErrorColorInt = mCardNumberEditText.getDefaultErrorColorInt();
        mTintColorInt = mCardNumberEditText.getHintTextColors().getDefaultColor();
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CardInputView,
                    0, 0);

            try {
                mErrorColorInt =
                        a.getColor(R.styleable.CardInputView_cardTextErrorColor, mErrorColorInt);
                mTintColorInt =
                        a.getColor(R.styleable.CardInputView_cardTint, mTintColorInt);
                mCardHintText =
                        a.getString(R.styleable.CardInputView_cardHintText);
            } finally {
                a.recycle();
            }
        }

        if (mCardHintText != null) {
            mCardNumberEditText.setHint(mCardHintText);
        }
        mCardNumberEditText.setErrorColor(mErrorColorInt);
        mExpiryDateEditText.setErrorColor(mErrorColorInt);
        mCvcNumberEditText.setErrorColor(mErrorColorInt);

        // This puts the outlined help icon to the right of the ExpiryDate and CvcNumber fields
        setHelpRightDrawable(true);

        mCardNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mCardInputListener != null) {
                        mCardInputListener.onFocusChange(FOCUS_CARD);
                    }
                }
            }
        });

        // Updates the floating label
        mExpiryDateEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus || mExpiryDateEditText.length() != 0) {
                    mExpiryDateInputLayout.setHint("Data de venc.");
                    if (mCardInputListener != null) {
                        mCardInputListener.onFocusChange(FOCUS_EXPIRY);
                    }
                } else {
                    mExpiryDateInputLayout.setHint("MM/AA");
                }
            }
        });

        // Updates the floating label
        mCvcNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus || mCvcNumberEditText.length() != 0) {
                    mCvcInputLayout.setHint("Cód. de segurança");
                    if (mCardInputListener != null) {
                        mCardInputListener.onFocusChange(FOCUS_CVC);
                    }
                } else {
                    mCvcInputLayout.setHint("CSC");
                }
            }
        });

        mCvcNumberEditText.setAfterTextChangedListener(new StripeEditText.AfterTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                if (mCardInputListener != null
                        && isCvcMaximalLength(mCardNumberEditText.getCardBrand(), text)) {
                    mCardInputListener.onCvcComplete();
                    return;
                }

                if (mCardInputListener != null) {
                    mCardInputListener.onFieldChange(isCvcMaximalLength(mCardNumberEditText.getCardBrand(), text));
                }
            }
        });

        mCardNumberEditText.setCardNumberCompleteListener(
                new TnmCardNumberEditText.CardNumberListener() {
                    @Override
                    public void onCardNumberComplete() {
                        if (mCardInputListener != null) {
                            mCardInputListener.onCardComplete();
                        }
                    }

                    @Override
                    public void onCardFieldChanged(boolean isValid) {
                        if (mCardInputListener != null) {
                            mCardInputListener.onFieldChange(isValid);
                        }
                    }
                });

        mCardNumberEditText.setCardBrandChangeListener(
                new TnmCardNumberEditText.CardBrandChangeListener() {
                    @Override
                    public void onCardBrandChanged(@NonNull @Card.CardBrand String brand) {
                        mIsAmEx = Card.AMERICAN_EXPRESS.equals(brand);
                        updateIcon(brand);
                        updateCvc(brand);
                    }
                });

        mExpiryDateEditText.setExpiryDateEditListener(new TnmExpiryDateEditText.ExpiryDateEditListener() {
            @Override
            public void onExpiryDateComplete() {
                mCvcNumberEditText.requestFocus();
                if (mCardInputListener != null) {
                    mCardInputListener.onExpirationComplete();
                }
            }

            @Override
            public void onExpiryDateChanged(boolean isDateValid) {
                if (mCardInputListener != null) {
                    mCardInputListener.onFieldChange(isDateValid);
                }
            }
        });

        // Set up on click in help drawables
        mExpiryDateEditText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int drawableLeftX = mExpiryDateEditText.getRight() - mExpiryDateEditText
                            .getCompoundDrawables()[2].getBounds().width();
                    if (event.getX() >= drawableLeftX) {
                        if (mCardInputListener != null) {
                            mCardInputListener.onHelpClick(HELP_EXPIRY);
                            mExpiryDateEditText.post(new Runnable() {
                                @Override
                                public void run() {
                                    AndroidUtilities.hideKeyboard(mExpiryDateEditText);
                                }
                            });
                        }
                    }
                }

                return false;
            }
        });

        mCvcNumberEditText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int drawableLeftX = mCvcNumberEditText.getRight() - mCvcNumberEditText
                            .getCompoundDrawables()[2].getBounds().width();
                    if (event.getX() >= drawableLeftX) {
                        if (mCardInputListener != null) {
                            mCardInputListener.onHelpClick(HELP_CVC);
                            mCvcNumberEditText.post(new Runnable() {
                                @Override
                                public void run() {
                                    AndroidUtilities.hideKeyboard(mCvcNumberEditText);
                                }
                            });
                        }
                    }
                }

                return false;
            }
        });

        mCardNumberEditText.requestFocus();
    }

    /**
     * Sets the help icon to the right of fields
     * @param show
     */
    private void setHelpRightDrawable(boolean show) {
        setHelpRightDrawable(show, show);
    }

    /**
     * Sets the help icons to the right of the specified fields
     */
    private void setHelpRightDrawable(boolean expiry, boolean cvc) {
        Drawable drawableRight = AppCompatResources.getDrawable(getContext(),
                R.drawable.ic_help_outline_black_16dp);

        if (expiry) {
            mExpiryDateEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null);
        } else {
            mExpiryDateEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        if (cvc) {
            mCvcNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null);
        } else {
            mCvcNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            applyTint(false);
        }
    }

    /**
     * Determines whether or not the icon should show the card brand instead of the
     * CVC helper icon.
     *
     * @param brand the {@link CardBrand} in question, used for determining max length
     * @param cvcHasFocus {@code true} if the CVC entry field has focus, {@code false} otherwise
     * @param cvcText the current content of {@link #mCvcNumberEditText}
     * @return {@code true} if we should show the brand of the card, or {@code false} if we
     * should show the CVC helper icon instead
     */
    @VisibleForTesting
    static boolean shouldIconShowBrand(
            @NonNull @CardBrand String brand,
            boolean cvcHasFocus,
            @Nullable String cvcText) {
        if (!cvcHasFocus) {
            return true;
        }
        return isCvcMaximalLength(brand, cvcText);
    }

    private static boolean isCvcMaximalLength(
            @NonNull @CardBrand String cardBrand,
            @Nullable String cvcText) {
        if (cvcText == null) {
            return false;
        }

        if (Card.AMERICAN_EXPRESS.equals(cardBrand)) {
            return cvcText.length() == CVC_LENGTH_AMERICAN_EXPRESS;
        } else {
            return cvcText.length() == CVC_LENGTH_COMMON;
        }
    }

    private void applyTint(boolean isCvc) {
        if (isCvc || Card.UNKNOWN.equals(mCardNumberEditText.getCardBrand())) {
            Drawable icon = mCardIconImageView.getDrawable();
            Drawable compatIcon = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(compatIcon.mutate(), mTintColorInt);
            mCardIconImageView.setImageDrawable(DrawableCompat.unwrap(compatIcon));
        }
    }

    private void updateCvc(@NonNull @Card.CardBrand String brand) {
        if (Card.AMERICAN_EXPRESS.equals(brand)) {
            mCvcNumberEditText.setFilters(
                    new InputFilter[] {new InputFilter.LengthFilter(CardUtils.CVC_LENGTH_AMEX)});
            //mCvcNumberEditText.setHint(R.string.cvc_amex_hint);
        } else {
            mCvcNumberEditText.setFilters(
                    new InputFilter[] {new InputFilter.LengthFilter(CardUtils.CVC_LENGTH_COMMON)});
            //mCvcNumberEditText.setHint(R.string.cvc_number_hint);
        }
    }

    private void updateIcon(@NonNull @Card.CardBrand String brand) {
        if (Card.UNKNOWN.equals(brand)) {
            Drawable icon  = getResources().getDrawable(R.drawable.ic_unknown);
            mCardIconImageView.setImageDrawable(icon);
            applyTint(false);
        } else {
            mCardIconImageView.setImageResource(BRAND_RESOURCE_MAP.get(brand));
        }
    }

    /**
     * Added for TNM needs
     *
     * Updates brand icon from outside this class
     * @param brand the card brand
     */
    public void updateBrandIcon(@NonNull @Card.CardBrand String brand) {
        mCardNumberEditText.updateCardBrand(brand);
        mIsAmEx = Card.AMERICAN_EXPRESS.equals(brand);
        updateIcon(brand);
        updateCvc(brand);
    }

    /**
     * Added for TNM needs
     *
     * Sets the editing mode of the view
     * @param value the new value
     */
    public void setEditingMode(final boolean value) {
        this.mIsEditingCard = value;

        // Disable the field and errors if we are editing (Stripe doesn't allow to edit the card number
        // or cvv, only exp date)
        mCardNumberEditText.setFocusable(!value);
        mCardNumberEditText.setFocusableInTouchMode(!value);
        mCardNumberEditText.setEnabled(!value);
        mCardNumberEditText.setShouldShowError(!value);
        mCardNumberEditText.setErrorColor(value ? 0xFF000000 : mErrorColorInt);

        // For some reason this avoid the NPE crash with the InputConnection
        mCvcNumberEditText.post(new Runnable() {
            @Override
            public void run() {
                mCvcNumberEditText.setFocusable(!value);
                mCvcNumberEditText.setEnabled(!value);
                mCardNumberEditText.setShouldShowError(!value);
                mCvcNumberEditText.setErrorColor(value ? 0xFF000000 : mErrorColorInt);

                setHelpRightDrawable(true, !value);

                if (value) {
                    mExpiryDateEditText.requestFocus();
                }
            }
        });
    }

    /**
     * Gets a {@link Card} object from the user input, if all fields are valid. If not, returns
     * {@code null}.
     *
     * @return a valid {@link Card} object based on user input, or {@code null} if any field is
     * invalid
     */
    @Nullable
    public Card getCard() {
        String cardNumber = mCardNumberEditText.getCardNumber();
        int[] cardDate = mExpiryDateEditText.getValidDateFields();
        // Modified for TNM needs
        if (cardNumber == null && !mIsEditingCard) {
            return null;
        }

        if (cardDate == null || cardDate.length != 2) {
            return null;
        }

        // CVC/CVV is the only field not validated by the entry control itself, so we check here.
        int requiredLength = mIsAmEx ? CardUtils.CVC_LENGTH_AMEX : CardUtils.CVC_LENGTH_COMMON;
        String cvcValue = mCvcNumberEditText.getText().toString();
        if (StripeTextUtils.isBlank(cvcValue) || cvcValue.length() != requiredLength) {
            return null;
        }

        return new Card(cardNumber, cardDate[0], cardDate[1], cvcValue)
                .addLoggingToken(LoggingUtils.CARD_WIDGET_TOKEN);
    }

    /**
     * Set a {@link CardInputListener} to be notified of card input events.
     *
     * @param listener the listener
     */
    public void setCardInputListener(@Nullable CardInputListener listener) {
        mCardInputListener = listener;
    }

    @VisibleForTesting
    void setCardNumberIsViewed(boolean cardNumberIsViewed) {
        mCardNumberIsViewed = cardNumberIsViewed;
    }

    /**
     * Set the card number. Method does not change text field focus.
     *
     * @param cardNumber card number to be set
     */
    public void setCardNumber(String cardNumber) {
        mCardNumberEditText.setText(cardNumber);
        setCardNumberIsViewed(!mCardNumberEditText.isCardNumberValid());
    }

    /**
     * Set the expiration date. Method invokes completion listener and changes focus
     * to the CVC field if a valid date is entered.
     *
     * Note that while a four-digit and two-digit year will both work, information
     * beyond the tens digit of a year will be truncated. Logic elsewhere in the SDK
     * makes assumptions about what century is implied by various two-digit years, and
     * will override any information provided here.
     *
     * @param month a month of the year, represented as a number between 1 and 12
     * @param year a year number, either in two-digit form or four-digit form
     */
    public void setExpiryDate(
            @IntRange(from = 1, to = 12) int month,
            @IntRange(from = 0, to = 9999) int year) {
        mExpiryDateEditText.setText(DateUtils.createDateStringFromIntegerInput(month, year));
        mExpiryDateInputLayout.setHint("Data de venc.");
    }

    /**
     * Set the CVC value for the card. Note that the maximum length is assumed to
     * be 3, unless the brand of the card has already been set (by setting the card number).
     *
     * @param cvcCode the CVC value to be set
     */
    public void setCvcCode(String cvcCode) {
        mCvcNumberEditText.setText(cvcCode);
    }

    /**
     * Clear all text fields in the CardInputWidget.
     */
    public void clear() {
        if (mCardNumberEditText.hasFocus()
                || mExpiryDateEditText.hasFocus()
                || mCvcNumberEditText.hasFocus()
                || this.hasFocus()) {
            mCardNumberEditText.requestFocus();
        }
        mCvcNumberEditText.setText("");
        mExpiryDateEditText.setText("");
        mCardNumberEditText.setText("");
    }

    /**
     * Represents a listener for card input events. Note that events are
     * not one-time events. For instance, a user can "complete" the CVC many times
     * by deleting and re-entering the value.
     */
    public interface CardInputListener {

        /**
         * Called whenever the field of focus within the widget changes.
         *
         * @param focusField a {@link FocusField} to which the focus has just changed.
         */
        void onFocusChange(@FocusField String focusField);

        /**
         * Called when a potentially valid card number has been completed in the
         * {@link CardNumberEditText}. May be called multiple times if the user edits
         * the field.
         */
        void onCardComplete();

        /**
         * Called when a expiration date (one that has not yet passed) has been entered.
         * May be called multiple times, if the user edits the date.
         */
        void onExpirationComplete();

        /**
         * Called when a potentially valid CVC has been entered. The only verification performed
         * on the number is that it is the correct length. May be called multiple times, if
         * the user edits the CVC.
         */
        void onCvcComplete();

        /**
         * Called when the user touches in the help right drawables
         *
         * @param helpIcon a {@link HelpIcon} to which the icon has clicked.
         */
        void onHelpClick(@HelpIcon String helpIcon);

        /**
         * Called when a field changes its value
         */
        void onFieldChange(boolean isFieldValid);
    }
}
