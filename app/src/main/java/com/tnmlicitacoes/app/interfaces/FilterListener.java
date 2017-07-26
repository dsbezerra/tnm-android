package com.tnmlicitacoes.app.interfaces;

import com.tnmlicitacoes.app.main.MainActivity;

import java.util.HashMap;

public interface FilterListener {
    /**
     * Called when the user presses filter button
     * in NoticeFilterDialog {@link MainActivity}
     */
    void onFilter(HashMap<String, Object> filterParams);
}
