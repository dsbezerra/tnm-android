package com.tnmlicitacoes.app.ui.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.RequestCodeMutation;
import com.tnmlicitacoes.app.TNMApplication;
import com.tnmlicitacoes.app.interfaces.OnPhoneConfirmationListener;
import com.tnmlicitacoes.app.ui.activity.VerifyNumberActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class InputNumberFragment extends VerifyNumberFragment
        implements VerifyNumberActivity.VerifyNumberContent, View.OnClickListener, OnPhoneConfirmationListener {

    private static final String TAG = "InputNumberFragment";

    private static final Pattern sOnlyNumbers = Pattern.compile("\\D");

    private static final Pattern sBetweenParentheses = Pattern.compile("(\\d{2})(\\d)");

    private static final Pattern sSeparateDigits = Pattern.compile("(\\d)(\\d{4})$");

    private Button mConfirmButton;

    private Button mContactButton;

    private EditText mEmailField;

    private EditText mPhoneField;

    private HashMap<String, String> mDDDs = new HashMap<>();

    private String mDialogNumberStateMessage;
    private String mTypedPhone;
    private String mTypedEmail;

    private TNMApplication mApplication;

    private ApolloCall<RequestCodeMutation.Data> mRequestCodeCall;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             Bundle savedInstanceState) {

        mApplication = (TNMApplication) getActivity().getApplication();

        View v = inflater.inflate(R.layout.fragment_number_input, container, false);
        initViews(v);
        initListeners();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateHashMap();
    }

    /**
     * Initialize all subviews of the parent view
     * @param view Parent view
     */
    private void initViews(View view) {
        mEmailField         = (EditText) view.findViewById(R.id.emailField);
        mPhoneField         = (EditText) view.findViewById(R.id.phoneField);
        mConfirmButton      = (Button) view.findViewById(R.id.numberOkButton);
        mContactButton      = (Button) view.findViewById(R.id.contactButton);
    }

    /**
     * Initialize all view listeners
     */
    private void initListeners() {
        mConfirmButton.setOnClickListener(this);
        mContactButton.setOnClickListener(this);
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
                currentText = sOnlyNumbers.matcher(currentText).replaceAll("");
                currentText = sBetweenParentheses.matcher(currentText).replaceFirst("($1) $2");
                currentText = sSeparateDigits.matcher(currentText).replaceAll("$1-$2");

                if (currentText != null) {
                    mPhoneField.setText(currentText);
                    mPhoneField.setSelection(mPhoneField.length());
                }
            }
        });
    }

    /**
     * Fills the HashMap with DDDs from states.txt file
     */
    private void populateHashMap() {
        mDDDs.clear();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources()
                    .getAssets()
                    .open("states.txt")));

            String line;
            while ((line = reader.readLine()) != null) {

                String[] params     = line.split(";");
                if(params.length != 3) {
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

    /**
     * Check if the phone is valid
     * @param phone The phone to check
     * @return true if phone is valid false if not
     */
    private boolean isPhoneNumberValid(String phone) {
        Pattern exp = Pattern.compile("(?:1[1-9]|2[12478]|3[1-578]|[4689][1-9]|5[13-5]|7[13-579])(?:7|9?[689])\\d{7}$");
        return exp.matcher(phone).matches();
    }

    /**
     * Ask for READ_SMS permission for Marshmallow and up
     */
    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

            showPhoneConfirmationDialog(false);

        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                Toast.makeText(getContext(),
                        getString(R.string.sms_rationale_verification),
                        Toast.LENGTH_LONG).show();
            }

            showSmsPermissionExplanationDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case AndroidUtilities.PERMISSION_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPhoneConfirmationDialog(true);
                } else {
                    showPhoneConfirmationDialog(true);
                    Toast.makeText(getContext(),
                            "Não será possível adicionar o código automaticamente para você!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Displays the phone confirmation dialog
     * @param delayed needs a delay to avoid IllegalStateException: Can not perform this action after onSaveInstanceState
     * because of the permission dialog fragment
     */
    private void showPhoneConfirmationDialog(boolean delayed) {

        final String DIALOG_TAG = "phoneConfirmationDialog";

        if(delayed) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PhoneConfirmationDialog dialog = PhoneConfirmationDialog
                            .newInstance(mDialogNumberStateMessage);
                    dialog.setListener(InputNumberFragment.this);
                    dialog.show(getFragmentManager(), DIALOG_TAG);
                }
            }, 400);
        }
        else {
            PhoneConfirmationDialog dialog = PhoneConfirmationDialog
                    .newInstance(mDialogNumberStateMessage);
            dialog.setListener(InputNumberFragment.this);
            dialog.show(getFragmentManager(), DIALOG_TAG);
        }
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return !SettingsUtils.isWaitingForSms(context);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.numberOkButton) {

            mTypedPhone = mPhoneField.getText().toString();
            mTypedEmail = mEmailField.getText().toString();

            if(TextUtils.isEmpty(mTypedEmail) && TextUtils.isEmpty(mTypedPhone)) {
                displayDialog(getContext(), getString(R.string.mandatory_fields_title),
                        getString(R.string.mandatory_fields_text));
                return;
            }

            if (!AndroidUtilities.isEmailValid(mTypedEmail)) {
                displayDialog(getContext(), getString(R.string.invalid_email),
                        getString(R.string.invalid_email_text));
                return;
            }

            if (!isPhoneNumberValid(mTypedPhone)) {
                displayDialog(getContext(), getString(R.string.invalid_phone_number),
                        getString(R.string.invalid_phone_number_text));
                return;
            }

            String phoneDigits = sOnlyNumbers.matcher(mTypedPhone).replaceAll("");
            if (TextUtils.isDigitsOnly(phoneDigits)) {

                String ddd = phoneDigits.substring(0, 2);

                if (mDDDs.containsKey(ddd)) {

                    AndroidUtilities.getInstance(getContext()).hideKeyboard(mPhoneField);

                    String state = mDDDs.get(ddd);

                    mDialogNumberStateMessage = mTypedEmail + "\n\n"
                            + mTypedPhone
                            + " "
                            + state;

                    SettingsUtils.putString(getContext(),
                            SettingsUtils.PREF_KEY_USER_DEFAULT_EMAIL,
                            mTypedEmail);

                    askForPermission();
                }
                else {
                    displayDialog(getContext(), getString(R.string.invalid_state_code),
                            getString(R.string.invalid_state_ddd_text));
                }
            }
        }
        else if (v.getId() == R.id.contactButton) {

            Intent i = Utils.sendContactEmail("", "");

            try {
                startActivity(Intent.createChooser(i, getString(R.string.send_email_contact)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(),
                        getString(R.string.no_email_clients_installed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Displays an explanation for permission of SMS for users using Marshmallow and up
     */
    private void showSmsPermissionExplanationDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("TáNaMão Licitações");
        alertDialog.setMessage("Permita o acesso à SMS para que possamos adicionar o número para você.");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{ Manifest.permission.READ_SMS },
                        AndroidUtilities.PERMISSION_REQUEST_READ_SMS);
            }
        });

        alertDialog.setNegativeButton(R.string.dialog_no_thanks, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPhoneConfirmationDialog(false);
            }
        });

        alertDialog.show();
    }

    /**
     * Formats a phone from 55XXXXXXXXXXX to +55 (XX) XXXXX-XXXX
     * @param phone The phone to be formatted
     */
    private static String formatPhone(String phone) {
        if(phone.length() > 13) {
            throw new RuntimeException("Phone length invalid!");
        }

        String formattedPhone = phone.substring(2);
        formattedPhone = InputNumberFragment.sBetweenParentheses
                .matcher(formattedPhone)
                .replaceFirst("($1) $2");
        formattedPhone = InputNumberFragment.sSeparateDigits
                .matcher(formattedPhone)
                .replaceAll("$1-$2");
        formattedPhone = "+55 " + formattedPhone;
        return formattedPhone;
    }

    /**
     * Called when the user touches the edit button in the PhoneConfimationDialog
     */
    @Override
    public void onEditNumberClick() {
        AndroidUtilities.getInstance(getContext()).showKeyboard(mPhoneField);
    }

    /**
     * Called when the user touches the confirm button in the PhoneConfimationDialog
     */
    @Override
    public void onConfirmNumberClick() {

        String phoneDigits = "55" + sOnlyNumbers.matcher(mTypedPhone).replaceAll("");

        SettingsUtils.putString(getContext(),
                SettingsUtils.PREF_USER_PHONE_NUMBER_FORMATTED,
                mTypedPhone);
        SettingsUtils.putString(getContext(),
                SettingsUtils.PREF_USER_PHONE_NUMBER,
                phoneDigits);


        RequestCodeMutation requestCodeMutation = RequestCodeMutation.builder()
                .phone(phoneDigits)
                .build();

        mRequestCodeCall = mApplication.getApolloClient()
                .newCall(requestCodeMutation)
                .cacheControl(CacheControl.NETWORK_ONLY);
        mRequestCodeCall.enqueue(dataCallback);
    }

    private ApolloCall.Callback<RequestCodeMutation.Data> dataCallback = new ApolloCall.Callback<RequestCodeMutation.Data>() {
        @Override
        public void onResponse(final Response<RequestCodeMutation.Data> response) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), response.data().requestCode().activeTrial() + "",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(ApolloException e) {
        }
    };

    /**
     * Dialog that appears when confirming the typed phone and emails...
     */
    public static class PhoneConfirmationDialog extends DialogFragment {

        private static final String MESSAGE_ARG = "MESSAGE";

        private OnPhoneConfirmationListener mListener;

        public static PhoneConfirmationDialog newInstance(String message) {
            Bundle args = new Bundle();
            args.putString(MESSAGE_ARG, message);
            
            PhoneConfirmationDialog fragment = new PhoneConfirmationDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                    R.style.MaterialBaseTheme_Light_AlertDialog);

            View v = View.inflate(
                    getActivity(),
                    R.layout.dialog_phone_confirmation,
                    null);

            TextView messageView = (TextView) v.findViewById(R.id.phoneAndState);
            messageView.setText(getArguments().getString(MESSAGE_ARG));

            builder.setView(v);
            builder.setPositiveButton(R.string.dialog_confirm_button,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (mListener != null) {
                                mListener.onConfirmNumberClick();
                            }
                        }

                    });
            builder.setNegativeButton(R.string.dialog_edit_button,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                            if (mListener != null) {
                                mListener.onEditNumberClick();
                            }
                        }

                    });

            return builder.create();
        }

        public void setListener(OnPhoneConfirmationListener listener) {
            this.mListener = listener;
        }
    }
 }
