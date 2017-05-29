package com.tnmlicitacoes.app.utils;

import android.util.Log;

public class LogUtils {

    private static boolean LOG_ENABLED = true;

    private LogUtils() {}

    public static void LOG_DEBUG(final String tag, String message) {
        if(LOG_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void LOG_ERROR(final String tag, String message) {
        if (LOG_ENABLED) {
            Log.e(tag, message);
        }
    }

    public static void LOG_INFO(final String tag, String message) {
        if(LOG_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void LOG_VERBOSE(final String tag, String message) {
        if(LOG_ENABLED) {
            Log.v(tag, message);
        }
    }

    public static void LOG_WARN(final String tag, String message) {
        if(LOG_ENABLED) {
            Log.w(tag, message);
        }
    }

}
