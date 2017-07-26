package com.tnmlicitacoes.app.interfaces;

public interface DownloadedFilesListener {

    /**
     * Called when the user clicks a file.
     */
    void onFileClick(int position);

    /**
     * Called when the user long clicks a file.
     */
    void onFileLongClick(int position);

    /**
     * Called when the count of selected files changes.
     */
    void onFilesSelectedChanged(int oldCount, int newCount);
}
