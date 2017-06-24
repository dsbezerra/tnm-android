package com.tnmlicitacoes.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseDialogFragment;

public class ShowErrorDialog extends BaseDialogFragment {

    /* The logging and dialog tag */
    public static final String TAG = "ShowErrorDialog";

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    /* Displays the error title */
    private TextView mTitle;

    /* Displays the error message */
    private TextView mMessage;

    /* The dismiss button */
    private Button mOkButton;

    public static ShowErrorDialog newInstance(String title, String message) {

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);

        ShowErrorDialog fragment = new ShowErrorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_show_error, container, false);
        initViews(view);
        fill();
        return view;
    }

    private void initViews(View view) {
        mTitle = (TextView) view.findViewById(R.id.error_title);
        mMessage = (TextView) view.findViewById(R.id.error_message);
        mOkButton = (Button) view.findViewById(R.id.ok_btn);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    /* Fill layout views */
    private void fill() {
        Bundle args = getArguments();
        String title = args.getString(TITLE);
        String message = args.getString(MESSAGE);
        mTitle.setText(title);
        mMessage.setText(message);
    }
}
