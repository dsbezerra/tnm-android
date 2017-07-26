package com.tnmlicitacoes.app.subscription;

import android.content.Context;

import com.tnmlicitacoes.app.interfaces.OnSubscriptionListener;
import com.tnmlicitacoes.app.ui.base.BaseFragment;


public abstract class SubscriptionFragment extends BaseFragment {

    public static final String TAG = "SubscriptionFragment";

    protected SubscriptionActivity mActivity;

    /* Listener for subscription events */
    protected OnSubscriptionListener mSubscriptionListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (SubscriptionActivity) context;
        mSubscriptionListener = (SubscriptionActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mSubscriptionListener = null;
    }
}
