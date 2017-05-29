package com.tnmlicitacoes.app.interfaces;

import com.tnmlicitacoes.app.NoticesQuery;

public interface OnNoticeActionsDialogListener {
    /**
     * Called when the user clicks on the see details action in the bottom sheet
     */
    void onSeeDetailsClicked();

    /**
     * Called when the user clicks on the view online action in the bottom sheet
     */
    void onViewOnlineClicked();

    /**
     * Called when the user clicks on the send to email action in the bottom sheet
     */
    void onSendToEmailClicked();

    /**
     * Called when the user clicks on the download action in the bottom sheet
     */
    void onDownloadClicked();
}
