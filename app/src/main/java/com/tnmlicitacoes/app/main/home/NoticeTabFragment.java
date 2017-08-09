package com.tnmlicitacoes.app.main.home;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.apollo.NoticesQuery;
import com.tnmlicitacoes.app.apollo.type.Modality;
import com.tnmlicitacoes.app.apollo.type.NoticeOrder;
import com.tnmlicitacoes.app.apollo.type.NoticeOrderField;
import com.tnmlicitacoes.app.apollo.type.OrderDirection;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.interfaces.OnNoticeActionsDialogListener;
import com.tnmlicitacoes.app.main.MainActivity;
import com.tnmlicitacoes.app.model.realm.Agency;
import com.tnmlicitacoes.app.model.realm.City;
import com.tnmlicitacoes.app.model.realm.Notice;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.model.realm.Segment;
import com.tnmlicitacoes.app.service.DownloadService;
import com.tnmlicitacoes.app.ui.adapter.NoticeAdapter;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;
import com.tnmlicitacoes.app.utils.FileUtils;
import com.tnmlicitacoes.app.utils.NoticeUtils;
import com.tnmlicitacoes.app.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.main.NoticeFilterDialog.FILTER_EXCLUSIVE;
import static com.tnmlicitacoes.app.main.NoticeFilterDialog.FILTER_MODALITY;
import static com.tnmlicitacoes.app.main.NoticeFilterDialog.FILTER_REGION;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_ERROR;

