package com.tnmlicitacoes.app.ui.accountconfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SegmentsQuery;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.type.OrderDirection;
import com.tnmlicitacoes.app.type.SegmentOrder;
import com.tnmlicitacoes.app.type.SegmentOrderField;
import com.tnmlicitacoes.app.ui.adapter.SegmentAdapter;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

public class SegmentSelectFragment extends AccountConfigurationFragment implements OnClickListenerRecyclerView {

    /* The logging and fragment tag */
    public static final String TAG = "SegmentsSelectFragment";

    /* The application singleton */
    private TnmApplication mApplication;

    /* Displays the city list */
    private RecyclerView mSegmentsRecyclerView;

    /* The adapter that holds the segment data */
    private SegmentAdapter mSegmentAdapter;

    /* Displays the no segments found text */
    private TextView mNoItemText;

    /* Displays the progress bar */
    private ProgressBar mProgressBar;

    /* SegmentsQuery call */
    private ApolloCall<SegmentsQuery.Data> mSegmentsCall;

    /* The RealmDB instance */
    private Realm mRealm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mApplication = (TnmApplication) getActivity().getApplication();
        mRealm = Realm.getDefaultInstance();

        View v = inflater.inflate(R.layout.fragment_segment_select, container, false);
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
                .orderBy(segmentOrder)
                .build();

        mSegmentsCall = mApplication.getApolloClient()
                .query(segmentsQuery)
                .cacheControl(CacheControl.NETWORK_FIRST);
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
        mSegmentsRecyclerView = (RecyclerView) view.findViewById(R.id.segmentsRecyclerView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        // Initialization of adapter
        mSegmentAdapter = new SegmentAdapter(getContext());
        mSegmentAdapter.setListenerHack(this);

        if(savedInstanceState == null) {
            mSegmentAdapter.setItems(new ArrayList<SegmentsQuery.Edge>());
        }

        mNoItemText = (TextView) view.findViewById(R.id.text1);
        mNoItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = Utils.sendContactEmail("[Segmento]", "");
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.send_email_contact)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), getString(R.string.no_email_clients_installed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialization of RecyclerView
        mSegmentsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), R.drawable.segment_item_divider));
        mSegmentsRecyclerView.setHasFixedSize(true);
        mSegmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mSegmentsRecyclerView.setAdapter(mSegmentAdapter);


        mSegmentsRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
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
}