package com.tnmlicitacoes.app.interfaces;

import com.tnmlicitacoes.app.apollo.CitiesQuery;
import com.tnmlicitacoes.app.apollo.SegmentsQuery;

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

    /**
     * Called when the fragment finishes the setup and notify the activity to update the
     * count text
     */
    void onCompleteInitialisation(String tag);
}
