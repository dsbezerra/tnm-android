package com.tnmlicitacoes.app.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TNMApplication;
import com.tnmlicitacoes.app.interfaces.OnVerifyNumberListener;
import com.tnmlicitacoes.app.verifynumber.VerifyNumberActivity;

public abstract class VerifyNumberFragment extends Fragment {

    private static final String TAG = "VerifyNumberFragment";

    protected static final String PHONE = "phone";
    protected static final String SECRET = "secret";
    protected static final String DEVICE_ID = "deviceId";

    /* Listener for api calls */
    protected OnVerifyNumberListener mListener;

    /* The parent activity */
    protected VerifyNumberActivity mActivity;

    /* The app singleton */
    protected TNMApplication mApplication;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (VerifyNumberActivity) getActivity();
        mApplication = (TNMApplication) mActivity.getApplication();
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

        AlertDialog.Builder alertDialog;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            alertDialog = new AlertDialog.Builder(context,
                    R.style.MaterialBaseTheme_Light_AlertDialog);
        }
        else {
            alertDialog = new AlertDialog.Builder(context);
        }

        alertDialog.setTitle(title);
        alertDialog.setMessage(message)
                .setPositiveButton(R.string.dialog_close_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog.show();
    }
}

