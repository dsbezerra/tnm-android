package com.tnmlicitacoes.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.DownloadedFilesListener;
import com.tnmlicitacoes.app.utils.AndroidUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {

    private static final String TAG = "FileAdapter";

    /** Constant used when functions are not successfully executed */
    private static final int NO_SUCCESS = -1;

    //private List<File> mSelectedFiles = new ArrayList<>();
    private HashMap<String, File> mSelected = new HashMap<>();

    private List<File> mFileList = new ArrayList<>();

    private Context mContext;

    private Activity mParentActivity;

    private boolean mIsActionModeActive = false;

    /** Downloaded files actions listener */
    private DownloadedFilesListener mDownloadedFilesListener;

    public FileAdapter(Activity activity) {
        this.mContext = activity.getApplicationContext();
        this.mParentActivity = activity;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {

        File file = mFileList.get(position);
        String fileName = file.getName();
        holder.fileName.setText(fileName);
        if(fileName.endsWith(".pdf")) {
            holder.fileType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pdf));
        } else {
            holder.fileType.setVisibility(View.GONE);
        }

        int formatIndex = fileName.lastIndexOf('.');
        //String fileFormat = fileName.substring(formatIndex + 1).toUpperCase();
        holder.fileFormat.setText(mContext.getString(R.string.fileFormat, "PDF"));

        holder.checkBox.setVisibility(mIsActionModeActive ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(mSelected.containsKey(file.getAbsolutePath()));


        holder.fileName.setSelected(holder.checkBox.isChecked());
        if(mIsActionModeActive) {
            holder.fileName.setPadding(AndroidUtilities.dp(mParentActivity, 4.0f), 0, AndroidUtilities.dp(mParentActivity, 34.0f), 0);
        } else {
            holder.fileName.setPadding(AndroidUtilities.dp(mParentActivity, 4.0f), 0, AndroidUtilities.dp(mParentActivity, 4.0f), 0);
        }
    }

    /**
     * Sets the DownloadedFilesListener
     * @param listener
     */
    public void setDownloadedFilesListener(DownloadedFilesListener listener) {
        this.mDownloadedFilesListener = listener;
    }

    /**
     * Sets the selected HashMap
     * @param map the new HashMap
     */
    public void setSelected(HashMap<String, File> map) {
        int oldCount = mSelected != null ? mSelected.size() : 0;
        mSelected = map;
        if (mDownloadedFilesListener != null) {
            int newCount = mSelected.size();
            mDownloadedFilesListener.onFilesSelectedChanged(oldCount, newCount);
        }
        notifyDataSetChanged();
    }

    /**
     * Gets the file in the given position
     * @param position index of the file in the array
     * @return file if exists and null if index is invalid
     */
    public File getItem(int position) {
        if (position >= 0 && position < mFileList.size()) {
            return mFileList.get(position);
        }

        return null;
    }

    /**
     * Selects the file in the given position
     * @param position the position were the file is located
     * @return new count of selected items if successful and -1 if not
     */
    public int select(int position) {
        int result = -1;

        // Store old count
        int oldCount = getSelectedCount();

        File file = getItem(position);
        if (file == null) {
            return result;
        }

        // Filepath here acts like a key for the map
        String filePath = file.getAbsolutePath();
        if (mSelected.containsKey(filePath)) {
            result = deselect(filePath);
        } else {
            mSelected.put(filePath, file);
            result = mSelected.size();
        }

        // Notify the adapter for changes in the item at 'position'
        if (result != NO_SUCCESS) {
            if (mDownloadedFilesListener != null) {
                int newCount = mSelected.size();
                mDownloadedFilesListener.onFilesSelectedChanged(oldCount, newCount);
            }
            notifyItemChanged(position);
        }

        return result;
    }

    /**
     * Deselects the file from the selected map
     * @param key the key of the file (the absolutePath)
     * @return newCount if successful and -1 if not
     */
    public int deselect(String key) {
        int result = NO_SUCCESS;

        if (TextUtils.isEmpty(key)) {
            return result;
        }

        if (mSelected.remove(key) != null) {
            result = mSelected.size();
        }

        return result;
    }

    /**
     * Selects all files in the list
     */
    public void selectAll() {
        // Store old count
        int oldCount = getSelectedCount();

        for (File file : mFileList) {
            String key = file.getAbsolutePath();
            mSelected.put(key, file);
        }

        if (mDownloadedFilesListener != null) {
            int newCount = mSelected.size();
            mDownloadedFilesListener.onFilesSelectedChanged(oldCount, newCount);
        }

        notifyDataSetChanged();
    }

    /**
     * Deselect all files by clearing the selected hash map
     */
    public void deselectAll() {
        // Store old count
        int oldCount = getSelectedCount();

        mSelected.clear();

        if (mDownloadedFilesListener != null) {
            int newCount = mSelected.size();
            mDownloadedFilesListener.onFilesSelectedChanged(oldCount, newCount);
        }
        notifyDataSetChanged();
    }

    /**
     * Gets the selected count
     * @return the selected files count
     */
    public int getSelectedCount() {
        return mSelected.size();
    }

    /**
     * Gets the selected hash map
     * @return the HashMap with the selected files
     */
    public HashMap<String, File> getSelected() {
        return mSelected;
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    public void setItems(List<File> list) {
        this.mFileList = list;
        notifyDataSetChanged();
    }

    public boolean isActionModeActive() {
        return mIsActionModeActive;
    }

    public void setActionModeActive(boolean value) {
        mIsActionModeActive = value;
        if (!value) {
            mSelected.clear();
            if (mDownloadedFilesListener != null) {
                // Call onFileLongClick again to disable the action mode
                mDownloadedFilesListener.onFileLongClick(-1);
            }
        }
        notifyDataSetChanged();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ImageView fileType;
        public TextView fileName;
        public TextView fileFormat;
        public CheckBox checkBox;

        public VH(View itemView) {
            super(itemView);
            this.fileType = (ImageView) itemView.findViewById(R.id.filetype_image);
            this.fileName = (TextView) itemView.findViewById(R.id.filename_tv);
            this.fileFormat = (TextView) itemView.findViewById(R.id.fileformat_tv);
            this.checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            this.itemView.setClickable(true);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            view.requestFocus();
            if (mDownloadedFilesListener != null) {
                // Just to make sure the checkbox animation runs
                checkBox.performClick();
                mDownloadedFilesListener.onFileClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            view.requestFocus();
            if (mDownloadedFilesListener != null) {
                mDownloadedFilesListener.onFileLongClick(getAdapterPosition());
                return true;
            }
            return false;
        }
    }
}
