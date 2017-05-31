package com.tnmlicitacoes.app.utils;

import com.tnmlicitacoes.app.ui.main.MainActivity;

import java.util.HashMap;

public class FilterUtils {

    /**
     * Flag used in Filterable to determine if the user is filtering by
     * typing some text or is using the {@link MainActivity.FilterDialog }
     */
    public static boolean NON_TEXT_FILTERING = false;

    /**
     * Filter param used to retrieve the city to be filtered
     */
    public static final String FILTER_CITY = "FILTER_CITY";

    /**
     * Filter param used to retrieve the date to be filtered
     */
    public static final String FILTER_DATE = "FILTER_DATE";

    /**
     * Filter param used to retrieve the modality to be filtered
     */
    public static final String FILTER_MODALITY = "FILTER_MODALITY";

    /**
     * Filter param used to retrieve the is exclusive value to be filtered
     */
    public static final String FILTER_EXCLUSIVE = "FILTER_EXCLUSIVE";

    /**
     * Date constants int indicators spinner items positions
     */
    private static final int DATE_FILTER_ALL = 0;

    private static final int DATE_FILTER_TODAY = 1;

    private static final int DATE_FILTER_THIS_WEEK = 2;

    private static final int DATE_FILTER_THIS_MONTH = 3;

    private static final int DATE_FILTER_THIS_YEAR = 4;

    /**
     * Filter hash map that contains the params to be filtered
     */
    private static HashMap<String, Object> mFilterHashMap = new HashMap<>();

    /**
     * Sets the filter hash map
     */
    public static void setFilterHashMap(final HashMap<String, Object> filterHashMap) {
        mFilterHashMap = filterHashMap;
    }

    /**
     * Clears the filter hash map
     */
    public static void clearFilterHashMap() {
        mFilterHashMap.clear();
    }

    /**
     * Add a filter param
     */
    public static void addFilterParam(String paramName, Object value) {
        mFilterHashMap.put(paramName, value);
    }

    /**
     * Get the modality filter value we subtract 1 because there
     * is an initial value (all modalities) that isn't in the database,
     * just locally
     */
    public static int getModalityFilter() {
        return (int) mFilterHashMap.get(FILTER_MODALITY) - 1;
    }

    /**
     * Get the city filter value
     */
    public static String getCityFilter() {
        return mFilterHashMap.get(FILTER_CITY).toString();
    }

    /**
     * Get is exclusive filter value
     */
    public static boolean getIsExclusiveFilter() {
        return (boolean) mFilterHashMap.get(FILTER_EXCLUSIVE);
    }

    /**
     * Get the date filter value
     */
    public static int getDateFilter() {
        return (int) mFilterHashMap.get(FILTER_DATE);
    }

    /**
     * Check if the date is within the range specified in filter dialog
     */
    public static boolean isDateWithinRange(int dateSpinnerValue, long noticeDate) {
        boolean result;
        long currentTime = System.currentTimeMillis();

        long timeRemaining = (noticeDate - currentTime);

        switch (dateSpinnerValue) {
            case DATE_FILTER_ALL:
                result = true;
                break;
            case DATE_FILTER_TODAY:
                int days = (int) (timeRemaining / Utils.DAY_IN_MILLIS);
                result = days >= 0 && days <= 1;
                break;
            case DATE_FILTER_THIS_WEEK:
                int weeks = (int) (timeRemaining / Utils.WEEK_IN_MILLIS);
                result = weeks >= 0 && weeks <= 1;
                break;
            case DATE_FILTER_THIS_MONTH:
                int months = (int) (timeRemaining / Utils.MONTH_IN_MILLIS);
                result = months >= 0 && months <= 1;
                break;
            case DATE_FILTER_THIS_YEAR:
                int years = (int) (timeRemaining / Utils.YEAR_IN_MILLIS);
                result = years >= 0 && years <= 1;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private FilterUtils() {}
}
