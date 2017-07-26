package com.tnmlicitacoes.app.accountconfiguration;

import android.app.Activity;
import android.content.Context;

import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.ui.base.BaseFragment;

public abstract class AccountConfigurationFragment extends BaseFragment
        implements OnClickListenerRecyclerView {

    /* The context of the application */
    private Context mContext;

    /* Callback for when a the state of configuration changes */
    protected OnAccountConfigurationListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        try {
            if(mContext instanceof Activity) {
                mCallback = (OnAccountConfigurationListener) mContext;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(mContext.toString()
                    + " must implement OnAccountConfigurationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    /* Get the maximum allowed to pick */
    public abstract int getMaximum();
}
