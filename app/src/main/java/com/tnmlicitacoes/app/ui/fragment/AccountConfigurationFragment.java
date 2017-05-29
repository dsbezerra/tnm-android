package com.tnmlicitacoes.app.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.tnmlicitacoes.app.interfaces.OnAccountConfigurationListener;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;

public abstract class AccountConfigurationFragment extends Fragment implements OnClickListenerRecyclerView {

    private Context mContext;

    OnAccountConfigurationListener mCallback;

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
}