public class NoticeTabFragment extends BaseFragment implements OnClickListenerRecyclerView,
        DownloadService.OnDownloadListener, OnNoticeActionsDialogListener {

    /* Tag for logging */
    private static final String TAG = "NoticeTabFragment";

    /* Request code of the write ext storage permission */
    private static final int PERMISSION_REQUEST_WRITE_EXT_STORAGE = 10412;

    /* Holds a reference for the refresh layout */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /* Holds a reference for the recycler view */
    private RecyclerView mNoticesRecyclerView;

    /* The notice adapter */
    private NoticeAdapter mNoticeAdapter;

    /* View that displays a no connection message */
    private TextView mNoConnectionView;

    /* View that displays a not found text */
    private LinearLayout mNotFoundView;

    /* Indicate whether is loading more or not */
    private boolean mIsLoadingMore = false;

    /* Default items to load at each fetch call */
    private static int DEFAULT_LIMIT_ITEMS = 10;

    /* Holds a reference to the main activity */
    private MainActivity mActivity;

    /* The application singleton */
    private TnmApplication mApplication;

    /* The Apollo notices query call */
    private ApolloCall<NoticesQuery.Data> mNoticesCall;

    /* The notices query pageInfo */
    private NoticesQuery.PageInfo mPageInfo = null;

    /* Stores the last touched notice */
    private Notice mLastTouchedNotice = null;

    /* Realm instance */
    private Realm mRealm;

    /* RealmResults 'live' notices */
    private RealmResults<Notice> mNoticeResults;
    private RealmResults<PickedCity> mCityResults;

    /* Last filter */
    private HashMap<String, Object> mLastFilterParams;
    private boolean mIsFiltering = false;

    /* Arguments used to identify this tab fragment */
    private static final String SEGMENT_ID = "segId";
    private static final String SEGMENT_NAME = "segName";

    /**
     * Returns a newInstance of this fragment
     * @param segment a Segment
     * @return NoticeTabFragment instance
     */
    public static NoticeTabFragment newInstance(PickedSegment segment) {

        Bundle args = new Bundle();
        args.putString(SEGMENT_ID, segment.getId());
        args.putString(SEGMENT_NAME, segment.getName());

        NoticeTabFragment fragment = new NoticeTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mApplication = (TnmApplication) getActivity().getApplication();
        mRealm = Realm.getDefaultInstance();
        View v = inflater.inflate(R.layout.fragment_notices, container, false);
        initAdapter();
        initViews(v);
        initViewsListeners();

        // Update adapter every time notice results update
        mRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                mNoticeAdapter.setItems(new ArrayList<>(mNoticeResults));
            }
        });

        mNoticeResults = mRealm.where(Notice.class)
                .equalTo("segId", getArguments().getString(SEGMENT_ID))
                .findAll();
        mCityResults = mRealm.where(PickedCity.class)
                .findAll();

        // Delete all obsolete notices
        deleteObsoletesIfAny();

        mNoticeAdapter.setItems(new ArrayList<>(mNoticeResults));

        if (!TnmApplication.IsRefreshingToken) {
            fetchNotices(null);
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
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
        if (mNoticesCall != null) {
            mNoticesCall.cancel();
        }
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    private void initAdapter() {
        mNoticeAdapter = new NoticeAdapter();
        mNoticeAdapter.setOnClickListener(this);
    }

    private void initViews(View v) {
        mNotFoundView           = (LinearLayout) v.findViewById(R.id.notFoundView);
        mNoConnectionView       = (TextView) v.findViewById(R.id.noConnectionView);
        mNoticesRecyclerView    = (RecyclerView) v.findViewById(R.id.noticeRecyclerView);
        mSwipeRefreshLayout     = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);

        mNoConnectionView.setVisibility(View.GONE);

        mNoticesRecyclerView.setHasFixedSize(true);
        mNoticesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mNoticesRecyclerView.setAdapter(mNoticeAdapter);
        mNoticesRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(),
                R.drawable.notice_item_divider));

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary
        );
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
                    if (!mIsLoadingMore && mPageInfo != null && mPageInfo.hasNextPage()) {
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

    private void deleteObsoletesIfAny() {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                boolean deleted = false;
                deleted = realm.where(Notice.class)
                        .equalTo("segId", NoticeTabFragment.this.getArguments().getString(SEGMENT_ID))
                        .lessThan("disputeDate", new Date())
                        .findAll()
                        .deleteAllFromRealm();

                if (deleted) {
                    LOG_DEBUG(TAG, "Deleted obsoletes of " + NoticeTabFragment.this.getArguments().getString(SEGMENT_NAME));
                }
            }
        });
    }

    public void fetchNotices(Object after) {
        // Get the segment for this tab
        String segId = getArguments().getString(SEGMENT_ID);
        String segName = getArguments().getString(SEGMENT_NAME);

        LOG_DEBUG(TAG, "Fetching notices of " + segName + "...");

        final NoticeOrder noticeOrder = NoticeOrder.builder()
                .field(NoticeOrderField.DISPUTE_DATE)
                .order(OrderDirection.DESC)
                .build();

        final NoticesQuery citiesQuery = NoticesQuery.builder()
                .orderBy(noticeOrder)
                .cityId(getPickedCitiesId())
                .segId(segId)
                .first(DEFAULT_LIMIT_ITEMS)
                .after(after)
                .active(true)
                .build();

        mNoticesCall = mApplication.getApolloClient()
                .query(citiesQuery);
        mNoticesCall.enqueue(dataCallback);
    }

    private ApolloCall.Callback<NoticesQuery.Data> dataCallback = new ApolloCall.Callback<NoticesQuery.Data>() {
        @Override
        public void onResponse(final Response<NoticesQuery.Data> response) {

            final String segName = getArguments().getString(SEGMENT_NAME);
            if (!response.hasErrors()) {
                mPageInfo = response.data().notices().pageInfo();

                // Update views
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);

                            boolean wasLoadingMore = mIsLoadingMore;
                            if (mIsLoadingMore) {
                                mNoticeAdapter.remove(mNoticeAdapter.getItemCount() - 1);
                                mIsLoadingMore = false;
                            }

                            List<Notice> notices = mapToListOfNotices(new ArrayList<>(response.data().notices().edges()));
                            if (mIsFiltering) {
                                if (wasLoadingMore) {
                                    mNoticeAdapter.append(notices);
                                } else {
                                    mNoticeAdapter.setItems(notices);
                                }
                            } else {
                                // Persist values only if they're new!
                                persistIfNew(notices);
                            }

                            boolean hasItems = mNoticeAdapter.getItemCount() > 0;
                            if (hasItems) {
                                mNoticesRecyclerView.setVisibility(View.VISIBLE);
                                mNotFoundView.setVisibility(View.GONE);
                            } else {
                                mNoticesRecyclerView.setVisibility(View.GONE);
                                mNotFoundView.setVisibility(View.VISIBLE);
                                ((TextView) mNotFoundView.findViewById(R.id.text))
                                        .setText("No momento não encontramos licitações para este segmento.");
                            }
                        }
                    });
                }

                LOG_DEBUG(TAG, "Finished fetching notices of " + segName + "...");

            } else {

                LOG_ERROR(TAG, "Failed to fetch notices of " + segName + "!!");

                for (Error e : response.errors()) {
                    if (e.message().equals("Unauthorized access")) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.refreshToken();
                    }

                    LOG_ERROR(TAG, e.message());
                }
            }
        }

        @Override
        public void onFailure(ApolloException e) {
            e.printStackTrace();
        }
    };

    /**
     * Fetches more notices from backend and append the results to the
     * current items
     */
    private void fetchMore() {
        if (mIsLoadingMore || mNoticeAdapter.getItemCount() == 0) {
            return;
        }

        mIsLoadingMore = true;
        mNoticeAdapter.add(null, mNoticeAdapter.getItemCount());

        if (mIsFiltering) {
            fetchNoticesFiltered(mPageInfo.endCursor());
        } else {
            fetchNotices(mPageInfo.endCursor());
        }

    }

    @Override
    public void OnClickListener(View v, int position) {
        final Notice notice = mNoticeAdapter.getItem(position);
        if (notice == null) {
            return;
        }

        mLastTouchedNotice = notice;

        // Show bottom sheet for this notice
        NoticeActionsDialog bottomSheet = new NoticeActionsDialog();
        bottomSheet.setListener(this);
        bottomSheet.show(getFragmentManager(), bottomSheet.getTag());
    }

    private List<Notice> mapToListOfNotices(List<NoticesQuery.Edge> edges) {
        List<Notice> result = new ArrayList<>();

        if (edges.size() == 0) {
            return result;
        }

        for (int i = 0; i < edges.size(); i++) {
            NoticesQuery.Node node = edges.get(i).node();
            result.add(NoticeUtils.mapToRealmFromGraphQL(node));
        }

        return result;
    }

    /**
     * Starts the download file process asking for permission if needed
     */
    private void startDownloadProcess() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            NoticeUtils.download(getContext(), mLastTouchedNotice, this);
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), getString(R.string.storage_rationale_download), Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_WRITE_EXT_STORAGE);
        }
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
        mPageInfo = null;
        mIsLoadingMore = false;
        mIsFiltering = false;
        mSwipeRefreshLayout.setRefreshing(true);
        fetchNotices(null);
    }


    private void fetchNoticesFiltered(Object after) {
        mLastFilterParams.put("after", after);
        fetchNoticesFiltered(null);
    }

    public void fetchNoticesFiltered(HashMap<String, Object> filterParams) {

        mIsFiltering = true;
        mPageInfo = null;

        if (filterParams != null) {
            mLastFilterParams = filterParams;
        }

        // Get the segment for this tab
        String segId = getArguments().getString(SEGMENT_ID);
        String segName = getArguments().getString(SEGMENT_NAME);

        LOG_DEBUG(TAG, "Fetching notices of " + segName + "...");

        final NoticeOrder noticeOrder = NoticeOrder.builder()
                .field(NoticeOrderField.DISPUTE_DATE)
                .order(OrderDirection.DESC)
                .build();

        NoticesQuery.Builder builder = NoticesQuery.builder()
                .first(DEFAULT_LIMIT_ITEMS)
                .segId(segId)
                .active(true)
                .orderBy(noticeOrder);

        if (mLastFilterParams.containsKey("after")) {
            builder = builder.after(mLastFilterParams.get("after"));
        }

        if (mLastFilterParams.containsKey(FILTER_MODALITY)) {
            Modality modality = Modality.valueOf((String) mLastFilterParams.get(FILTER_MODALITY));
            builder = builder.modality(modality);
        }

        if (mLastFilterParams.containsKey(FILTER_REGION)) {
            builder = builder.cityId(Collections.singletonList(mLastFilterParams.get(FILTER_REGION)));
        }

        if (mLastFilterParams.containsKey(FILTER_EXCLUSIVE)) {
            builder = builder.exclusive((Boolean) mLastFilterParams.get(FILTER_EXCLUSIVE));
        }

        final NoticesQuery citiesQuery = builder.build();

        mNoticesCall = mApplication.getApolloClient()
                .query(citiesQuery);
        mNoticesCall.enqueue(dataCallback);
    }

    @Override
    public void onDownloadStart() {
        Activity activity = getActivity();
        if (activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), getString(R.string.start_download_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get picked city ids
     */
    private List<Object> getPickedCitiesId() {
        List<Object> result = new ArrayList<>();

        int size = mCityResults.size();
        if (size == 0) {
            if (BuildConfig.DEBUG) {
                Toast.makeText(getContext(), "Ir para tela de escolher cidades.",
                        Toast.LENGTH_SHORT).show();
            }
            return result;
        }

        for (int i = 0; i < mCityResults.size(); i++) {
            PickedCity city = mCityResults.get(i);
            if (city != null && !TextUtils.isEmpty(city.getId())) {
                result.add(city.getId());
            }
        }
        return result;
    }

    private void persistIfNew(final List<Notice> notices) {
        if (notices.size() == 0) {
            LOG_DEBUG(TAG, "Nothing to persist!");
            return;
        }

        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < notices.size(); i++) {
                    Notice notice = notices.get(i);
                    if (notice != null) {
                        Agency agency = notice.getAgency();
                        Segment segment = notice.getSegment();
                        City city = agency.getCity();

                        realm.copyToRealmOrUpdate(city);
                        realm.copyToRealmOrUpdate(agency);
                        realm.copyToRealmOrUpdate(segment);
                        realm.copyToRealmOrUpdate(notice);
                    }
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                String segName = getArguments().getString(SEGMENT_NAME);
                LOG_DEBUG(TAG, "Total of notices (" + segName + ") : " + mNoticeResults.size());
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                LOG_DEBUG(TAG, error.getMessage());
            }
        });
    }

    public RecyclerView getList() {
        return mNoticesRecyclerView;
    }

    @Override
    public void onDownloadFailure(final String fileName) {
        Activity activity = getActivity();
        if (activity != null) {

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
                    View view = mActivity.findViewById(R.id.navigation);
                    if (getView() == null || view == null)
                        return;

                    UIUtils.Companion.createSnackbarAbove(getView(), view,
                            getString(R.string.download_fail_messsage, fileName), Snackbar.LENGTH_LONG)
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

                View view = mActivity.findViewById(R.id.navigation);
                if (getView() == null || view == null)
                    return;

                UIUtils.Companion.createSnackbarAbove(getView(), view,
                        getString(R.string.download_finished_with_filename, file.getName()), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.snackbar_action_open), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String fileName = file.getName();
                                boolean isPdfFile = FileUtils.isPdf(fileName);

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
        NoticeUtils.seeDetails(getContext(), mLastTouchedNotice);
    }

    @Override
    public void onViewOnlineClicked() {
        if (!NoticeUtils.seeOnline(getContext(), mLastTouchedNotice)) {
            Toast.makeText(getContext(), "Não é possível visualizar este edital",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSendToEmailClicked() {
        if (mLastTouchedNotice != null) {
            NoticeUtils.sendToEmail(mApplication, getActivity(), mLastTouchedNotice.getId());
        }
    }

    @Override
    public void onDownloadClicked() {
        startDownloadProcess();
    }
}
