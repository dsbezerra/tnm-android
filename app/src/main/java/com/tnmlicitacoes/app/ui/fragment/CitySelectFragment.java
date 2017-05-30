package com.tnmlicitacoes.app.ui.fragment;

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
import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TNMApplication;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.City;
import com.tnmlicitacoes.app.type.CityOrder;
import com.tnmlicitacoes.app.type.CityOrderField;
import com.tnmlicitacoes.app.type.OrderDirection;
import com.tnmlicitacoes.app.type.State;
import com.tnmlicitacoes.app.ui.activity.AccountConfigurationActivity;
import com.tnmlicitacoes.app.ui.adapter.CityAdapter;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

public class CitySelectFragment extends AccountConfigurationFragment
        implements OnClickListenerRecyclerView {

    private static final String TAG = "CitySelectFragment";

    private RecyclerView mRecyclerView;

    private TextView mNoItemText;

    private ProgressBar mProgressBar;

    private CityAdapter mCityAdapter;

    private TNMApplication mApplication;

    private ApolloCall<CitiesQuery.Data> mCitiesCall;

    private Realm mRealm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mApplication = (TNMApplication) getActivity().getApplication();
        mRealm = Realm.getDefaultInstance();

        View v = inflater.inflate(R.layout.fragment_city_select, container, false);
        initViews(v, savedInstanceState);
        return v;
    }

    @Override
    public void OnClickListener(View v, int position) {
        int newCount = mCityAdapter.addToSelected(position);
        mCallback.onCitySelected(newCount, mCityAdapter.getItem(position));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!TNMApplication.IsRefreshingToken) {
            fetchCities();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
        if (mCitiesCall != null) {
            mCitiesCall.cancel();
        }
    }

    /**
     * Fetch cities from backend
     */
    public void fetchCities() {
        final CityOrder cityOrder = CityOrder.builder()
                .field(CityOrderField.NAME)
                .order(OrderDirection.ASC)
                .build();

        final CitiesQuery citiesQuery = CitiesQuery.builder()
                .first(10)
                .orderBy(cityOrder)
                .build();

        mCitiesCall = mApplication.getApolloClient()
                .newCall(citiesQuery)
                .cacheControl(CacheControl.NETWORK_FIRST);
        mCitiesCall.enqueue(dataCallback);
    }

    private ApolloCall.Callback<CitiesQuery.Data> dataCallback = new ApolloCall.Callback<CitiesQuery.Data>() {
        @Override
        public void onResponse(final Response<CitiesQuery.Data> response) {
            if (response.isSuccessful()) {
                // Update views
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCityAdapter.setItems(response.data().cities().edges());
                        mProgressBar.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);

                        restoreSavedCities();
                    }
                });
            }
            else {
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

    private void initViews(final View v, Bundle savedInstanceState) {
        // Initialization of adapter
        mCityAdapter = new CityAdapter(getContext());
        mCityAdapter.setListenerHack(this);

        if(savedInstanceState == null) {
            mCityAdapter.setItems(new ArrayList<CitiesQuery.Data.Edge>());
        }

        mNoItemText = (TextView) v.findViewById(R.id.text1);

        mNoItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = Utils.sendContactEmail("[Cidade]", "");
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.send_email_contact)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), getString(R.string.no_email_clients_installed), Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Initialization of RecyclerView
        mRecyclerView = (RecyclerView) v.findViewById(R.id.citiesRecyclerView);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mCityAdapter);


        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
    }

    /**
     * Restores saved cities from database
     */
    private void restoreSavedCities() {
        final RealmResults<City> selectedCities = mRealm.where(City.class).findAll();
        if (selectedCities.size() > 0) {
            HashMap<String, CitiesQuery.Data.Node> selected = new HashMap<>();
            for (int i = 0; i < selectedCities.size(); i++) {
                City selectedCity = selectedCities.get(i);
                CitiesQuery.Data.Node city = new CitiesQuery.Data.Node(selectedCity.getId(),
                        selectedCity.getName(), State.valueOf(selectedCity.getState()));
                selected.put(selectedCity.getId(), city);
            }
            mCityAdapter.setSelected(selected);
            mCallback.onCitySelected(selected.size(), null);
        }
    }
}
