package com.tnmlicitacoes.app.registration

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.tnmlicitacoes.app.R
import com.tnmlicitacoes.app.model.realm.LocalSupplier
import com.tnmlicitacoes.app.utils.LogUtils
import com.tnmlicitacoes.app.utils.SettingsUtils
import com.tnmlicitacoes.app.utils.UIUtils

class InputNameFragment : RegistrationFragment(), RegistrationActivity.RegistrationContent {

    override fun getLogTag(): String {
        return TAG
    }

    /* Display the input field for the name */
    private var mName: EditText? = null

    private var mListeningForChanges = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_input_name, container, false)
        initViews(view, savedInstanceState)
        return view
    }

    private fun initViews(view: View?, savedInstanceState: Bundle?) {
        mName = view?.findViewById(R.id.name_value) as EditText
        mName?.addTextChangedListener(object : TextWatcher {
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

        // Fill field if we have a name
        val name = SettingsUtils.getUserName(activity);
        if (!TextUtils.isEmpty(name) && savedInstanceState == null) {
            mName?.setText(name)
            mName?.setSelection(name.length)
        }

        notifyChanges()
    }

    private fun notifyChanges() {
        val text = mName?.text.toString()

        if (mName?.length() != 0 && !mListeningForChanges) {
            mListeningForChanges = true
            UIUtils.setRightDrawable(mName!!, R.drawable.ic_clear_select_search_field)
            // Listens for click on the clear drawable
            mName?.setOnTouchListener({ _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableLeftX = mName?.right!! - mName?.compoundDrawables!![2].bounds.width()
                    if (event.x >= drawableLeftX && mName?.length() != 0) {
                        clearField()
                    }
                }

                false
            })
        } else if (mName?.length() == 0) {
            clearField()
        }

        mListener?.onNameChanged(text.trim())
    }

    fun clearField() {
        mListeningForChanges = false
        mName?.text!!.clear()
        mName?.setOnTouchListener(null)
        UIUtils.setRightDrawable(mName!!, UIUtils.NO_DRAWABLE)
    }

    override fun shouldEnableAdvanceButton(): Boolean {
        return mName?.length() != 0
    }

    override fun setError(message: String) {
        mName?.error = message
    }

    override fun getFocusView(): View? {
        if (mName?.isFocusable!! && mName?.hasFocus()!!) {
            return mName
        }
        return super.getFocusView()
    }

    override fun shouldDisplay(localSupplier: LocalSupplier?): Boolean {
        if (localSupplier == null) {
            return true;
        }

        return TextUtils.isEmpty(localSupplier.name)
    }

    companion object {

        /* The logging tag */
        val TAG = "InputNameFragment"
    }
}