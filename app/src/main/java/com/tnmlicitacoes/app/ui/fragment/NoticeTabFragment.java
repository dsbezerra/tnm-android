package com.tnmlicitacoes.app.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.NoticesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TNMApplication;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.interfaces.OnNoticeActionsDialogListener;
import com.tnmlicitacoes.app.service.DownloadService;
import com.tnmlicitacoes.app.type.NoticeOrder;
import com.tnmlicitacoes.app.type.NoticeOrderField;
import com.tnmlicitacoes.app.type.OrderDirection;
import com.tnmlicitacoes.app.ui.activity.MainActivity;
import com.tnmlicitacoes.app.ui.activity.WebviewActivity;
import com.tnmlicitacoes.app.ui.adapter.NoticeAdapter;
import com.tnmlicitacoes.app.ui.view.CustomRecyclerView;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.FileUtils;
import com.tnmlicitacoes.app.utils.NoticeUtils;

import java.io.File;
import java.util.ArrayList;

import okhttp3.HttpUrl;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class NoticeTabFragment extends Fragment implements OnClickListenerRecyclerView,
        DownloadService.OnDownloadListener, OnNoticeActionsDialogListener {

    /* Tag for logging */
    private static final String TAG = "NoticeTabFragment";

    private ProgressBar mProgressBar;

    /* Holds a reference for the refresh layout */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /* Holds a reference for the recycler view */
    private CustomRecyclerView mNoticesRecyclerView;

    /* The notice adapter */
    private NoticeAdapter mNoticeAdapter;

    /* View that displays a no connection message */
    private TextView mNoConnectionView;

    private TextView mNotFoundView;

    private boolean mIsFirstLoad = true;

    /* Indicate whether is loading more or not */
    private boolean mIsLoadingMore = false;

    /* Default items to load at each fetch call */
    private static int DEFAULT_LIMIT_ITEMS = 10;

    /* Holds a reference to the main activity */
    private MainActivity mActivity;

    /* The application singleton */
    private TNMApplication mApplication;

    /* The Apollo notices query call */
    private ApolloCall<NoticesQuery.Data> mNoticesCall;

    /* The notices query pageInfo */
    private NoticesQuery.Data.PageInfo mPageInfo = null;

    /* Request code of the write ext storage permission */
    private static final int PERMISSION_REQUEST_WRITE_EXT_STORAGE = 1041231;

    /* Stores the last touched notice */
    private NoticesQuery.Data.Node mLastTouchedNotice = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mApplication = (TNMApplication) getActivity().getApplication();
        View v = inflater.inflate(R.layout.fragment_notices, container, false);
        initAdapter();
        initViews(v);
        initViewsListeners();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchNotices(null);
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
    public void onDestroy() {
        super.onDestroy();
        mNoticeAdapter.setOnClickListener(null);
        mActivity = null;
    }

    private void initAdapter() {
        mNoticeAdapter = new NoticeAdapter(getContext());
        mNoticeAdapter.setOnClickListener(this);
    }

    private void initViews(View v) {
        mNotFoundView           = (TextView) v.findViewById(R.id.notFoundView);
        mNoConnectionView       = (TextView) v.findViewById(R.id.noConnectionView);
        mNoticesRecyclerView    = (CustomRecyclerView) v.findViewById(R.id.noticeRecyclerView);
        mProgressBar            = (ProgressBar) v.findViewById(R.id.recyclerProgressBar);
        mSwipeRefreshLayout     = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);

        mProgressBar.setVisibility(View.GONE);
        mNoConnectionView.setVisibility(View.GONE);

        mNoticesRecyclerView.setHasFixedSize(true);
        mNoticesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mNoticesRecyclerView.setAdapter(mNoticeAdapter);

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorAccent);
    }

    private void initViewsListeners() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mNoticesRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llm = (LinearLayoutManager) mNoticesRecyclerView.getLayoutManager();
                if (mNoticeAdapter.getItemCount() == llm.findLastCompletelyVisibleItemPosition() + 1) {
                    if (!mIsLoadingMore && mPageInfo.hasNextPage()) {
                        mNoticesRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                fetchMore();
                                mIsLoadingMore = true;
                            }
                        });
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        mNoConnectionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNotices(null);
            }
        });
    }

    private void fetchNotices(Object after) {
        if (mNoticeAdapter.getItemCount() == 0) {
            mIsFirstLoad = true;
            mIsLoadingMore = false;
            //showProgressBar();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        // Get the segment for this tab
        String segId = getArguments().getString("segId");

        final NoticeOrder noticeOrder = NoticeOrder.builder()
                .field(NoticeOrderField.DISPUTE_DATE)
                .order(OrderDirection.DESC)
                .build();

        final NoticesQuery citiesQuery = NoticesQuery.builder()
                .first(DEFAULT_LIMIT_ITEMS)
                .segId(segId)
                .after(after)
                .orderBy(noticeOrder)
                .build();

        mNoticesCall = mApplication.getApolloClient()
                .newCall(citiesQuery)
                .cacheControl(CacheControl.NETWORK_FIRST);
        mNoticesCall.enqueue(dataCallback);
    }

    private ApolloCall.Callback<NoticesQuery.Data> dataCallback = new ApolloCall.Callback<NoticesQuery.Data>() {
        @Override
        public void onResponse(final Response<NoticesQuery.Data> response) {

            if (response.isSuccessful()) {
                mPageInfo = response.data().notices().pageInfo();

                // Update views
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsLoadingMore) {
                                mNoticeAdapter.remove(mNoticeAdapter.getItemCount() - 1);
                                mNoticeAdapter.append(new ArrayList<>(response.data().notices().edges()));
                                mIsLoadingMore = false;
                            } else {
                                mNoticeAdapter.setItems(new ArrayList<>(response.data().notices().edges()));
                            }
                            mProgressBar.setVisibility(View.GONE);
                            mNoticesRecyclerView.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }

                if (mIsFirstLoad) {
                    mIsFirstLoad = false;
                }
            }
        }

        @Override
        public void onFailure(ApolloException e) {

        }
    };

    private void fetchMore() {
        if(mIsLoadingMore || mNoticeAdapter.getItemCount() == 0) {
            return;
        }

        mIsLoadingMore = true;
        mNoticeAdapter.add(null, mNoticeAdapter.getItemCount());

        fetchNotices(mPageInfo.endCursor());
    }

    @Override
    public void OnClickListener(View v, int position) {
        final NoticesQuery.Data.Node notice = mNoticeAdapter.getItem(position);
        if(notice == null) {
            return;
        }

        mLastTouchedNotice = notice;

        // Show bottom sheet for this notice
        NoticeActionsDialog bottomSheet = new NoticeActionsDialog();
        bottomSheet.setListener(this);
        bottomSheet.show(getFragmentManager(), bottomSheet.getTag());
    }

    private void showProgressBar() {
        mNoConnectionView.setVisibility(View.GONE);
        mNoticesRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mNoticesRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void showNoConnectionView() {
        mNoticesRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mNoConnectionView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the notice in a webview
     * @param pageTitle custom page title
     * @param noticeLink notice link
     */
    private void showOnline(String pageTitle, String noticeLink) {

        if(!AndroidUtilities.verifyConnection(getContext())) {
            if(getView() != null) {
                Snackbar.make(getView(), "Sem conexão, por favor conecte e tente novamente",
                        Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        if(HttpUrl.parse(noticeLink) != null) {
            Intent webviewIntent = new Intent(getContext(), WebviewActivity.class);
            webviewIntent.putExtra(WebviewActivity.PAGE_TITLE, pageTitle);
            webviewIntent.putExtra(WebviewActivity.PAGE_LINK, noticeLink);
            webviewIntent.putExtra(WebviewActivity.IS_PDF_FILE, noticeLink.endsWith(".pdf"));
            webviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(webviewIntent);
            overridePendingTransition();
        } else {
            Toast.makeText(getContext(), getString(R.string.download_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private void startDownloadProcess() {
        if(!AndroidUtilities.verifyConnection(getContext())) {
            if(getView() != null) {
                Snackbar.make(getView(), getString(R.string.no_connection_try_again),
                        Snackbar.LENGTH_SHORT).show();
            }

            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startDownload(mLastTouchedNotice);
        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), getString(R.string.storage_rationale_download), Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_WRITE_EXT_STORAGE);
        }
    }

    private void startDownload(NoticesQuery.Data.Node notice) {
        if(notice == null)
            return;

        final String fileLink = notice.link();
        if(TextUtils.isEmpty(fileLink) || HttpUrl.parse(fileLink) == null) {
            LOG_DEBUG(TAG, "Invalid link.");
            Toast.makeText(getContext(), getString(R.string.download_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }

        LOG_DEBUG(TAG, "Starting download...");
        final String fileName = NoticeUtils.resolveModalityToName(notice.modality()) + " - " +
                notice.number().replaceAll("/", "-") + ".pdf";

        LOG_DEBUG(TAG, "Filename: " + fileName);
        LOG_DEBUG(TAG, "Link: " + fileLink);

        Intent intent = new Intent(getContext(), DownloadService.class);
        intent.putExtra("LINK", fileLink);
        intent.putExtra("NAME", fileName);
        intent.putExtra("NOTIFICATION_ID", fileLink.length() + (Math.random() * 10001) + 10000);
        mActivity.startService(intent);

    }

    private void overridePendingTransition() {
        getActivity().overridePendingTransition(R.anim.activity_fade_enter,
                R.anim.activity_fade_exit);
    }

    public void startAnimation() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mNoticesRecyclerView.setAlpha(0);
            ViewCompat.animate(mNoticesRecyclerView)
                    .setDuration(250)
                    .alpha(1)
                    .withLayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_WRITE_EXT_STORAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadProcess();
            } else {
                Toast.makeText(getContext(), getString(R.string.download_cancel_no_permission_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Refreshes the fetched data
     */
    public void refreshData() {
        mIsFirstLoad = true;
        mIsLoadingMore = false;
        //showProgressBar();
        fetchNotices(null);
    }

    @Override
    public void onDownloadStart() {
        Activity activity = getActivity();
        if(activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), getString(R.string.start_download_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDownloadFailure(final String fileName) {
        Activity activity = getActivity();
        if(activity != null) {

            /*if(mLastDownloadedNotice != null) {
                AnalyticsUtils.fireEvent(getContext(),
                        "Download",
                        "Falha",
                        mLastDownloadedNotice.id,
                        1);
            }*/

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View view = getView();
                    if(view == null)
                        return;

                    Snackbar.make(view, getString(R.string.download_fail_messsage, fileName), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.snackbar_action_try_again), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //startDownload(mLastDownloadedNotice);
                                }
                            }).show();
                }
            });
        }
    }

    @Override
    public void onDownloadFinished(final File file) {
        Activity activity = getActivity();
        if(activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                View view = getView();
                if(view == null)
                    return;

                Snackbar.make(view, getString(R.string.download_finished_with_filename, file.getName()), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.snackbar_action_open), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String fileName = file.getName();
                                boolean isPdfFile = fileName.endsWith(".pdf");

                                if (isPdfFile) {
                                    FileUtils.openPdf(getContext(), file);
                                } else {
                                    // TODO(diego): Tentar abrir com algum aplicativo diferente
                                }
                            }
                        }).show();
            }
        });
    }


    @Override
    public void onSeeDetailsClicked() {
        Toast.makeText(mActivity, "onSeeDetailsClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewOnlineClicked() {
        String pageTitle = NoticeUtils.resolveModalityToName(mLastTouchedNotice.modality()) + " - " +
                mLastTouchedNotice.number();
        showOnline(pageTitle, mLastTouchedNotice.link());
    }

    @Override
    public void onSendToEmailClicked() {
        Toast.makeText(mActivity, "onSendToEmailClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadClicked() {
        startDownloadProcess();
    }
}
