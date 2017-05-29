package com.tnmlicitacoes.app.verifynumber;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.ConfirmCodeMutation;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.activity.AccountConfigurationActivity;
import com.tnmlicitacoes.app.ui.fragment.VerifyNumberFragment;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class WaitingSmsFragment extends VerifyNumberFragment implements
        VerifyNumberActivity.VerifyNumberContent, View.OnClickListener {

    /* Tag for logging */
    private static final String TAG = "WaitingSmsFragment";

    /* The enter code text field */
    private TextInputEditText mCodeField;

    /* The confirm phone number button */
    private Button mConfirmButton;

    /* The contact button */
    private Button mContactButton;

    /* API Apollo confirmCode call */
    private ApolloCall<ConfirmCodeMutation.Data> mConfirmCodeCall;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiting_sms_new, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mCodeField     = (TextInputEditText) view.findViewById(R.id.codeField);
        mConfirmButton = (Button) view.findViewById(R.id.confirmBtn);
        mContactButton = (Button) view.findViewById(R.id.contactBtn);
        mConfirmButton.setOnClickListener(this);
        mContactButton.setOnClickListener(this);
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return SettingsUtils.isWaitingForSms(context);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirmBtn) {

            boolean cancel = false;

            String code = mCodeField.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                mCodeField.setError("Campo obrigatório!");
                cancel = true;
            } else if (code.length() < 6) {
                //mCodeField.setError("Campo obrigatório!");
            }

            if (!cancel) {

                String phone = SettingsUtils.getUserPhoneNumber(mActivity);

                ConfirmCodeMutation confirmCode = ConfirmCodeMutation.builder()
                        .phone(phone)
                        .code(code)
                        .deviceId(AndroidUtilities.getDeviceToken())
                        .build();

                mConfirmCodeCall = mApplication.getApolloClient()
                        .newCall(confirmCode)
                        .cacheControl(CacheControl.NETWORK_ONLY);

                mConfirmCodeCall.enqueue(dataCallback);
            }

        } else if (v.getId() == R.id.contactBtn) {
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
     * Callback for the confirmCode mutation call
     */
    private ApolloCall.Callback<ConfirmCodeMutation.Data> dataCallback = new ApolloCall.Callback<ConfirmCodeMutation.Data>() {
        @Override
        public void onResponse(Response<ConfirmCodeMutation.Data> response) {

            if (response.isSuccessful()) {
                ConfirmCodeMutation.Data.ConfirmCode confirmCode = response.data().confirmCode();
                boolean isCodeValid = confirmCode.validCode();
                if (!isCodeValid) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCodeField.setError("Código inválido!");
                        }
                    });
                    return;
                }

                if (mListener != null) {
                    mListener.onRegisterFinished(confirmCode.refreshToken(),
                            confirmCode.accessToken());
                }
            }

        }

        @Override
        public void onFailure(ApolloException e) {
            if (mListener != null) {
                LOG_DEBUG(TAG, e.getMessage());
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, R.string.server_communication_error,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };
}
