package com.tnmlicitacoes.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.FileViewListener;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {

    private static final String TAG = "FileAdapter";

    private List<File> mSelectedFiles = new ArrayList<>();

    private List<File> mFilesList = new ArrayList<>();

    private Context mContext;

    private Activity mParentActivity;

    private boolean mIsActionModeActive = false;

    private OnClickListenerRecyclerView mRecViewListener;

    private FileViewListener mFileViewListener;

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
    public void onBindViewHolder(VH holder, int position) {

        File file = mFilesList.get(position);
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
        holder.checkBox.setChecked(mSelectedFiles.contains(file));


        holder.fileName.setSelected(holder.checkBox.isChecked());
        if(mIsActionModeActive) {
            holder.fileName.setPadding(AndroidUtilities.dp(mParentActivity, 4.0f), 0, AndroidUtilities.dp(mParentActivity, 34.0f), 0);
        } else {
            holder.fileName.setPadding(AndroidUtilities.dp(mParentActivity, 4.0f), 0, AndroidUtilities.dp(mParentActivity, 4.0f), 0);
        }

        final int pos = position;
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecViewListener.OnClickListener(v, pos);
                mFileViewListener.onUpdateActionModeTitle(mSelectedFiles.size());
            }
        });
    }

    public void setRecListener(OnClickListenerRecyclerView listener) {
        this.mRecViewListener = listener;
    }

    public void setFileViewListener(FileViewListener listener) {
        this.mFileViewListener = listener;
    }

    public void addToSelected(int position) {
        File file = mFilesList.get(position);
        if(mSelectedFiles.contains(file)) {
            mSelectedFiles.remove(file);
            if(mSelectedFiles.size() == 0) {
                mIsActionModeActive = false;
                mFileViewListener.onActionModeListener(mIsActionModeActive);
            }
        } else {
            mSelectedFiles.add(file);
        }

        notifyItemChanged(position);
    }

    public void setSelectedItems(List<File> items) {
        mSelectedFiles = new ArrayList<>(items);
        mFileViewListener.onUpdateActionModeTitle(mSelectedFiles.size());
        notifyDataSetChanged();
    }

    public void deselectAll() {
        mSelectedFiles.clear();
        mFileViewListener.onUpdateActionModeTitle(0);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedFiles.size();
    }

    @Override
    public int getItemCount() {
        return mFilesList.size();
    }

    public List<File> getSelectedFiles() {
        return mSelectedFiles;
    }

    public void setItems(List<File> list) {
        this.mFilesList = list;
        notifyDataSetChanged();
    }

    public boolean isActionModeActive() {
        return mIsActionModeActive;
    }

    public void setIsActionModeActive(boolean value) {
        mIsActionModeActive = value;
        if(!mIsActionModeActive) {
            mFileViewListener.onActionModeListener(false);
            mSelectedFiles.clear();
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
        public void onClick(View v) {
            if(mRecViewListener != null) {
                v.requestFocus();
                checkBox.performClick();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mRecViewListener != null && mFileViewListener != null) {
                mIsActionModeActive = !mIsActionModeActive;
                mFileViewListener.onActionModeListener(mIsActionModeActive);
                if(mIsActionModeActive) {
                    mSelectedFiles.add(mFilesList.get(getPosition()));
                } else {
                    mSelectedFiles.clear();
                }
                mFileViewListener.onUpdateActionModeTitle(mSelectedFiles.size());
                v.requestFocus();
                LogUtils.LOG_DEBUG(TAG, mSelectedFiles.size() + "");
                notifyDataSetChanged();
                return true;
            }
            return false;
        }
    }
}
