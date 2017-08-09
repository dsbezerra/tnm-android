package com.tnmlicitacoes.app.registration

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.tnmlicitacoes.app.R
import com.tnmlicitacoes.app.model.realm.LocalSupplier
import com.tnmlicitacoes.app.utils.SettingsUtils
import com.tnmlicitacoes.app.utils.UIUtils

class InputEmailFragment : RegistrationFragment(), RegistrationActivity.RegistrationContent {
    override fun setError(message: String) {
        mEmail?.error = message
    }

    override fun getLogTag(): String {
        return InputNameFragment.TAG
    }

    /* The email edit text layout */
    private lateinit var mEmailLayout: TextInputLayout

    /* Display the input field for the email */
    private var mEmail: EditText? = null

    private var mListeningForChanges = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_input_email, container, false)
        initViews(view, savedInstanceState)
        return view
    }

    override fun onResume() {
        super.onResume()
        mListener?.onEmailChanged(mEmail?.text.toString().trim())
    }

    private fun initViews(view: View?, savedInstanceState: Bundle?) {
        mEmailLayout = view?.findViewById(R.id.email_layout) as TextInputLayout
        mEmail = view.findViewById(R.id.email_value) as EditText
        mEmail?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No-op
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No-op
            }
            override fun afterTextChanged(s: Editable?) {
                notifyChanges()
            }
        })

        mEmail?.setOnFocusChangeListener {
            _, hasFocus -> if (hasFocus) {
            mEmailLayout.hint = "E-mail"
        } else {
            mEmailLayout.hint = resources.getString(R.string.email_input_hint)
        } }

        // Fill field if we have an email
        val email = SettingsUtils.getUserDefaultEmail(activity);
        if (!TextUtils.isEmpty(email) && savedInstanceState == null) {
            mEmail?.setText(email)
            mEmailLayout.hint = "E-mail"
            mEmail?.setSelection(email.length)
        }

        notifyChanges()
    }

    private fun notifyChanges() {
        val text = mEmail?.text.toString()

        if (mEmail?.length() != 0 && !mListeningForChanges) {
            mListeningForChanges = true
            UIUtils.setRightDrawable(mEmail!!, R.drawable.ic_clear_select_search_field)
            // Listens for click on the clear drawable
            mEmail?.setOnTouchListener({ _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableLeftX = mEmail!!.right - mEmail!!.compoundDrawables[2].bounds.width()
                    if (event.x >= drawableLeftX && mEmail?.length() != 0) {
                        clearField()
                    }
                }

                false
            })
        } else if (mEmail?.length() == 0) {
            clearField()
        }

        mListener?.onEmailChanged(text.trim())
    }

    fun clearField() {
        mListeningForChanges = false
        mEmail?.text!!.clear()
        mEmail?.setOnTouchListener(null)
        UIUtils.setRightDrawable(mEmail!!, UIUtils.NO_DRAWABLE)
    }

    override fun shouldEnableAdvanceButton(): Boolean {
        return mEmail?.length() != 0 && Patterns.EMAIL_ADDRESS
                .matcher(mEmail?.text.toString()).matches()
    }


    override fun shouldDisplay(localSupplier: LocalSupplier?): Boolean {
        if (localSupplier == null) {
            return true;
        }

        return TextUtils.isEmpty(localSupplier.email)
    }

    override fun shouldDisplayBackArrow(): Boolean {
        return true
    }

    override fun getFocusView(): View? {
        if (mEmail?.isFocusable!! && mEmail?.hasFocus()!!) {
            return mEmail
        }
        return super.getFocusView()
    }

    companion object {

        /* The logging tag */
        val TAG = "InputEmailFragment"
    }

}