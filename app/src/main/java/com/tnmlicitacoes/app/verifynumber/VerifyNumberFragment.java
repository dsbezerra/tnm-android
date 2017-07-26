package com.tnmlicitacoes.app.verifynumber;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.OnVerifyNumberListener;

public abstract class VerifyNumberFragment extends Fragment {

    /* The logging tag */
    private static final String TAG = "VerifyNumberFragment";

    /* Listener for api calls */
    protected OnVerifyNumberListener mListener;

    /* The parent activity */
    protected VerifyNumberActivity mActivity;

    /* The app singleton */
    protected TnmApplication mApplication;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (VerifyNumberActivity) getActivity();
        mApplication = (TnmApplication) mActivity.getApplication();
        mListener = mActivity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
        mApplication = null;
        mListener = null;
    }

    /**
     * Displays a simple AlertDialog
     * @param message Message of dialog
     */
    protected void displayDialog(Context context, String title, int message) {
        displayDialog(context, title, getString(message));
    }

    /**
     * Displays a simple AlertDialog
     * @param message Message of dialog
     */
    protected void displayDialog(Context context, String title, String message) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message)
                .setPositiveButton(R.string.dialog_close_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog.show();
    }
}

