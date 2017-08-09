package com.tnmlicitacoes.app.verifynumber;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.apollo.ConfirmCodeMutation;
import com.tnmlicitacoes.app.interfaces.OnSmsListener;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.model.realm.LocalSupplier;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.service.SmsBroadcastListener;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.ApiUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.List;

import io.realm.Realm;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class WaitingSmsFragment extends VerifyNumberFragment implements
        VerifyNumberActivity.VerifyNumberContent, View.OnClickListener, OnSmsListener {

    /* The logging and fragment tag */
    public static final String TAG = "WaitingSmsFragment";

    /* The enter code text field */
    private TextInputEditText mCodeField;

    /* The timer views */
    private TextView mTimerText;
    private ProgressBar mTimerBar;

    /* The confirm phone number button */
    private Button mConfirmButton;

    /* The contact button */
    private Button mContactButton;

    /* API Apollo confirmCode call */
    private ApolloCall<ConfirmCodeMutation.Data> mConfirmCodeCall;
    private CountDownTimer mTimer;

    private final long MAX_WAITING_TIME = 1000 * 60 * 4;

    /** Realm instance */
    private Realm mRealm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        View view = inflater.inflate(R.layout.fragment_waiting_sms, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        SmsBroadcastListener.sListener = this;

        long remaining = 0;
        long startTime = SettingsUtils.getWaitingForSmsTimestamp(getContext());
        if (startTime > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            remaining = MAX_WAITING_TIME - elapsed;
            if (remaining <= 0) {
                remaining = MAX_WAITING_TIME;
            }
        } else {
            remaining = MAX_WAITING_TIME;
        }

        mTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                calcProgress(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                calcProgress(0);
                SettingsUtils.putLong(getContext(),
                        SettingsUtils.PREF_IS_WAITING_FOR_SMS_TIMESTAMP,
                        0);
            }
        }.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
        mTimer = null;
        SmsBroadcastListener.sListener = null;
    }

    private void initViews(View view) {
        mCodeField     = (TextInputEditText) view.findViewById(R.id.codeField);
        mTimerText     = (TextView) view.findViewById(R.id.timerText);
        mTimerBar      = (ProgressBar) view.findViewById(R.id.timerBar);
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
                        .mutate(confirmCode);

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
        public void onResponse(final Response<ConfirmCodeMutation.Data> response) {

            if (!response.hasErrors()) {
                ConfirmCodeMutation.ConfirmCode confirmCode = response.data().confirmCode();
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

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLocalSupplier(response.data().confirmCode().supplier());
                        }
                    });
                }

                if (mListener != null) {
                    mListener.onRegisterFinished(confirmCode.refreshToken(),
                            confirmCode.accessToken());
                }
            } else {

                final ApiUtils.ApiError error = ApiUtils.getFirstValidError(mActivity, response.errors());
                if (error != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LOG_DEBUG(TAG, "Code: " + error.getCode());
                            LOG_DEBUG(TAG, error.getCode() + "");
                        }
                    });
                }
            }

        }

        @Override
        public void onFailure(final ApolloException e) {
            if (mListener != null) {
                e.printStackTrace();
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

    private void updateLocalSupplier(ConfirmCodeMutation.Supplier supplier) {
        LocalSupplier local = new LocalSupplier();
        local.setId(supplier.id());
        local.setName(supplier.name());
        local.setEmail(supplier.email());
        local.setActivated(supplier.activated());
        if (supplier.cityNum() != null) {
            local.setCityNum(supplier.cityNum());
        } else {
            local.setCityNum(SubscriptionPlan.BASIC_MAX_QUANTITY);
        }

        if (supplier.segNum() != null) {
            local.setSegNum(supplier.segNum());
        } else {
            local.setSegNum(SubscriptionPlan.BASIC_MAX_QUANTITY);
        }

        local.setPhone(supplier.phone());
        local.setDefaultCard(supplier.defaultCard());

        // TODO(diego): Set the picked cities and segments if we already have
        mRealm.beginTransaction();

        if (supplier.cities() != null) {
            int size = supplier.cities().edges().size();
            StringBuilder sb = new StringBuilder();

            mRealm.where(PickedCity.class).findAll().deleteAllFromRealm();

            for (int i = 0; i < size; i++) {
                ConfirmCodeMutation.Node1 node = supplier.cities().edges().get(i).node();
                sb.append(node.id());
                if (i + 1 < size) {
                    sb.append(";");
                }

                PickedCity pickedCity = new PickedCity();
                pickedCity.setId(node.id());
                pickedCity.setName(node.name());
                pickedCity.setState(node.state().name());
                mRealm.copyToRealmOrUpdate(pickedCity);
            }
            local.setCities(sb.toString());
        }

        if (supplier.segments() != null) {
            int size = supplier.segments().edges().size();
            StringBuilder sb = new StringBuilder();

            mRealm.where(PickedSegment.class).findAll().deleteAllFromRealm();

            for (int i = 0; i < size; i++) {
                ConfirmCodeMutation.Node node = supplier.segments().edges().get(i).node();
                sb.append(node.id());
                if (i + 1 < size) {
                    sb.append(";");
                }

                PickedSegment pickedSegment = new PickedSegment();
                pickedSegment.setId(node.id());
                pickedSegment.setName(node.name());
                pickedSegment.setIcon(node.icon());
                mRealm.copyToRealmOrUpdate(pickedSegment);
            }
            local.setSegments(sb.toString());
        }

        mRealm.copyToRealmOrUpdate(local);
        mRealm.commitTransaction();
    }

    @Override
    public void onSmsReceived(String verificationCode) {

    }

    /**
     * Gets the time formatted in MM:SS ex: 4:08
     */
    private String getFormattedTime(long currentMillis) {
        int remainingInSeconds = (int) (currentMillis / Utils.SECOND_IN_MILLIS);
        String minutes = Integer.toString((remainingInSeconds / 60) < 0
                ? 0 : remainingInSeconds / 60);
        String seconds = addZero(remainingInSeconds % 60 < 0
                ? 0 : remainingInSeconds % 60);
        return minutes + ":" + seconds;
    }

    /**
     * Adds a zero as prefix in an number that is lower than 10
     * @param i The number to be prefixed
     * @return A string with zero added or not
     */
    private String addZero(int i) {
        if(i < 10) {
            return "0" + i;
        }
        return Integer.toString(i);
    }

    /**
     * Calculates the progress of time
     */
    private void calcProgress(long millisUntilFinished) {
        long elapsedTime = MAX_WAITING_TIME - millisUntilFinished;
        int progress = (int) ((elapsedTime * 100) / MAX_WAITING_TIME);

        updateTimerDisplay(getFormattedTime(millisUntilFinished), progress);
    }

    private void updateTimerDisplay(String formattedTime, int progress) {
        mTimerText.setText(formattedTime);
        mTimerBar.setProgress(progress);
    }

}
