package com.tnmlicitacoes.app.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.internal.android.ISO8601Utils;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class DateUtils {

    private static final String TAG = "DateUtils";

    public static final String INVALID_DATE = "invalid_date";

    /** Default pattern */
    private static final String PATTERN = "dd/MM/yyyy";

    /** Format pattern with hours and minutes */
    private static final String PATTERN_WITH_TIME = "dd/MM/yyyy HH:mm";

    /** The date desired locale */
    private static Locale sLocale = new Locale("pt", "BR");

    /**
     * Formats given date input in millis to DD/MM/YYYY
     */
    public static String format(long millis) {
        return format(new Date(millis));
    }

    /**
     * Formats a given date input to DD/MM/YYYY
     */
    public static String format(Date input) {
        if (input == null) {
            return INVALID_DATE;
        }
        boolean withTime = input.getHours() != 0;
        return new SimpleDateFormat(withTime ? PATTERN_WITH_TIME : PATTERN, sLocale)
                .format(input);
    }

    /**
     * Add left zero if number is below 10
     */
    private static String addZero(int number) {
        if (number < 10) {
            return "0" + number;
        }

        return String.valueOf(number);
    }

    /**
     * Parses a ISO date format string to java.util.Date format
     */
    public static Date parse(String date) {
        Date result = null;

        try {
            result = ISO8601Utils.parse(date, new ParsePosition(0));
        } catch (ParseException e) {
            LOG_DEBUG(TAG, e.getMessage());
        }

        return result;
    }

    private DateUtils() { }
}
