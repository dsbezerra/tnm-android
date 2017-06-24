package com.tnmlicitacoes.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseActivity;

public class SettingsUtils {

    /**
     * Preferences xml file name
     */
    public static final String PREFERENCES_NAME = "tnm_preferences";

    /**
     * Temporary settings preference name
     */
    public static final String PREFERENCES_TMP_SETTINGS_NAME = "tnm_temporary_settings";

    /**
     * Boolean indicating if the user is logged in or not
     */
    public static final String PREF_USER_IS_LOGGED = "tnm_user_is_logged_in";

    /**
     * String that contains the user's phone number
     */
    public static final String PREF_USER_PHONE_NUMBER = "tnm_user_phone_number";

    /**
     * String that contains the user's phone number formatted
     */
    public static final String PREF_USER_PHONE_NUMBER_FORMATTED = "tnm_user_phone_number_formatted";

    /**
     * String that contains the billing subscription sku
     */
    public static final String PREF_BILLING_STATE = "tnm_billing_state";

    /**
     * String that contains the billing subscription name
     */
    public static final String PREF_BILLING_SUB_NAME = "tnm_billing_sub_name";

    /**
     * Boolean indicating if we need to check for unique session
     * Used only on {@link BaseActivity}
     * to check if the session is valid or not
     */
    public static final String PREF_CHECK_FOR_UNIQUE_SESSION = "tnm_check_for_unique_session";

    /**
     * String that contains a device id (from another session). It's used only to check later
     * if the app isn't on top of activity stack, in another words,
     * check when the user is in fact using the application.
     *
     * Also only used in {@link BaseActivity}
     */
    public static final String PREF_NEW_DEVICE_ID = "tnm_new_device_id";

    /**
     * String tha containt the user device id
     */
    public static final String PREF_DEVICE_ID = "tnm_device_id";

    /**
     * String that contains the user id
     */
    public static final String PREF_USER_ID = "tnm_user_id";

    /**
     * Long used to check if the session is valid after 1 week, also only used in
     * {@link BaseActivity}
     */
    public static final String PREF_LAST_SESSION_CHECKED_TIMESTAMP = "tnm_last_session_checked_timestamp";

    /**
     * Long used to check last time we got an access token
     */
    public static final String PREF_LAST_ACCESS_TOKEN_REFRESH_TIMESTAMP = "tnm_last_access_token_timestamp";

    /**
     * Boolean indicating if the user has already finished the initial configuration
     * to use the app
     */
    public static final String PREF_INITIAL_CONFIG_IS_FINISHED = "tnm_initial_config_is_finished";

    /**
     * Boolean indicating if the was sent to server
     */
    public static final String PREF_SENT_TOKEN_TO_SERVER = "tnm_sent_token_to_server";

    /**
     * Int newest version code
     */
    public static final String PREF_NEWEST_VERSION_CODE = "tnm_n_v_code";

    /**
     * String that contains the gcm token
     */
    public static final String PREF_GCM_TOKEN = "tnm_gcm_token";

    /**
     * String that contains refresh token
     */
    public static final String PREF_REFRESH_TOKEN = "tnm_refresh_token";

    /**
     * String that contains access token
     */
    public static final String PREF_ACCESS_TOKEN = "tnm_access_token";

    /**
     * Boolean indicating if the pdf viewer is app's default
     */
    public static final String PREF_DEFAULT_PDF_VIEWER = "tnm_default_pdf_viewer";

    /**
     * Boolean indicating if is the first start
     */
    public static final String PREF_IS_FIRST_START = "tnm_is_first_start";

    /**
     * Boolean indicating if already saw intro screen
     */
    public static final String PREF_INTRO_VIEWED = "tnm_intro_viewed";

    /**
     * Long activation date
     */
    public static final String PREF_ACTIVATION_DATE = "tnm_activation_date";

    /**
     * String that contains the user's email
     */
    public static final String PREF_KEY_USER_DEFAULT_EMAIL = "pref_key_tnm_user_default_email";

    /**
     * Boolean indicating if notifications are enabled
     */
    public static final String PREF_KEY_NOTIFICATIONS_ENABLED = "pref_key_tnm_notifications_enabled";

    /**
     * Boolean indicating if tips notifications are enabled
     */
    public static final String PREF_KEY_NOTIFICATIONS_TIPS = "pref_key_tnm_notifications_tips";

    /**
     * Boolean indicating if the news notifications are enabled
     */
    public static final String PREF_KEY_NOTIFICATIONS_NEWS = "pref_key_tnm_notifications_news";

    /**
     * Boolean indicating if the biddings notifications are enabled
     */
    public static final String PREF_KEY_NOTIFICATIONS_BIDDINGS = "pref_key_tnm_notifications_biddings";

    /**
     * String that contains temporary selected cities to easy restore in onSaveInstance
     */
    public static final String PREF_TMP_SELECTED_CITIES = "tnm_tmp_selected_cities";

