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
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.realm.LocalSupplier;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.registration.RegistrationActivity;
import com.tnmlicitacoes.app.type.CityOrder;
import com.tnmlicitacoes.app.type.CityOrderField;
import com.tnmlicitacoes.app.type.OrderDirection;
import com.tnmlicitacoes.app.type.State;
import com.tnmlicitacoes.app.ui.adapter.CityAdapter;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.UIUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class SelectCityFragment extends AccountConfigurationFragment
        implements OnClickListenerRecyclerView, RegistrationActivity.RegistrationContent {

    /* The logging and fragment tag */
    public static final String TAG = "SelectCityFragment";

    /* The application singleton */
    private TnmApplication mApplication;

    /* Displays the city list */
    private RecyclerView mRecyclerView;

    /** Search field */
    private EditText mSearchField;

    /* The adapter that holds the city data */
    private CityAdapter mCityAdapter;

    /* Displays the progress bar */
    private ProgressBar mProgressBar;

    /* CitiesQuery call */
    private ApolloCall<CitiesQuery.Data> mCitiesCall;

    /* The RealmDB instance */
    private Realm mRealm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mApplication = (TnmApplication) getActivity().getApplication();
        mRealm = Realm.getDefaultInstance();

        View v = inflater.inflate(R.layout.fragment_select_city, container, false);
        initViews(v, savedInstanceState);
        return v;
    }

    @Override
    public void OnClickListener(View v, int position) {
        int newCount = mCityAdapter.select(position);
        mCallback.onCitySelected(newCount, mCityAdapter.getItem(position));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!TnmApplication.IsRefreshingToken) {
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

    @Override
    public String getLogTag() {
        return TAG;
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
                .query(citiesQuery)
                .cacheControl(CacheControl.NETWORK_FIRST);
        mCitiesCall.enqueue(dataCallback);
    }

    /**
     * Callback for the CitiesQuery call
     */
    private ApolloCall.Callback<CitiesQuery.Data> dataCallback = new ApolloCall.Callback<CitiesQuery.Data>() {
        @Override
        public void onResponse(final Response<CitiesQuery.Data> response) {
            if (!response.hasErrors()) {
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

    /**
     * Initializes the views
     */
    private void initViews(final View v, Bundle savedInstanceState) {
        // Initialization of adapter
        mCityAdapter = new CityAdapter(getContext());
        mCityAdapter.setListenerHack(this);

        if(savedInstanceState == null) {
            mCityAdapter.setItems(new ArrayList<CitiesQuery.Edge>());
        }

        // Initialization of RecyclerView
        mRecyclerView = (RecyclerView) v.findViewById(R.id.pick_cities_list);
        mSearchField = (EditText) v.findViewById(R.id.pick_cities_search_field);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mCityAdapter);

        mRecyclerView.setVisibility(View.GONE);
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
                mCityAdapter.getFilter().filter(mSearchField.getText().toString().trim());
            }
        });

        // Sets the maximum possible to pick cities
        LocalSupplier localSupplier = mRealm.where(LocalSupplier.class).findFirst();
        if (localSupplier != null) {
            mCityAdapter.setMax(localSupplier.getCityNum());
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
     * Restores saved cities from database
     */
    private void restoreSavedCities() {
        final RealmResults<PickedCity> selectedCities = mRealm.where(PickedCity.class).findAll();
        if (selectedCities.size() > 0) {
            HashMap<String, CitiesQuery.Node> selected = new HashMap<>();
            for (int i = 0; i < selectedCities.size(); i++) {
                PickedCity selectedCity = selectedCities.get(i);
                CitiesQuery.Node city = new CitiesQuery.Node("City", selectedCity.getId(),
                        selectedCity.getName(), State.valueOf(selectedCity.getState()));
                selected.put(selectedCity.getId(), city);
            }
            mCityAdapter.setSelected(selected);
            mCallback.onCitySelected(selected.size(), null);
        }
    }

    /**
     * Gets the adapter
     */
    public CityAdapter getAdapter() {
        return mCityAdapter;
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
        return "Cidade(s) de interesse";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Escolha abaixo a(s) cidade(s) da(s) qual(is) deseja receber informações";
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
        if (mCityAdapter != null) {
            return mCityAdapter.getSelectedCount() + "/" + getMaximum();
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

        List<String> cities = localSupplier.getCitiesIds();
        if (cities == null) {
            return true;
        }

        return cities.size() == 0;
    }

    @Override
    public void setError(String message) {}
}
