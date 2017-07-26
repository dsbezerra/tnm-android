package com.tnmlicitacoes.app.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by bigde on 24/06/2017.
 */

public class StringUtils {

    /**
     * Gets the price in this format X,XX
     */
    public static String getPriceInBrazil(double price) {
        return String.format(new Locale("pt", "BR"), "%.2f", price);
    }

    private StringUtils() {}
}
