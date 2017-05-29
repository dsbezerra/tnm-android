package com.tnmlicitacoes.app.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnSmsListener;
import com.tnmlicitacoes.app.service.SmsBroadcastListener;
import com.tnmlicitacoes.app.ui.activity.VerifyNumberActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.HashMap;

public class WaitingSmsFragment extends VerifyNumberFragment implements VerifyNumberActivity.VerifyNumberContent, OnSmsListener, View.OnClickListener {

    private static final String TAG = "WaitingSmsFragment";

    private TextView mPhoneText;

    private TextView mTimerText;

    private ProgressBar mTimerBar;

    private ImageButton mPhoneEdit;

    private EditText mCodeField;

    private Button mNotReceived;

    private WaitingTimer mTimer;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_waiting_sms, container, false);
        initViews(v);
        initListeners();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTimer = new WaitingTimer();
        mPhoneText.setText(SettingsUtils.getUserPhoneFormattedNumber(getContext()));
    }

    @Override
    public void onStart() {
        super.onStart();
        SmsBroadcastListener.sListener = this;
        mTimer.start();
        restoreTimerRemainingTime();
        mCodeField.requestFocus();
    }

    /**
     * Restore the timer remaining time if
     */
    private void restoreTimerRemainingTime() {
        long lastTime = PreferenceManager
                .getDefaultSharedPreferences(getContext()).getLong("lastTime", 0);
        if(lastTime > 0) {
            long diff = System.currentTimeMillis() - lastTime;
            mTimer.setRemainingTime(SettingsUtils.getRemainingSmsWaitingTime(getContext()) - diff);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.stop();
        SmsBroadcastListener.sListener = null;
    }

    /**
     * Initialize all views
     * @param view
     */
    private void initViews(View view) {
        mTimerText = (TextView) view.findViewById(R.id.timerText);
        mPhoneText = (TextView) view.findViewById(R.id.phoneText);
        mPhoneEdit = (ImageButton) view.findViewById(R.id.editPhone);
        mCodeField = (EditText) view.findViewById(R.id.codeField);
        mTimerBar = (ProgressBar) view.findViewById(R.id.timerBar);
        mNotReceived = (Button) view.findViewById(R.id.smsNotReceived);
    }

    /**
     * Initialize all view listeners
     */
    private void initListeners() {
        mPhoneEdit.setOnClickListener(this);
        mNotReceived.setOnClickListener(this);
        mCodeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final String typedCode = mCodeField.getText().toString();
                if(typedCode.length() == 6) {
                    performVerification(typedCode);
                }
            }
        });
    }

    /**
     * Updates the interface
     * @param currentTime
     * @param progress
     */
    private void updateUi(String currentTime, int progress) {
        mTimerText.setText(currentTime);

        if(progress > 100) progress = 100;
        mTimerBar.setProgress(progress);
        switchButtonStatus(progress == 100, mNotReceived);
    }

    /**
     * Disable or enable  a button
     * @param enabled
     * @param button
     */
    private void switchButtonStatus(boolean enabled, Button button) {
        button.setEnabled(enabled);
        button.setTextColor(enabled ?
                getContext().getResources().getColor(R.color.md_white_1000) :
                getContext().getResources().getColor(R.color.md_grey_300));
    }

    /**
     *
     * @param code
     */
    private void performVerification(final String code) {
        AndroidUtilities utilities = AndroidUtilities.getInstance(getContext());
        utilities.hideKeyboard(mCodeField);

        //final String deviceId = AndroidUtilities.generateDeviceId();
        final String phone = SettingsUtils.getUserPhoneNumber(getContext());

        final HashMap<String, Object> parameters = new HashMap<String, Object>()
        {{
            put(PHONE, phone);
            put(SECRET, code);
            //put(DEVICE_ID, deviceId);
        }};
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return SettingsUtils.isWaitingForSms(context);
    }

    @Override
    public void onSmsReceived(String verificationCode) {
        //performVerification(verificationCode);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.editPhone) {

        }
        else if (id == R.id.smsNotReceived) {

        }
    }

    public class WaitingTimer implements Runnable {

        private long MAX_WAITING_TIME_IN_MILLIS = 1000 * 60 * 5;

        private long mCurrentWaitingTime = MAX_WAITING_TIME_IN_MILLIS;

        private Handler mHandler;

        private boolean isRunning = false;

        public WaitingTimer() {
            mHandler = new Handler();
        }

        /**
         * Main timer loop
         */
        @Override
        public void run() {
            decreaseTime();
            calcProgress();
        }

        /**
         * Decrease the timer by one second until it's zero
         */
        private void decreaseTime() {
            mCurrentWaitingTime -= 1000;
            if(mCurrentWaitingTime > 0) {
                mHandler.postDelayed(this, 1000);
            }
        }

        /**
         * Calculates the progress of time
         */
        private void calcProgress() {
            long elapsedTime = MAX_WAITING_TIME_IN_MILLIS - mCurrentWaitingTime;
            int progress = (int) ((elapsedTime * 100) / MAX_WAITING_TIME_IN_MILLIS);

            updateUi(getFormattedTime(), progress);
        }

        /**
         * Init the timer
         */
        public void start() {
            if(!isRunning) {
                mCurrentWaitingTime = MAX_WAITING_TIME_IN_MILLIS;
                mHandler.post(this);
                isRunning = true;
            }
        }

        /**
         * Stops the timer
         */
        public void stop() {
            if(mHandler != null) {
                mHandler.removeCallbacks(this);
            }

            if(isRunning) {
                SettingsUtils.putLong(getContext(),
                        "lastTime",
                        System.currentTimeMillis());
                SettingsUtils.putLong(getContext(),
                        SettingsUtils.PREF_WAITING_SMS_REMAINING_TIME,
                        mCurrentWaitingTime);
                isRunning = false;
            }
        }

        public void reset() {
            mCurrentWaitingTime = MAX_WAITING_TIME_IN_MILLIS;
            SettingsUtils.putLong(getContext(),
                    "lastTime",
                    0);
            isRunning = false;
        }

        /**
         * Sets the remaining time for the Timer
         */
        public void setRemainingTime(long time) {
            if(time <= 0) {
                time = 0;
                mNotReceived.setEnabled(true);
            }
            this.mCurrentWaitingTime = time;

        }

        /**
         * Gets the current remaining time in the Timer
         */
        public long getRemainingTime() {
            return mCurrentWaitingTime;
        }

        /**
         * Gets the time formatted in MM:SS ex: 4:08
         */
        private String getFormattedTime() {
            int remainingInSeconds = (int) (mCurrentWaitingTime / Utils.SECOND_IN_MILLIS);
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
    }
 }
