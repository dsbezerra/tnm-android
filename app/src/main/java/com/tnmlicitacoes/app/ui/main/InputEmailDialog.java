package com.tnmlicitacoes.app.ui.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseDialogFragment;

import org.w3c.dom.Text;

public class InputEmailDialog extends BaseDialogFragment implements View.OnClickListener {

    /* The logging and fragment tag */
    public static final String TAG = "InputEmailDialog";

    /* The email field */
    private TextInputEditText mEmailField;

    /* The confirm button */
    private Button mConfirmButton;

    /* The dismiss button */
    private Button mCancelButton;

    /* Callback for button clicks */
    private InputEmailCallback mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_input_email, container, false);
        mEmailField = (TextInputEditText) view.findViewById(R.id.emailField);
        mConfirmButton = (Button) view.findViewById(R.id.confirm);
        mCancelButton = (Button) view.findViewById(R.id.cancel);

        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        return view;
    }

    public InputEmailDialog withCallback(InputEmailCallback callback) {
        this.mCallback = callback;
        return this;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        switch (id) {

            case R.id.confirm:

                String email = mEmailField.getText().toString().trim();
                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailField.setError("E-mail inválido!");
                    break;
                }

                if (mCallback != null) {
                    mCallback.onConfirm(email);
                    dismiss();
                } else {
                    throw new Error(InputEmailDialog.this.toString()
                            + " must register a callback");
                }

                break;

            case R.id.cancel:
                dismiss();
                break;
        }
    }

    interface InputEmailCallback {
        void onConfirm(String typedEmail);
    }
}
