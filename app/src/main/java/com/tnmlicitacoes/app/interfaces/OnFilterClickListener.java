package com.tnmlicitacoes.app.interfaces;

import com.tnmlicitacoes.app.ui.main.MainActivity;

public interface OnFilterClickListener {
    /**
     * Called when the user presses filter button
     * in FilterDialog {@link MainActivity}
     */
    void onFilter(CharSequence constraint);
}
