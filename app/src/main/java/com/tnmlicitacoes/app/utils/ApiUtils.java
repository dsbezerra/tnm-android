package com.tnmlicitacoes.app.utils;


import android.content.Context;
import android.support.annotation.UiThread;

import com.apollographql.apollo.api.Error;
import com.tnmlicitacoes.app.R;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import okhttp3.Request;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class ApiUtils {

    /* The logging tag */
    private static final String TAG = "ApiUtils";

    /* The code attribute from error response */
    private static final String CODE_ATTR = "code";

    private static final int TITLE = 0;
    private static final int MESSAGE = 1;

    /* API error codes*/
    public interface ErrorCode {

        /* Error not handled */
        int UNKNOWN                                         =       1;

        /* Client is not permitted to perform this action */
        int NOT_AUTHORIZED                                  =      10;

        /* LocalSupplier not found */
        int SUPPLIER_NOT_FOUND                              =      20;

        /* Rate limit exceeded */
        int LIMIT_EXCEEDED                                  =      30;

        /* User not found */
        int USER_NOT_FOUND                                  =      40;

        /* Wrong password (not used in the mobile app) */
        int INVALID_CREDENTIALS                             =      50;

        /* The cursor does not appear to be valid */
        int INVALID_CURSOR                                  =      60;

        /* Invalid ISO Date */
        int INVALID_ISO_DATE                                =      70;

        /* Sending a SMS failed */
        int SEND_SMS_FAILED                                 =      80;

        /* This account belongs to another user */
        int INVALID_OWNER                                   =      90;

        /* This email is already activated */
        int EMAIL_ACTIVATED                                 =     100;

        /* An unexpected payment error occurred */
        int STRIPE_UNEXPECTED                               =     120;

        /* Too many requests made to the API too quickly */
        int STRIPE_RATE_LIMIT                               =     140;

        /* Invalid parameters were supplied to the payment API */
        int STRIPE_INVALID_PARAMS                           =     150;


        /* Stripe string error codes */

        /* The card number is not a valid credit card number */
        String STRIPE_INVALID_NUMBER                        = "invalid_number";

        /* The card's expiration month is invalid */
        String STRIPE_INVALID_EXPIRY_MONTH                  = "invalid_expiry_month";

        /* The card's expiration month is invalid */
        String STRIPE_INVALID_EXPIRY_YEAR                   = "invalid_expiry_year";

        /* The card's security code is invalid */
        String STRIPE_INVALID_CVC                           = "invalid_cvc";

        /* The card's swipe data is invalid */
        String STRIPE_INVALID_SWIPE_DATA                    = "invalid_swipe_data";

        /* The card number is incorrect */
        String STRIPE_INCORRECT_NUMBER                      = "incorrect_number";

        /* The card has expired */
        String STRIPE_EXPIRED_CARD                          = "expired_card";

        /* The card's security code is incorrect */
        String STRIPE_INCORRECT_CVC                         = "incorrect_cvc";

        /* The card's zip code failed validation */
        String STRIPE_INCORRECT_ZIP                         = "incorrect_zip";

        /* The card was declined */
        String STRIPE_CARD_DECLINED                         = "card_declined";

        /* An error occurred while processing the card */
        String STRIPE_PROCESSING_ERROR                      = "processing_error";
    }

    /**
     * Handle errors from API
     * TODO(diego): Real error handler
     */
    @UiThread
    public static ApiError getFirstValidError(Context context, List<Error> errors) {

        ApiError result = null;

        for (Error error : errors) {
            Object code = error.customAttributes().get(CODE_ATTR);

            LOG_DEBUG(TAG, "Code: " + code + "\nMessage: " + error.message());

            if (code instanceof String) {
                String errorCode = (String) code;
                result = getErrorFromStringCode(errorCode);
                if (result != null) {
                    return result;
                }
            } else if (code instanceof BigDecimal) {
                BigDecimal errorCode = (BigDecimal) code;
                if (errorCode.intValue() >= 1) {
                    String message = getErrorMessage(context, errorCode.intValue());

                    result = new ApiError();
                    result.setCode(errorCode.intValue());
                    result.setMessage(message);
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * Returns a string for the given error code
     * @param context the application context
     * @param code the error code from API
     * @return the error in string format
     */
    public static String getErrorMessage(Context context, Object code) {
        return context.getString(getErrorMessageFromIntCode((Integer) code));
    }

    private static int getErrorMessageFromIntCode(int code) {
        int result;

        switch (code) {

            case ErrorCode.NOT_AUTHORIZED:
                result = R.string.error_api_not_authorized;
                break;

            case ErrorCode.LIMIT_EXCEEDED:
                result = R.string.error_api_limit_exceeded;
                break;

            case ErrorCode.SEND_SMS_FAILED:
                result = R.string.error_api_send_sms_failed;
                break;

            case ErrorCode.EMAIL_ACTIVATED:
                result = R.string.error_api_email_activated;
                break;

            default:
                result = R.string.error_api_unknown;
                break;

        }

        return result;
    }

    private static ApiError getErrorFromStringCode(String code) {

        if (code == null) {
            return  null;
        }

        ApiError result = new ApiError();
        result.setTextCode(code);
        result.setIsFromResources(true);

        switch (code) {
            case ErrorCode.STRIPE_INVALID_NUMBER:
                result.setTitleRes(R.string.error_stripe_invalid_number);
                result.setMessageRes(R.string.error_stripe_invalid_number_message);
                break;
            case ErrorCode.STRIPE_INVALID_EXPIRY_MONTH:
                result.setTitleRes(R.string.error_stripe_invalid_expiry_month);
                result.setMessageRes(R.string.error_stripe_invalid_expiry_month_message);
                break;
            case ErrorCode.STRIPE_INVALID_EXPIRY_YEAR:
                result.setTitleRes(R.string.error_stripe_invalid_expiry_year);
                result.setMessageRes(R.string.error_stripe_invalid_expiry_year_message);
                break;
            case ErrorCode.STRIPE_INVALID_CVC:
                result.setTitleRes(R.string.error_stripe_invalid_cvc);
                result.setMessageRes(R.string.error_stripe_invalid_cvc_message);
                break;
            case ErrorCode.STRIPE_INCORRECT_NUMBER:
                result.setTitleRes(R.string.error_stripe_incorrect_number);
                result.setMessageRes(R.string.error_stripe_incorrect_number_message);
                break;
            case ErrorCode.STRIPE_EXPIRED_CARD:
                result.setTitleRes(R.string.error_stripe_expired_card);
                result.setMessageRes(R.string.error_stripe_expired_card_message);
                break;
            case ErrorCode.STRIPE_INCORRECT_CVC:
                result.setTitleRes(R.string.error_stripe_incorrect_cvc);
                result.setMessageRes(R.string.error_stripe_incorrect_cvc_message);
                break;
            case ErrorCode.STRIPE_INCORRECT_ZIP:
                result.setTitleRes(R.string.error_stripe_incorrect_zip);
                result.setMessageRes(R.string.error_stripe_incorrect_zip_message);
                break;
            case ErrorCode.STRIPE_CARD_DECLINED:
                result.setTitleRes(R.string.error_stripe_card_declined);
                result.setMessageRes(R.string.error_stripe_card_declined_message);
                break;
            case ErrorCode.STRIPE_PROCESSING_ERROR:
                result.setTitleRes(R.string.error_stripe_processing_error);
                result.setMessageRes(R.string.error_stripe_processing_error_message);
                break;
            default:
                result.setTitleRes(R.string.error_api_unknown);
                result.setMessageRes(R.string.error_api_unknown_message);
                break;
        }

        return result;
    }

    public static class ApiError {

        private String mTitle;
        private String mMessage;

        private int mTitleRes;
        private int mMessageRes;

        private String mTextCode;
        private int mCode;

        private boolean mIsFromResources;

        public ApiError() {
            this.mIsFromResources = false;
        }

        public ApiError(boolean isFromResources) {
            this.mIsFromResources = isFromResources;
        }

        public ApiError(String title, String message) {
            this.mTitle = title;
            this.mMessage = message;
            this.mIsFromResources = false;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            this.mMessage = message;
        }

        public String getTextCode() {
            return mTextCode;
        }

        public void setTextCode(String textCode) {
            this.mTextCode = textCode;
        }

        public int getCode() {
            return mCode;
        }

        public void setCode(int code) {
            this.mCode = code;
        }

        public int getTitleRes() {
            return mTitleRes;
        }

        public void setTitleRes(int titleRes) {
            this.mTitleRes = titleRes;
        }

        public int getMessageRes() {
            return mMessageRes;
        }

        public void setMessageRes(int messageRes) {
            this.mMessageRes = messageRes;
        }

        public boolean isFromResources() {
            return mIsFromResources;
        }
        public void setIsFromResources(boolean value) {
            this.mIsFromResources = value;
        }
    }
}
