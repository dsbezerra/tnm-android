package com.tnmlicitacoes.app.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.model.realm.Notice;
import com.tnmlicitacoes.app.service.DownloadService;
import com.tnmlicitacoes.app.tasks.SendEmailTask;
import com.tnmlicitacoes.app.ui.activity.DetailsActivity;
import com.tnmlicitacoes.app.ui.activity.WebviewActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.FileUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.io.File;
import java.util.HashMap;

import okhttp3.HttpUrl;

import static com.tnmlicitacoes.app.utils.AndroidUtilities.PERMISSION_REQUEST_WRITE_EXT_STORAGE;

/**
 *  DetailsFragment
 *  Show the details about the selected public notice
 */

public class DetailsFragment extends Fragment implements View.OnClickListener, DownloadService.OnDownloadListener {

    private static final String TAG = "DetailsFragment";

    private TextView mFavoriteText;

    private Notice mNotice;

    private boolean mSavedOnDatabase = false;

    private DetailsActivity mParentActivity;

    private boolean mFromNotification = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);
        initViews(view);
        initListeners();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleIntent();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = (DetailsActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mParentActivity = null;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void handleIntent() {

    }

    private void initViews(View v) {

    }

    private void initListeners() {
        mFavoriteText.setOnClickListener(this);
    }

    private void fillWithData() {


    }

    @Override
    public void onClick(View v) {
    }

    private void updateFavorite(boolean saved, boolean firstStart) {
        mFavoriteText.setCompoundDrawablesWithIntrinsicBounds(0, saved
                ? R.drawable.ic_star_on
                : R.drawable.ic_star_off, 0, 0);

        mFavoriteText.setText(saved
                ? getString(R.string.saved_notice_text)
                : getString(R.string.save_notice_text));

        if(!firstStart) {
            Toast.makeText(getContext(), saved ? getString(R.string.database_saved_notice)
                    : getString(R.string.database_removed_notice), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        DownloadService.onDownloadListener = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        DownloadService.onDownloadListener = null;
    }

    @Override
    public void onDownloadStart() {
        if(mParentActivity != null) {
            mParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(),
                            getString(R.string.start_download_message),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDownloadFailure(final String fileName) {
        if(mParentActivity != null) {
            mParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View v = getView();
                    if(v != null) {
                        Snackbar.make(v,
                                getString(R.string.download_fail_messsage, fileName),
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onDownloadFinished(final File file) {
        if(mParentActivity != null) {
            mParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View view = getView();
                    if(view != null) {
                        Snackbar.make(view,
                                getString(R.string.download_finished_with_filename, file.getName()),
                                Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.snackbar_action_open), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        String fileName = file.getName();
                                        boolean isPdfFile = fileName.endsWith(".pdf");

                                        if (isPdfFile) {
                                            FileUtils.openPdf(getContext(), file);
                                        } else {
                                            // Tentar abrir com algum aplicativo diferente
                                        }


                                    }
                                }).show();
                    }
                }
            });
        }
    }

    private void startDownload(String fileName, String fileLink) {
        Intent intent = new Intent(getContext(), DownloadService.class);
        intent.putExtra("LINK", fileLink);
        if(!fileLink.contains("http")) {
            Toast.makeText(getContext(), getString(R.string.download_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("NAME", fileName);
        intent.putExtra("NOTIFICATION_ID", fileLink.length() + (Math.random() * 10001) + 10000);

        if(mParentActivity != null) {
            mParentActivity.startService(intent);
        }
    }

    // Used to download notices only
    public void startDownloadProcess() {
        String fileLink = mNotice.getLink();
        String fileName = (mNotice.getModality() + " - " + mNotice.getNumber() +
                fileLink.substring(fileLink.length() - 4)).replaceAll("/", "-");

        startDownloadProcess(fileName, fileLink);
    }

    // Used to download attachments and notices
    private void startDownloadProcess(String fileName, String fileLink) {
        if(!AndroidUtilities.verifyConnection(getContext())) {
            if(getView() != null) {
                Snackbar.make(getView(),
                        getString(R.string.no_connection_try_again),
                        Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startDownload(fileName, fileLink);
        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(),
                        getString(R.string.storage_rationale_download),
                        Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_WRITE_EXT_STORAGE);
        }
    }

    public void showOnline() {

        if(!AndroidUtilities.verifyConnection(getContext())) {
            if(getView() != null) {
                Snackbar.make(getView(),
                        "Sem conexão, por favor conecte e tente novamente",
                        Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        String pageTitle = mNotice.getModality() + " - " + mNotice.getNumber();
        String noticeLink = mNotice.getLink();

        if(HttpUrl.parse(noticeLink) != null) {

            Intent webviewIntent = new Intent(getContext(), WebviewActivity.class);
            webviewIntent.putExtra(WebviewActivity.PAGE_TITLE, pageTitle);
            webviewIntent.putExtra(WebviewActivity.PAGE_LINK, noticeLink);
            webviewIntent.putExtra(WebviewActivity.IS_PDF_FILE, noticeLink.endsWith(".pdf"));
            webviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(webviewIntent);
        } else {
            Toast.makeText(getContext(),
                    getString(R.string.download_unavailable),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void sendEmail() { }

    private void sendEmailTask(String l, String f, String e, String a) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Utils.FILE_LINK_PARAM, l);
        map.put(Utils.FILE_NAME_PARAM, f);
        map.put(Utils.USER_EMAIL_PARAM, e);
        map.put(Utils.AGENCY_NAME_PARAM, a);

        SendEmailTask sendTask = new SendEmailTask(getActivity(), map);
        sendTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_WRITE_EXT_STORAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadProcess();
            } else {
                Toast.makeText(getContext(),
                        getString(R.string.download_cancel_no_permission_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
