package com.tnmlicitacoes.app.registration

import android.content.Context
import android.os.Bundle
import com.tnmlicitacoes.app.ui.base.BaseFragment

abstract class RegistrationFragment : BaseFragment() {

    /** The registration listener */
    protected var mListener: OnRegistrationListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = context as OnRegistrationListener
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnRegistrationListener {

        /**
         * Called when the name field changes
         */
        fun onNameChanged(name: String)

        /**
         * Called when the email field changes and is valid
         **/
        fun onEmailChanged(email: String)

        /**
         * Called when the updateSupplier mutation is executed.
         */
        fun onStartRegistration()

        /**
         * Called when the response from updateSupplier mutation returns.
         * @param status Whether the mutation occurred successfully or not
         */
        fun onFinishRegistration(status: Boolean)
    }
}