    /**
     * String that contains temporary selected categories to easy restore in onSaveInstance
     */
    public static final String PREF_TMP_SELECTED_CATEGORIES = "tnm_tmp_selected_categories";

    /**
     * String that contains temporary topics to be unsubscribed
     */
    public static final String PREF_TMP_UNSUBSCRIBE_TOPICS = "tnm_tmp_unsubscribe_topics";

    /**
     * Boolean indicating if we need to update topics
     */
    public static final String PREF_NEED_TO_UPDATE_TOPICS = "tnm_need_to_update_topics";

    /**
     * Boolean indicating if trial is expired
     */
    public static final String PREF_IS_TRIAL_EXPIRED = "tnm_t_e";

    /**
     * Boolean indicating if the user is waiting for a sms
     */
    public static final String PREF_IS_WAITING_FOR_SMS = "tnm_is_waiting_for_sms";

    /**
     * Long indicating the amount of time elapsed in waiting sms
     */
    public static final String PREF_WAITING_SMS_REMAINING_TIME = "tnm_remaining_sms_waiting_time";

    /**
     * Default long value
     */
    public static final long LONG_DEFAULT = 0;

    /**
     * Returns true if trial is expired
     */
    public static boolean isTrialExpired(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_IS_TRIAL_EXPIRED, false);
    }

    /**
     * Returns true if user is logged and false when not
     */
    public static boolean isUserLogged(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_USER_IS_LOGGED, false);
    }

    /**
     * Returns true if the user already finished the initial configuration and false if not
     */
    public static boolean isInitalConfigFinished(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_INITIAL_CONFIG_IS_FINISHED, false);
    }

    /**
     * Returns true if default pdf viewer
     */
    public static boolean isDefaultPdfViewer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_DEFAULT_PDF_VIEWER, true);
    }

    /**
     * Returns true if token is in server
     */
    public static boolean isTokenInServer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_SENT_TOKEN_TO_SERVER, false);
    }

    /**
     * Return true if we need to check for unique session and false if not
     */
    public static boolean needToCheckSession(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_CHECK_FOR_UNIQUE_SESSION, false);
    }

    /**
     * Return true if we need to update subscribed topics
     */
    public static boolean needToUpdateTopics(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_NEED_TO_UPDATE_TOPICS, false);
    }

    /**
     * Returns the newest version code
     */
    public static int getNewestVersionCode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(PREF_NEWEST_VERSION_CODE, BuildConfig.VERSION_CODE);
    }

    /**
     * Return true if is the first start
     */
    public static boolean isFirstStart(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_IS_FIRST_START, true);
    }

    /**
     * Return true if the intro has passed
     */
    public static boolean isIntroViewed(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_INTRO_VIEWED, false);
    }

    /**
     * Return true if notifications are enabled, false otherwise
     */
    public static boolean isNotificationsEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, false);
    }

    /**
     * Return true if tips notifications are enabled, false otherwise
     */
    public static boolean isTipsNotificationsEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_KEY_NOTIFICATIONS_TIPS, false);
    }

    /**
     * Return true if news notifications are enabled, false otherwise
     */
    public static boolean isNewsNotificationsEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_KEY_NOTIFICATIONS_NEWS, false);
    }

    /**
     * Return true if biddings notifications are enabled, false otherwise
     */
    public static boolean isBiddingsNotificationsEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_KEY_NOTIFICATIONS_BIDDINGS, false);
    }

    /**
     * Return true if user is waiting for sms
     */
    public static boolean isWaitingForSms(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_IS_WAITING_FOR_SMS, false);
    }

    /**
     * Return the sms waiting time
     */
    public static long getRemainingSmsWaitingTime(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(PREF_WAITING_SMS_REMAINING_TIME, LONG_DEFAULT);
    }

    /**
     * Return the user phone number and STRING_DEFAULT when there is not stored
     * */
    public static String getUserPhoneNumber(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_USER_PHONE_NUMBER, null);
    }

    /**
     * Returns current gcm token
     */
    public static String getCurrentGcmToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_GCM_TOKEN, null);
    }

    /**
     * Return the user phone formatted
     */
    public static String getUserPhoneFormattedNumber(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_USER_PHONE_NUMBER_FORMATTED, null);
    }

    /**
     * Return the user default email
     */
    public static String getUserDefaultEmail(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_KEY_USER_DEFAULT_EMAIL, context.getString(R.string.pref_email_default));
    }

    /**
     * Return the billing subscription from sku
     */
    public static String getBillingState(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_BILLING_STATE, null);
    }

    /**
     * Return the billing subscription name
     */
    public static String getBillingSubName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_BILLING_SUB_NAME, null);
    }

    /**
     * Return unsubscribe topics
     */
    public static String getUnsubscribeTopics(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_TMP_UNSUBSCRIBE_TOPICS, null);
    }

    /**
     * Return the user id
     */
    public static String getUserId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_USER_ID, null);
    }

    /**
     * Return the new device id that was stored if there is another session with the same number in another
     * device
     * */
    public static String getNewDeviceId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_NEW_DEVICE_ID, null);
    }

    /**
     * Return the device if stored in loginWithCode
     */
    public static String getDeviceId(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_DEVICE_ID, null);
    }

    /**
     * Return the last time checked for a new session in milliseconds
     */
    public static long getLastSessionCheckedTimestamp(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(PREF_LAST_SESSION_CHECKED_TIMESTAMP, LONG_DEFAULT);
    }

    /**
     * Return the last time that we got an access token
     */
    public static long getLastAccessTokenRefreshTimestamp(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(PREF_LAST_ACCESS_TOKEN_REFRESH_TIMESTAMP, LONG_DEFAULT);
    }

    /**
     * Return activation date in miliseconds
     */
    public static long getActivationDateFromPrefs(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(PREF_ACTIVATION_DATE, LONG_DEFAULT);
    }

    /**
     * Get user refresh token
     */
    public static String getRefreshToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_REFRESH_TOKEN, null);
    }

    /**
     * Returns current access token
     */
    public static String getCurrentAccessToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_ACCESS_TOKEN, null);
    }

    /**
     * Put an int in the specified pref key
     */
    public static void putInt(Context context, final String key, int value) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();
        prefsEditor.putInt(key, value);
        prefsEditor.apply();
    }

    /**
     * Put a string in the specified preference key
     * */
    public static void putString(Context context, final String key, String string) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();
        prefsEditor.putString(key, string);
        prefsEditor.apply();
    }

    /**
     * Store a boolean value in the specified preference key
     * */
    public static void putBoolean(Context context, final String key, boolean value) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    /**
     * Store a long value in the specified preference key
     */
    public static void putLong(Context context, final String key, long value) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefsEditor.putLong(key, value);
        prefsEditor.apply();
    }

    /**
     * Store a temporary string in preferences
     * */
    public static void putTemporaryString(Context context, final String key, String string) {
        SharedPreferences.Editor prefsEditor = context
                .getSharedPreferences(PREFERENCES_TMP_SETTINGS_NAME, Context.MODE_PRIVATE).edit();
        prefsEditor.putString(key, string);
        prefsEditor.apply();
    }

    /**
     * Returns a temporary string in preferences
     * */
    public static String getTemporaryString(Context context, final String key) {
        SharedPreferences preferences = context
                .getSharedPreferences(PREFERENCES_TMP_SETTINGS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, null);
    }

    /**
     * Erase all temporary settings
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void eraseTemporarySettings(Context context) {
        SharedPreferences.Editor prefsEditor = context
                .getSharedPreferences(PREFERENCES_TMP_SETTINGS_NAME, Context.MODE_PRIVATE).edit();
        prefsEditor.clear().apply();
    }

    /**
     * Erase all preferences
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void erasePreference(Context context) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefsEditor.clear();
        prefsEditor.apply();
    }

    /**
     * Save configurations after verification code
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void saveVerifiedInfo(Context context, String userId,
                                        String formattedPhone,
                                        String refreshToken, String deviceId, 
                                        boolean isLogged, boolean isInitialConfigFinished) 
    {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();

        prefsEditor.putString(PREF_USER_ID, userId);
        prefsEditor.putString(PREF_USER_PHONE_NUMBER_FORMATTED, formattedPhone);
        prefsEditor.putString(PREF_REFRESH_TOKEN, refreshToken);
        prefsEditor.putString(PREF_DEVICE_ID, deviceId);
        prefsEditor.putBoolean(PREF_USER_IS_LOGGED, isLogged);
        prefsEditor.putBoolean(PREF_INITIAL_CONFIG_IS_FINISHED, isInitialConfigFinished);
        prefsEditor.apply();
    }

    public static void saveLoggedInfo(Context context, String refreshToken, String userId) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();

        prefsEditor.putString(PREF_REFRESH_TOKEN, refreshToken);
        prefsEditor.putString(PREF_USER_ID, userId);
        prefsEditor.putBoolean(PREF_IS_WAITING_FOR_SMS, false);
        prefsEditor.putBoolean(PREF_USER_IS_LOGGED, true);
        prefsEditor.putBoolean(PREF_INITIAL_CONFIG_IS_FINISHED, false);
        prefsEditor.apply();
    }

    /**
     * Activate all notifications
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void activateNotifications(Context context) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefsEditor.putBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, true);
        prefsEditor.putBoolean(PREF_KEY_NOTIFICATIONS_TIPS, true);
        prefsEditor.putBoolean(PREF_KEY_NOTIFICATIONS_NEWS, true);
        prefsEditor.putBoolean(PREF_KEY_NOTIFICATIONS_BIDDINGS, true);
        prefsEditor.apply();
    }

    public static String getAesKey(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("aes_key", null);
    }

    public static String getIV(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("iv", null);
    }

}
