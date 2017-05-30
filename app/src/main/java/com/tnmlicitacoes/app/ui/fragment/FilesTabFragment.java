package com.tnmlicitacoes.app.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.adapter.FileAdapter;
import com.tnmlicitacoes.app.interfaces.FileViewListener;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.tnmlicitacoes.app.utils.AndroidUtilities.PERMISSION_REQUEST_WRITE_EXT_STORAGE;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class FilesTabFragment extends Fragment implements OnClickListenerRecyclerView {

    private static final String TAG = "FilesTabFragment";

    private RecyclerView mRecyclerView;

    private TextView mNoItemsView;

    private List<File> mFilesList = new ArrayList<>();

    private FileAdapter mFilesAdapter;

    private Handler mHandler;

    private boolean isFilesLoaded = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(mFilesAdapter == null) {
            mFilesAdapter = new FileAdapter(activity);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFilesAdapter.setFileViewListener(null);
    }

    private void initViews(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.myFilesRecView);
        mNoItemsView = (TextView) v.findViewById(R.id.noItemsView);

        mFilesAdapter.setItems(new ArrayList<File>());
        mFilesAdapter.setRecListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mFilesAdapter);
    }

    public void loadFiles() {
        if(isFilesLoaded) {
            return;
        }

        boolean permissionGranted = false;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true;
        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), getString(R.string.storage_rationale_load_files), Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_WRITE_EXT_STORAGE);
        }

        if(permissionGranted) {
            mHandler = new Handler();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFilesList = FileUtils.getNotices();
                    if (mFilesList != null) {
                        mFilesAdapter.setItems(mFilesList);
                        mRecyclerView.setVisibility(mFilesList.size() != 0 ? View.VISIBLE : View.GONE);
                        mNoItemsView.setVisibility(mFilesList.size() != 0 ? View.GONE : View.VISIBLE);
                    }
                }
            });
        } 
    }

    public void setActionModeEnabled(boolean value) {
        mFilesAdapter.setIsActionModeActive(value);
    }

    public void startDeleteTask() {
        DeleteTask task = new DeleteTask();
        task.execute();
    }

    @Override
    public void OnClickListener(View v, int position) {
        if(mFilesAdapter.isActionModeActive()) {
            mFilesAdapter.addToSelected(position);
        } else {
            FileUtils.openPdf(getContext(), mFilesList.get(position));
        }
    }

    public void setAllSelected() {
        mFilesAdapter.setSelectedItems(mFilesList);
    }

    public void deselectAll() {
        mFilesAdapter.deselectAll();
    }

    private class DeleteTask extends AsyncTask<Void, Integer, Void> {

        private int mTotalFiles;
        private int mDeleteCount;

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getContext().getString(R.string.preparing_delete_files));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setMessage(getContext().getString(R.string.deleting_file_from_total, mDeleteCount, mTotalFiles));
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<File> files = mFilesAdapter.getSelectedFiles();
            mTotalFiles = files.size();
            for (File file : files) {
                if(file.exists()) {
                    String filePath = file.getAbsolutePath();
                    boolean deleted = file.delete();
                    if(deleted) {
                        mDeleteCount++;
                        publishProgress((100 * mDeleteCount) / mTotalFiles);
                        if(BuildConfig.DEBUG)
                            LOG_DEBUG(TAG, "Deleted " + filePath);
                    } else {
                        if(BuildConfig.DEBUG)
                            LOG_DEBUG(TAG, "Problem in file " + filePath);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            View view = getView();
            if(view != null) {
                Snackbar.make(getView(), getString(R.string.deleted_files_message, mDeleteCount), Snackbar.LENGTH_SHORT).show();
            }
            setActionModeEnabled(false);
            loadFiles();
        }
    }
}
