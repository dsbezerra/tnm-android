package com.tnmlicitacoes.app.main.mynotices

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import android.widget.Toast

import com.tnmlicitacoes.app.R
import com.tnmlicitacoes.app.adapter.FileAdapter
import com.tnmlicitacoes.app.interfaces.DownloadedFilesListener
import com.tnmlicitacoes.app.main.MainActivity
import com.tnmlicitacoes.app.ui.base.BaseFragment
import com.tnmlicitacoes.app.utils.FileUtils

import java.io.File
import java.util.ArrayList

import com.tnmlicitacoes.app.utils.AndroidUtilities.PERMISSION_REQUEST_WRITE_EXT_STORAGE
import com.tnmlicitacoes.app.utils.LogUtils
import com.tnmlicitacoes.app.utils.UIUtils

class DownloadedFragment : BaseFragment(), DownloadedFilesListener {

    /** Displays the list of files */
    private var mRecyclerView: RecyclerView? = null

    /** Displays an info view when there's no file downloaded */
    private var mNoItemsView: TextView? = null

    /** The file list */
    private var mFilesList: List<File>? = ArrayList()

    /** The recycler view file adapter */
    private var mFilesAdapter: FileAdapter? = null

    /** The main handler used when loading files */
    private var mHandler: Handler? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_files, container, false)
        initViews(view)
        return view
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (mFilesAdapter == null) {
            mFilesAdapter = FileAdapter(activity)
            mFilesAdapter?.setDownloadedFilesListener(this)
        }
    }

    private fun initViews(v: View) {
        mRecyclerView = v.findViewById(R.id.myFilesRecView) as RecyclerView
        mNoItemsView = v.findViewById(R.id.noItemsView) as TextView

        mFilesAdapter!!.setItems(ArrayList<File>())
        mRecyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.adapter = mFilesAdapter
    }

    fun loadFiles() {
        var permissionGranted = false
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(context, getString(R.string.storage_rationale_load_files), Toast.LENGTH_LONG).show()
            }

            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_WRITE_EXT_STORAGE)
        }

        if (permissionGranted) {
            mHandler = Handler()
            mHandler!!.post {
                mFilesList = FileUtils.getNotices()
                if (mFilesList != null) {
                    mFilesAdapter!!.setItems(mFilesList)
                    mRecyclerView!!.visibility = if (mFilesList!!.isNotEmpty()) View.VISIBLE else View.GONE
                    mNoItemsView!!.visibility = if (mFilesList!!.isNotEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    fun setActionModeEnabled(value: Boolean) {
        // Explicitly using the property syntax, but keep in mind that we do extra things
        // when assigning to this variable (see the setActionModeActive method for more details)
        mFilesAdapter!!.isActionModeActive = value
    }

    fun startDeleteTask() {
        val task = DeleteTask()
        task.execute()
    }

    /**
     * Handles click on a file
     */
    override fun onFileClick(position: Int) {
        if (mFilesAdapter!!.isActionModeActive) {
            mFilesAdapter?.select(position)
        } else {
            FileUtils.openPdf(context, mFilesList?.get(position));
        }
    }

    /**
     * Handles long click on a file
     */
    override fun onFileLongClick(position: Int) {
        // When call this method with position -1 it means we want to finish the action mode
        if (position == -1) {
            finishActionMode()
            return;
        }

        if (!mFilesAdapter!!.isActionModeActive) {
            initActionMode()
            mFilesAdapter?.isActionModeActive = true
            mFilesAdapter?.select(position)
        } else {
            finishActionMode()
        }
    }

    /**
     * Make sure to update the action bar title at count changes
     */
    override fun onFilesSelectedChanged(oldCount: Int, newCount: Int) {
        if (newCount == 0) {
            updateActionMode("");
        } else if (newCount > 0) {
            val title = String.format("%d selecionado(s)", newCount)
            updateActionMode(title);
        }
    }

    /**
     * Initializes the action mode
     */
    private fun initActionMode() {
        (activity as MainActivity).initActionMode(ActionModeCallback())
    }

    /**
     * Updates the action mode
     */
    private fun updateActionMode(title: String) {
        setActionModeTitle(title)
        (activity as MainActivity).actionMode.invalidate()
    }

    /**
     * Finalizes the action mode
     */
    private fun finishActionMode() {
        (activity as MainActivity).finishActionMode()
    }

    /**
     * Updates the action mode title
     */
    private fun setActionModeTitle(title: String) {
        (activity as MainActivity).actionMode.title = title
    }

    override fun getLogTag(): String {
        return TAG
    }

    /**
     * AsyncTask class that deletes a given input of files
     */
    private inner class DeleteTask : AsyncTask<Void, Int, Void>() {

        private var mTotalFiles: Int = 0
        private var mDeleteCount: Int = 0

        private var mProgressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            mProgressDialog = ProgressDialog(context)
            mProgressDialog!!.setMessage(context.getString(R.string.preparing_delete_files))
            mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.show()
        }

        override fun onProgressUpdate(vararg progress: Int?) {
            super.onProgressUpdate(*progress)
            mProgressDialog!!.setMessage(context.getString(R.string.deleting_file_from_total, mDeleteCount, mTotalFiles))
            mProgressDialog!!.progress = progress[0]!!
        }

        override fun doInBackground(vararg params: Void): Void? {

            // Get the selected files and total to be deleted
            val files = mFilesAdapter?.selected
            mTotalFiles = files!!.size

            // For each file verify if exists then delete
            for (key in files.keys) {
                val file = files[key]
                if (file!!.exists()) {
                    val filePath = file.absolutePath;
                    val deleted = file.delete();
                    if (deleted) {
                        mDeleteCount++;
                        publishProgress((100 * mDeleteCount) / mTotalFiles);
                        LogUtils.LOG_DEBUG(TAG, "Deleted " + filePath);
                    } else {
                        LogUtils.LOG_DEBUG(TAG, "Problem in file " + filePath);
                    }
                }
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            mProgressDialog!!.dismiss()

            // Displays a feedback message
            val view = view
            if (view != null) {
                Snackbar.make(view, getString(R.string.deleted_files_message,
                        mDeleteCount), Snackbar.LENGTH_SHORT).show()
            }

            // Finish action mode if active
            finishActionMode()

            // Reload files
            loadFiles()
        }
    }


    /**
     * The ActionMode callback handlers
     */
    inner class ActionModeCallback() : ActionMode.Callback {

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {

            when(item?.itemId) {

                R.id.item_delete -> {
                    startDeleteTask()
                }

                R.id.item_select_all -> {
                    mFilesAdapter?.selectAll()
                }

                R.id.item_select_none -> {
                    mFilesAdapter?.deselectAll()
                }
            }

            return false
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.my_files_action_mode, menu);
            return true;
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {

            // Get menu items references
            val deleteItem = menu?.getItem(0);
            val selectAllItem = menu?.getItem(1);
            val selectNoneItem = menu?.getItem(2);

            val count = mFilesAdapter?.selectedCount
            if (count == 0) {
                // Hide delete when none is selected
                deleteItem?.isVisible = false
                deleteItem?.isEnabled = false

                // Show select all when none is selected as action never
                selectAllItem?.isVisible = true
                selectAllItem?.isEnabled = true
                MenuItemCompat.setShowAsAction(selectAllItem,
                        MenuItemCompat.SHOW_AS_ACTION_NEVER);

                // Hide select none when none is selected
                selectNoneItem?.isVisible = false
                selectNoneItem?.isEnabled = false
            } else if (count!! > 0) {
                // Show delete when at least one is selected
                deleteItem?.isVisible = true
                deleteItem?.isEnabled = true

                // Show select all when more than 0 is selected
                selectAllItem?.isVisible = true
                selectAllItem?.isEnabled = true

                // Show select none when more than 0 is selected
                selectNoneItem?.isVisible = true
                selectNoneItem?.isEnabled = true
            }

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // No-op
        }
    }

    companion object {

        private val TAG = "DownloadedFragment"
    }
}
