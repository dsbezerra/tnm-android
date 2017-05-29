package com.tnmlicitacoes.app.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.utils.Utils;

/**
 * CustomEditTextPeference
 * Custom edittext for allowing five
 */

public class CustomEditTextPreference extends DialogPreference {

    private static final int MAX_LENGTH = 1000;

    private Context     mContext;
    private EditText    mEditText;
    private TextView    mLeftEmailsView;

    private int         mLeftEmails;
    private String      mDefaultValue;

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mLeftEmails = Utils.MAX_EMAILS;
        setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_emails);
        setTitle(R.string.pref_email_label);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    private void initViews(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        mDefaultValue = sharedPreferences.getString(mContext.getString(R.string.pref_email_key),
                mContext.getString(R.string.pref_email_default));

        int counter = getNumOfChars(mDefaultValue, ',');
        mLeftEmails = Utils.MAX_EMAILS - counter - 1;

        mLeftEmailsView = (TextView) view.findViewById(R.id.left_emails);
        mEditText = (EditText) view.findViewById(R.id.etEmails);

        mEditText.setText(mDefaultValue);
        mLeftEmailsView.setText(mLeftEmails + "");
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String currentTextString = s.toString();
                int currentTextLength = currentTextString.length();
                int lastCharIndex = currentTextLength - 1;

                if (mLeftEmails <= 0 && currentTextString.charAt(lastCharIndex) == ',') {
                    mLeftEmails = 0;
                    setMaxLength(lastCharIndex - 1);
                }

                String emails = mEditText.getText().toString();
                int counter = getNumOfChars(emails, ',');

                mLeftEmails = Utils.MAX_EMAILS - counter - 1;
                if (mLeftEmails < 0) mLeftEmails = 0;
                if (mLeftEmails == 0 && counter == 5)
                    setMaxLength(lastCharIndex - 1);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String editText = mEditText.getText().toString();
                int afterLength = editText.length();
                if (afterLength == 0) mLeftEmails = Utils.MAX_EMAILS;

                if (afterLength != 0)
                    if (mLeftEmails >= 0 && editText.charAt(afterLength - 1) != ',')
                        setMaxLength(-1);

                mLeftEmailsView.setText(mLeftEmails + "");
            }

        });
    }

    @Override
    protected void onBindDialogView(View view) {
        initViews(view);
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String value = mEditText.getText().toString();
            SharedPreferences.Editor editor = getEditor();
            editor.putString(mContext.getResources().getString(R.string.pref_email_key), value);
            editor.commit();
            setSummary(value);
        }
    }

    private void setMaxLength(int value) {
        if(value < 0)
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH)});
        else
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(value)});
    }

    private int getNumOfChars(String s, char c) {
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                counter++;
            }
        }
        return counter;
    }

}
