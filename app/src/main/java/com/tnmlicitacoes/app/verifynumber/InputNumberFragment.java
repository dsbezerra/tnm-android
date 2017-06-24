package com.tnmlicitacoes.app.verifynumber;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.RequestCodeMutation;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.transitionseverywhere.TransitionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;


public class InputNumberFragment extends VerifyNumberFragment implements
        VerifyNumberActivity.VerifyNumberContent {

    /* The logging and fragment tag */
    public static final String TAG = "InputNumberFragment";

    /* Make sure the phone has only numbers */
    private static final Pattern ONLY_NUMBERS = Pattern.compile("\\D");

    /* Put the two first digits in parenteshes */
    private static final Pattern BETWEEN_PARENTHESES = Pattern.compile("(\\d{2})(\\d)");

    /* Separate the digits */
    private static final Pattern SEPARARE_DIGITS = Pattern.compile("(\\d)(\\d{4})$");

    /* Expression that validates a phone number without the 55 (brazil contry code) prefix */
    private static final Pattern VALID_PHONE_EXP = Pattern.compile(
            "(?:1[1-9]|2[12478]|3[1-578]|[4689][1-9]|5[13-5]|7[13-579])(?:7|9?[689])\\d{7}$"
    );

    /* Stores cities DDDs */
    private HashMap<String, String> mDDDs = new HashMap<>();

    /* The transition container */
    private ViewGroup mTransitionContainer;

    /* Containers of views */
    private ViewGroup mPhoneContainer;
    private ViewGroup mProgressContainer;

    /* The field where the user types his phone */
    private EditText mPhoneField;

    /* The button that advances the process of verification */
    private Button mAdvanceButton;

    /* API Apollo requestCode call */
    private ApolloCall<RequestCodeMutation.Data> mRequestCodeCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_number_input, container, false);
        initViews(view);
        mTransitionContainer = (ViewGroup) view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateDDDsMap();
    }

    /**
     * Initializes the views
     */
    private void initViews(View view) {
        mPhoneContainer = (ViewGroup) view.findViewById(R.id.phoneContainer);
        mProgressContainer = (ViewGroup) view.findViewById(R.id.progressContainer);
        mPhoneField = (EditText) view.findViewById(R.id.phoneField);
        mAdvanceButton = (Button) view.findViewById(R.id.advanceBtn);
        mAdvanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAdvanceButtonClick();
            }
        });
        mPhoneField.addTextChangedListener(new TextWatcher() {

            boolean ignoreChange = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            // Apply regex to format the phone while user is typing...
            @Override
            public void afterTextChanged(Editable s) {
                if (ignoreChange) {
                    ignoreChange = false;
                    return;
                }

                ignoreChange = true;

                String currentText = mPhoneField.getText().toString();
                currentText = ONLY_NUMBERS.matcher(currentText).replaceAll("");
                currentText = BETWEEN_PARENTHESES.matcher(currentText).replaceFirst("($1) $2");
                currentText = SEPARARE_DIGITS.matcher(currentText).replaceAll("$1-$2");

                if (currentText != null) {
                    mPhoneField.setText(currentText);
                    mPhoneField.setSelection(mPhoneField.length());
                }

            }
        });
    }

    /**
     * Handles the click on advance button
     */
    private void handleAdvanceButtonClick() {
        AndroidUtilities.hideKeyboard(mPhoneField);

        boolean cancel = false;

        String phoneText = mPhoneField.getText().toString();
        phoneText = ONLY_NUMBERS.matcher(phoneText).replaceAll("");

        boolean isValidNumber = isPhoneNumberValid(phoneText);
        if (!isValidNumber) {
            mPhoneField.setError("Telefone inválido");
            cancel = true;
        }

        if (!cancel) {
            // Hide phone container and show progress one
            TransitionManager.beginDelayedTransition(mTransitionContainer);
            mPhoneContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.VISIBLE);

            String phoneWithCountryCode = "55" + phoneText;
            // We save the user phone in preferences in both formats because we need
            // the phone in both formats in other parts of the application.
            // We could save one and format when necessary too
            SettingsUtils.putString(mActivity, SettingsUtils.PREF_USER_PHONE_NUMBER,
                    phoneWithCountryCode);
            SettingsUtils.putString(mActivity, SettingsUtils.PREF_USER_PHONE_NUMBER_FORMATTED,
                    formatPhone(phoneWithCountryCode));

            // Make the call
            //makeRequestCodeCall(phoneWithCountryCode);
            mListener.onRequestCodeResponse(null, null);
        }
    }

    /**
     * Call API requestCode mutation with a phone number
     * @param phoneWithCountryCode the phone number
     */
    private void makeRequestCodeCall(String phoneWithCountryCode) {
        RequestCodeMutation requestCode = RequestCodeMutation.builder()
                .phone(phoneWithCountryCode)
                .build();

        mRequestCodeCall = mApplication.getApolloClient()
                .mutate(requestCode)
                .cacheControl(CacheControl.NETWORK_ONLY);

        mRequestCodeCall.enqueue(dataCallback);
    }

    /**
     * Callback for the requestCode mutation API call
     */
    private ApolloCall.Callback<RequestCodeMutation.Data> dataCallback = new ApolloCall.Callback<RequestCodeMutation.Data>() {
        @Override
        public void onResponse(Response<RequestCodeMutation.Data> response) {
            handleRequestCodeResponse(response);
        }

        @Override
        public void onFailure(ApolloException e) {
            if (mListener != null) {
                mListener.onRequestCodeResponse(null, e);
                LOG_DEBUG(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    };

    /**
     * Handles the requestCode mutation response
     * @param response The response object
     */
    private void handleRequestCodeResponse(Response<RequestCodeMutation.Data> response) {
        if (mListener != null) {
            mListener.onRequestCodeResponse(response, null);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Restore phone container and hide progress one
                    TransitionManager.beginDelayedTransition(mTransitionContainer);
                    mPhoneContainer.setVisibility(View.VISIBLE);
                    mProgressContainer.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Check if the phone is valid
     * @param phone The phone to check
     * @return true if phone is valid false if not
     */
    private boolean isPhoneNumberValid(String phone) {
        return VALID_PHONE_EXP.matcher(phone).matches();
    }

    /**
     * Formats a phone from 55XXXXXXXXXXX to +55 (XX) XXXXX-XXXX
     * @param phone The phone to be formatted
     */
    public static String formatPhone(String phone) {
        if (phone.length() > 13) {
            throw new RuntimeException("Phone length invalid!");
        }

        String formattedPhone = phone.substring(2);
        formattedPhone = BETWEEN_PARENTHESES.matcher(formattedPhone).replaceFirst("($1) $2");
        formattedPhone = SEPARARE_DIGITS.matcher(formattedPhone).replaceAll("$1-$2");
        formattedPhone = "+55 " + formattedPhone;
        return formattedPhone;
    }

    /**
     * Fill the hash map
     */
    private void populateDDDsMap() {
        // Let's clear in case that this is called or twice
        mDDDs.clear();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources()
                    .getAssets()
                    .open("states.txt")));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] params = line.split(";");
                if (params.length != 3) {
                    throw new RuntimeException("Wrong parameters length, check if "
                            + "states.txt parameters is divided by ;");
                }

                String ddd          = params[0];
                String stateName    = params[1];
                String initials     = params[2];

                // Adds DDD to HashMap
                mDDDs.put(ddd, stateName + " - " + initials);
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return !SettingsUtils.isWaitingForSms(context);
    }
}
