package com.tnmlicitacoes.app.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.DrawableRes
import android.support.design.widget.Snackbar
import android.support.v7.content.res.AppCompatResources
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.support.design.widget.CoordinatorLayout
import android.support.annotation.NonNull





class UIUtils {
    companion object {

        val NO_DRAWABLE = 0;

        /**
         * Helper method to get the theme color from an attribute resource
         */
        fun getThemeColorFromAttr(context: Context, @AttrRes attr: Int): Int {
            var typedValue = TypedValue()
            if (context.theme.resolveAttribute(attr, typedValue, true)) {
                return typedValue.data
            }

            return 0
        }

        /**
         * Helper method that sets the elevation of a view
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun setElevation(view: View, activity: Activity, elevation: Float) {
            view.elevation = AndroidUtilities.dp(activity, elevation)
                    .toFloat()
        }

        /**
         * Helper method to set right drawable to a view
         */
        fun setRightDrawable(view: EditText, @DrawableRes drawableRes: Int) {
            val drawable = getDrawableFromResId(view.context, drawableRes)
            view.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }

        /**
         * Helper method to get a drawable
         */
        fun getDrawableFromResId(context: Context, @DrawableRes drawableRes: Int = NO_DRAWABLE): Drawable? {
            if (drawableRes == NO_DRAWABLE) {
                return null
            }
            return AppCompatResources.getDrawable(context, drawableRes)
        }

        /**
         * Helper method that creates a Snackbar above some other view
         *
         * @param rootView The view to find a parent from.
         * @param targetView The view used as target to compute the position.
         * @param message The text to show. Can be formatted text.
         * @param duration How long to display the message.
         *                 Either Snackbar.LENGTH_SHORT or Snackbar.LENGTH_LONG
         *
         */
        fun createSnackbarAbove(rootView: View, targetView: View, message: CharSequence,
                              duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
            val snackbar = Snackbar.make(rootView, message, duration)
            val params = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams

            val targetHeight = targetView.height
            params.bottomMargin = targetHeight
            snackbar.view.layoutParams = params
            return snackbar
        }
    }
}