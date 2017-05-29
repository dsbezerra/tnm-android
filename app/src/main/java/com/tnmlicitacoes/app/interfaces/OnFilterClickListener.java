package com.tnmlicitacoes.app.interfaces;

public interface OnFilterClickListener {
    /**
     * Called when the user presses filter button
     * in FilterDialog {@link com.tnmlicitacoes.app.ui.activity.MainActivity}
     */
    void onFilter(CharSequence constraint);
}
