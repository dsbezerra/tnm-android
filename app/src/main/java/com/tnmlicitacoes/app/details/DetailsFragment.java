package com.tnmlicitacoes.app.details;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.NoticeByIdQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.model.realm.Notice;
import com.tnmlicitacoes.app.service.DownloadService;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.utils.DateUtils;
import com.tnmlicitacoes.app.utils.FileUtils;
import com.tnmlicitacoes.app.utils.NoticeUtils;
import com.tnmlicitacoes.app.utils.StringUtils;

import java.io.File;
import java.util.Date;

import javax.annotation.Nonnull;

import io.realm.Realm;

import static com.tnmlicitacoes.app.utils.AndroidUtilities.PERMISSION_REQUEST_WRITE_EXT_STORAGE;

/**
 *  DetailsFragment
 *  Show the seeDetails about the selected public notice
 */

public class DetailsFragment extends BaseFragment implements DownloadService.OnDownloadListener {

    /** The logging tag */
    public static final String TAG = "DetailsFragment";

    /** The notice id from database key retriever */
    public static final String NOTICE_ID = "notice_id";
    public static final String FROM_NOTIFICATION = "from_notification";

    /** Displays whether the notice is exclusive or not */
    private LinearLayout mExclusive;

    /** The download button */
    private Button mDownload;

    /** Displays the modality */
    private TextView mModality;

    /** Displays the number */
    private TextView mNumber;

    /** Displays the date */
    private TextView mDate;

    /** Displays the object */
    private TextView mObject;

    /** Displays the amount */
    private TextView mAmount;

    /** Displays the agency */
    private TextView mAgency;

    /** The notice */
    private Notice mLocalNotice;

    /** The parent activity */
    private DetailsActivity mParentActivity;

    /** The realm instance */
    private Realm mRealm;

    /** The single notice call */
    private ApolloCall<NoticeByIdQuery.Data> mNoticeByIdCall;

    /** The application singleton */
    private TnmApplication mApplication;

    /* Create a new instance of this fragment */
    public static DetailsFragment newInstance(String id, boolean fromNotification) {

        Bundle args = new Bundle();
        args.putString(NOTICE_ID, id);
        args.putBoolean(FROM_NOTIFICATION, fromNotification);

        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
        if (mNoticeByIdCall != null) {
            mNoticeByIdCall.cancel();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mApplication = (TnmApplication) mParentActivity.getApplication();
        handleArguments();
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
    public String getLogTag() {
        return TAG;
    }

    private void initViews(View view) {
        mExclusive = (LinearLayout) view.findViewById(R.id.exclusive_container);
        mDownload = (Button) view.findViewById(R.id.download);
        mModality = (TextView) view.findViewById(R.id.modality_value);
        mNumber = (TextView) view.findViewById(R.id.number_value);
        mDate = (TextView) view.findViewById(R.id.date_value);
        mAmount = (TextView) view.findViewById(R.id.amount_value);
        mObject = (TextView) view.findViewById(R.id.object_value);
        mAgency = (TextView) view.findViewById(R.id.agency_value);

        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
    }

    private void handleArguments() {
        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            String id = args.getString(NOTICE_ID);
            boolean fromNotification = args.getBoolean(FROM_NOTIFICATION);
            if (!TextUtils.isEmpty(id)) {
                if (!fromNotification) {
                    mLocalNotice = mRealm.where(Notice.class).equalTo("id", id).findFirst();
                    if (mLocalNotice == null) {
                        mParentActivity.finish();
                        return;
                    }
                    fillView();
                } else {
                    showProgress();

                    NoticeByIdQuery query = NoticeByIdQuery.builder()
                            .id(id)
                            .build();

                    mNoticeByIdCall = mApplication.getApolloClient()
                            .query(query)
                            .cacheControl(CacheControl.NETWORK_ONLY);

                    mNoticeByIdCall.enqueue(new ApolloCall.Callback<NoticeByIdQuery.Data>() {
                        @Override
                        public void onResponse(@Nonnull final Response<NoticeByIdQuery.Data> response) {
                            if (!response.hasErrors()) {
                                mParentActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgress();
                                        mLocalNotice = NoticeUtils.mapToRealmFromGraphQL(response.data().notice());
                                        fillView();
                                    }
                                });
                            } else {
                                if (mParentActivity != null) {
                                    mParentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mParentActivity, getString(R.string.bidding_details_error), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    mParentActivity.finish();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            e.printStackTrace();

                            if (mParentActivity != null) {
                                mParentActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mParentActivity, getString(R.string.bidding_details_error), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                mParentActivity.finish();
                            }
                        }
                    });
                }
            }
        }
    }

    /* Fill views with data */
    private void fillView() {
        mExclusive.setVisibility(mLocalNotice.isExclusive() ? View.VISIBLE : View.GONE);
        mModality.setText(NoticeUtils.resolveEnumNameToName(mLocalNotice.getModality()));
        mNumber.setText(mLocalNotice.getNumber());
        mObject.setText(mLocalNotice.getObject());
        mDate.setText(DateUtils.format(mLocalNotice.getDisputeDate()));
        mAgency.setText(mLocalNotice.getAgency().getName());

        String text = mLocalNotice.getAmount() == 0.0 ? "Não informado" : getString(R.string.modality_estimated_amount_value,
                StringUtils.getPriceInBrazil(mLocalNotice.getAmount()));
        mAmount.setText(text);
    }

    public void download() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            NoticeUtils.download(getContext(), mLocalNotice, this);
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), getString(R.string.storage_rationale_download), Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_WRITE_EXT_STORAGE);
        }
    }

    /**
     * Sees in the official website
     */
    public void seeInWebsite() {
        if (mLocalNotice == null) {
            return;
        }

        if (!hasWebsiteUrl()) {
            return;
        }

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getContext(), Uri.parse(mLocalNotice.getUrl()));
    }

    public void seeOnline() {
        if (mLocalNotice == null) {
            return;
        }

        if (!hasPdfDirectLink()) {
            return;
        }

        NoticeUtils.seeOnline(getContext(), mLocalNotice);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXT_STORAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download();
            } else {
                Toast.makeText(getContext(), getString(R.string.download_cancel_no_permission_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (mParentActivity != null) {
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
        if (mParentActivity != null) {
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

    /**
     * Whether the model has a url or not
     */
    public boolean hasWebsiteUrl() {
        if (mLocalNotice != null) {
            return !TextUtils.isEmpty(mLocalNotice.getUrl());
        }

        return false;
    }

    /**
     * Has pdf direct links
     */
    public boolean hasPdfDirectLink() {
        if (mLocalNotice != null) {
            return FileUtils.isPdf(mLocalNotice.getLink());
        }

        return false;
    }

    private void showProgress() {
        if (mParentActivity != null) {
            mParentActivity.getProgressBar().setVisibility(View.VISIBLE);
            mParentActivity.getContentView().setVisibility(View.GONE);
        }
    }

    private void hideProgress() {
        if (mParentActivity != null) {
            mParentActivity.getProgressBar().setVisibility(View.GONE);
            mParentActivity.getContentView().setVisibility(View.VISIBLE);
        }
    }
}
