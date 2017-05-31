package com.tnmlicitacoes.app.interfaces;

import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.SegmentsQuery;

public interface OnAccountConfigurationListener {

    /**
     * Called when the user selects a city in the AccountConfigurationActivity
     * @param selectedSize new selected count
     * @param city new selected city
     */
    void onCitySelected(int selectedSize, CitiesQuery.Node city);

    /**
     * Called when the user selects a segment in the AccountConfigurationActivity
     * @param selectedSize new selected count
     * @param segment new selected segment
     */
    void onSegmentSelected(int selectedSize, SegmentsQuery.Node segment);
}
