package com.tnmlicitacoes.app.registration

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tnmlicitacoes.app.R
import com.tnmlicitacoes.app.utils.AndroidUtilities
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar


class ConclusionFragment : RegistrationFragment(), RegistrationActivity.RegistrationContent {

    override fun getLogTag(): String {
        return InputNameFragment.TAG
    }

    /** Whether the request is in progress or not */
    private var mIsPreparing = true

    /** Displays the progress bar */
    private var mProgressBar: SmoothProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_conclusion, container, false)
        initViews(view)

        mListener?.onStartRegistration()
        return view
    }

    private fun initViews(view: View?) {
        mProgressBar = view?.findViewById(R.id.progress_bar) as SmoothProgressBar
    }

    fun setIsPreparing(value: Boolean) {
        mIsPreparing = value
    }

    fun setIsPreparing(value: Boolean, status: Boolean) {
        mIsPreparing = value

        if (!value) {

            // Delay a little bit...
            Handler().postDelayed({
                mProgressBar?.visibility = View.GONE
                mListener?.onFinishRegistration(status)
            }, 2000)

        }
    }

    override fun setError(message: String) {
        // Empty
    }

    override fun getTitle(): String? {
        if (mIsPreparing) {
            return "Preparando aplicação..."
        }

        return "Tudo certo."
    }

    override fun getDescription(): String? {
        if (mIsPreparing) {
            return "Estamos configurando tudo para você, aguarde só um momento!"
        }

        return "Configuração finalizada!"
    }

    override fun getToolbarHeight(activity: Activity): Int {
        return AndroidUtilities.dp(activity, 140.0f)
    }

    companion object {

        /** The fragment and logging tag */
        val TAG = "ConclusionFragment"
    }
}
