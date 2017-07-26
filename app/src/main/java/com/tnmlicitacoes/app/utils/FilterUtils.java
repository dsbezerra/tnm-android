package com.tnmlicitacoes.app.utils;

import android.content.Context;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.main.MainActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FilterUtils {

    private FilterUtils() {}

    /**
     * Get the list of modality filter values
     */
    public static List<String> getModalityFilterValues(Context context) {
        if (context != null) {
            return Arrays.asList(
                    context.getResources()
                            .getStringArray(R.array.filter_modalities)
            );
        }

        return null;
    }
}
