package com.tnmlicitacoes.app.accountconfiguration;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.apollo.SegmentsQuery;
import com.tnmlicitacoes.app.apollo.type.OrderDirection;
import com.tnmlicitacoes.app.apollo.type.SegmentOrder;
import com.tnmlicitacoes.app.apollo.type.SegmentOrderField;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.realm.LocalSupplier;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.registration.RegistrationActivity;
import com.tnmlicitacoes.app.ui.adapter.SegmentAdapter;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.UIUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class SelectSegmentFragment extends AccountConfigurationFragment implements
        OnClickListenerRecyclerView, RegistrationActivity.RegistrationContent {

    /* The logging and fragment tag */
    public static final String TAG = "SegmentsSelectFragment";

    /* The application singleton */
    private TnmApplication mApplication;

    /** The segments search field */
    private EditText mSearchField;

    /* Displays the city list */
    private RecyclerView mSegmentsRecyclerView;

    /* The adapter that holds the segment data */
    private SegmentAdapter mSegmentAdapter;

    /* Displays the progress bar */
    private ProgressBar mProgressBar;

    /* SegmentsQuery call */
    private ApolloCall<SegmentsQuery.Data> mSegmentsCall;

    /* The RealmDB instance */
    private Realm mRealm;

    /** Hide */
    private boolean mIsScrolled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mApplication = (TnmApplication) getActivity().getApplication();
        mRealm = Realm.getDefaultInstance();
        View v = inflater.inflate(R.layout.fragment_select_segment, container, false);
        initViews(v, savedInstanceState);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!TnmApplication.IsRefreshingToken) {
            fetchSegments();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();

        if (mSegmentsCall != null) {
            mSegmentsCall.cancel();
        }
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    /**
     * Fetch segments from backend
     */
    public void fetchSegments() {
        final SegmentOrder segmentOrder = SegmentOrder.builder()
                .field(SegmentOrderField.NAME)
                .order(OrderDirection.ASC)
                .build();

        final SegmentsQuery segmentsQuery = SegmentsQuery.builder()
                .first(100)
                .active(true)
                .orderBy(segmentOrder)
                .build();

        mSegmentsCall = mApplication.getApolloClient()
                .query(segmentsQuery);
        mSegmentsCall.enqueue(dataCallback);
    }

    private ApolloCall.Callback<SegmentsQuery.Data> dataCallback = new ApolloCall.Callback<SegmentsQuery.Data>() {
        @Override
        public void onResponse(final Response<SegmentsQuery.Data> response) {
            if (!response.hasErrors()) {
                // Update views
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSegmentAdapter.setItems(response.data().segments().edges());
                        mProgressBar.setVisibility(View.GONE);
                        mSegmentsRecyclerView.setVisibility(View.VISIBLE);

                        restoreSavedSegments();
                    }
                });
            } else {
                for (Error e : response.errors()) {
                    if (e.message().equals("Unauthorized access")) {
                        AccountConfigurationActivity activity = (AccountConfigurationActivity) getActivity();
                        activity.refreshToken();
                    }
                }
            }
        }

        @Override
        public void onFailure(ApolloException e) {

        }
    };

    private void initViews(View view, Bundle savedInstanceState) {
        mSegmentsRecyclerView = (RecyclerView) view.findViewById(R.id.pick_segments_list);
        mSearchField = (EditText) view.findViewById(R.id.pick_segments_search_field);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        // Initialization of adapter
        mSegmentAdapter = new SegmentAdapter(getContext());
        mSegmentAdapter.setListenerHack(this);

        if(savedInstanceState == null) {
            mSegmentAdapter.setItems(new ArrayList<SegmentsQuery.Edge>());
        }

        // Initialization of RecyclerView
        mSegmentsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), R.drawable.segment_item_divider));
        mSegmentsRecyclerView.setHasFixedSize(true);
        mSegmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mSegmentsRecyclerView.setAdapter(mSegmentAdapter);

        mSegmentsRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSearchField();
                mSegmentAdapter.getFilter().filter(mSearchField.getText().toString().trim());
            }
        });

        // Sets the maximum possible to pick segments
        LocalSupplier localSupplier = mRealm.where(LocalSupplier.class).findFirst();
        if (localSupplier != null) {
            mSegmentAdapter.setMax(localSupplier.getSegNum());
        }

        if (mCallback != null) {
            mCallback.onCompleteInitialisation(TAG);
        }

        updateSearchField();
    }

    private void updateSearchField() {
        if (mSearchField.length() != 0) {
            UIUtils.Companion.setRightDrawable(mSearchField,
                    R.drawable.ic_clear_select_search_field);
            mSearchField.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        int drawableLeftX = mSearchField.getRight() - mSearchField
                                .getCompoundDrawables()[2].getBounds().width();
                        if (event.getX() >= drawableLeftX && mSearchField.length() != 0) {
                            clearField();
                        }
                    }

                    return false;
                }
            });
        } else {
            clearField();
        }
    }

    private void clearField() {
        mSearchField.getText().clear();
        UIUtils.Companion.setRightDrawable(mSearchField, 0);
        mSearchField.setOnTouchListener(null);
    }

    /**
     * Restores saved segments from database
     */
    private void restoreSavedSegments() {
        final RealmResults<PickedSegment> selectedSegments = mRealm.where(PickedSegment.class).findAll();
        if (selectedSegments.size() > 0) {
            HashMap<String, SegmentsQuery.Node> selected = new HashMap<>();
            for (int i = 0; i < selectedSegments.size(); i++) {
                PickedSegment selectedSegment = selectedSegments.get(i);
                SegmentsQuery.Node segment = new SegmentsQuery.Node("Segment", selectedSegment.getId(),
                        selectedSegment.getName(), selectedSegment.getIcon(),
                        selectedSegment.getDefaultImg(), selectedSegment.getMqdefault(),
                        selectedSegment.getHqdefault());
                selected.put(selectedSegment.getId(), segment);
            }
            mSegmentAdapter.setSelected(selected);
            mCallback.onSegmentSelected(selected.size(), null);
        }
    }

    @Override
    public void OnClickListener(View v, int position) {
        int newCount = mSegmentAdapter.select(position);
        mCallback.onSegmentSelected(newCount, mSegmentAdapter.getItem(position));
    }

    /**
     * Gets the adapter
     */
    public SegmentAdapter getAdapter() {
        return mSegmentAdapter;
    }

    @Override
    public int getMaximum() {
        return getAdapter().getMax();
    }

    @Override
    public boolean shouldDisplayBackArrow() {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return "Segmento(s) de interesse";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Escolha abaixo o(s) segmento(s) de atuação da sua empresa";
    }

    @Override
    public int getToolbarHeight(Activity activity) {
        return AndroidUtilities.dp(activity, 140.0f);
    }

    @Override
    public boolean shouldCollapseToolbarOnScroll() {
        return true;
    }

    @Nullable
    @Override
    public String getBottomInfoText() {
        if (mSegmentAdapter != null) {
            return mSegmentAdapter.getSelectedCount() + "/" + getMaximum();
        }
        return null;
    }

    @Nullable
    @Override
    public View getFocusView() {
        if (mSearchField.isFocusable() && mSearchField.hasFocus()) {
            return mSearchField;
        }
        return null;
    }

    @Override
    public boolean shouldDisplay(LocalSupplier localSupplier) {
        if (localSupplier == null) {
            return true;
        }

        List<String> segments = localSupplier.getSegmentsIds();
        return segments != null && segments.size() == 0;
    }

    @Override
    public void setError(String message) {}

    @Override
    public boolean shouldEnableAdvanceButton() {
        return mSegmentAdapter != null && mSegmentAdapter.getSelectedCount() != 0;
    }
}
