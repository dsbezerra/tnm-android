package com.tnmlicitacoes.app.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;
import com.tnmlicitacoes.app.interfaces.OnUpdateListener;
import com.tnmlicitacoes.app.main.mynotices.MyNoticesFragment;
import com.tnmlicitacoes.app.settings.SettingsActivity;
import com.tnmlicitacoes.app.accountconfiguration.AccountConfigurationActivity;
import com.tnmlicitacoes.app.details.DetailsActivity;
import com.tnmlicitacoes.app.main.MainActivity;
import com.tnmlicitacoes.app.ui.activity.SplashScreenActivity;
import com.tnmlicitacoes.app.ui.activity.WebviewActivity;
import com.tnmlicitacoes.app.main.account.AccountFragment;
import com.tnmlicitacoes.app.verifynumber.VerifyNumberActivity;

//import com.tnmlicitacoes.app.ui.activity.VerifyNumberActivity;

public class AndroidUtilities {

    private static final String TAG = "AndroidUtilities";

    public static OnUpdateListener sOnUpdateListener = null;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final int PERMISSION_REQUEST_WRITE_EXT_STORAGE = 201;

    public static final int PERMISSION_REQUEST_READ_SMS = 202;

    // Activities IDs
    private static final short SPLASH_ACTIVITY             = 1000;
    private static final short INTRO_ACTIVITY              = 1001;
    private static final short VERIFY_NUMBER_ACTIVITY      = 1002;
    private static final short INITIAL_CONFIG_ACTIVITY     = 1003;
    private static final short MAIN_ACTIVITY               = 1004;
    private static final short MY_SUBS_ACTIVITY            = 1005;
    private static final short SETTINGS_ACTIVITY           = 1006;
    private static final short MY_BIDDINGS_ACTIVITY        = 1007;
    private static final short WEB_VIEW_ACTIVITY           = 1008;
    private static final short DETAILS_ACTIVITY            = 1009;

    // Classname = full package name + class name
    // ex: com.empresa.app.MainActivity
    public static Class<?> getClassByName(Context context, String className) {
        Class<?> cls = null;
        if (className != null) {
            try {
                cls = Class.forName(context.getPackageName() + className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cls;
    }

    /**
     * Get .class from class id,
     * see constants on top for ids...
     */
    public static Class<?> getClassById(int clazzId) {
        Class<?> clazz;
        switch (clazzId) {
            case SPLASH_ACTIVITY:
            {
                clazz = SplashScreenActivity.class;
            } break;
            case VERIFY_NUMBER_ACTIVITY:
            {
                clazz = VerifyNumberActivity.class;
            } break;
            case INITIAL_CONFIG_ACTIVITY:
            {
                clazz = AccountConfigurationActivity.class;
            } break;
            case MAIN_ACTIVITY:
            {
                clazz = MainActivity.class;
            } break;
            case MY_SUBS_ACTIVITY:
            {
                clazz = AccountFragment.class;
            } break;
            case SETTINGS_ACTIVITY:
            {
                clazz = SettingsActivity.class;
            } break;
            case MY_BIDDINGS_ACTIVITY:
            {
                clazz = MyNoticesFragment.class;
            } break;
            case WEB_VIEW_ACTIVITY:
            {
                clazz = WebviewActivity.class;
            } break;
            case DETAILS_ACTIVITY:
            {
                clazz = DetailsActivity.class;
            } break;
            default:
            {
                clazz = MainActivity.class;
            } break;
        }
        return clazz;
    }

    // Create a intent and clear the stack
    public Intent createClearStackIntent(Context context, Class<?> cls) {
        Intent intent = null;
        if (cls != null) {
            try {
                intent = new Intent(context, cls);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return intent;
    }

    public boolean isAboveAPI10 () {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isEmailValid(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int dp(Activity activity, float value) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(metrics.density * value);
    }

    public static String getDeviceToken() {
        // We are going to use the InstanceId because it provides many other functionalities
        // that can be useful in the future:
        // https://developers.google.com/instance-id/
        return FirebaseInstanceId.getInstance().getToken();
    }

    public static boolean verifyDeviceToken(String id) {
        return !TextUtils.isEmpty(id) && getDeviceToken().equals(id);
    }

    public static boolean verifyConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public static void showKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Create a progress dialog
     * @param context Context of the app
     * @param message The message to display
     * @param indeterminate Whether is indeterminate or not
     * @param cancelable Whether is cancelable or not
     * @return
     */
    public static ProgressDialog createProgressDialog(Context context, String message,
                                                      boolean indeterminate, boolean cancelable) {
        ProgressDialog result = new ProgressDialog(context);
        result.setIndeterminate(indeterminate);
        result.setMessage(message);
        result.setCancelable(cancelable);
        return result;
    }

    /**
     * Mimics a enabled or disabled button changing alpha and clickable states
     */
    public static void setButtonEnabled(Button button, boolean enabled) {
        button.setClickable(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
    }
}