package com.tnmlicitacoes.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.RequestSupplierVerificationMutation;
import com.tnmlicitacoes.app.SupplierQuery;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.UpdateSupplierEmailMutation;
import com.tnmlicitacoes.app.interfaces.AccountStateListener;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.model.realm.Supplier;
import com.tnmlicitacoes.app.ui.adapter.AccountAdapter;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.ui.changepicked.ChangePickedActivity;
import com.tnmlicitacoes.app.ui.subscription.PaymentsActivity;
import com.tnmlicitacoes.app.ui.subscription.SubscriptionActivity;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;
import com.tnmlicitacoes.app.utils.ApiUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.verifynumber.InputNumberFragment;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class AccountFragment extends BaseFragment implements AccountStateListener {

    public static final String TAG = "AccountFragment";

    /* Account items adapter */
    private AccountAdapter mAdapter;

    /* Displays the account items */
    private RecyclerView mRecyclerView;

    /* The app singleton */
    private TnmApplication mApplication;

    /* Realm instance */
    private Realm mRealm;

    /* Picked cities */
    private RealmResults<PickedCity> mPickedCities;

    /* Picked segments */
    private RealmResults<PickedSegment> mPickedSegments;

    /* Realm listener */
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object realmObject) {
            if (realmObject instanceof Supplier) {
                mAdapter.setSupplier((Supplier) realmObject);
            }
        }
    };

    /* Supplier query call */
    private ApolloQueryCall<SupplierQuery.Data> mSupplierCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);
        mApplication = (TnmApplication) getActivity().getApplication();
        View v = inflater.inflate(R.layout.fragment_account, container, false);
        initViews(v);
        loadPicked();
        fetchSupplier();
        return v;
    }

    private void loadPicked() {
        mPickedCities = mRealm.where(PickedCity.class).findAll().sort("name");
        mPickedSegments = mRealm.where(PickedSegment.class).findAll().sort("name");
        mPickedCities.addChangeListener(new RealmChangeListener<RealmResults<PickedCity>>() {
            @Override
            public void onChange(RealmResults<PickedCity> pickedCities) {
                mAdapter.setPickedCities(new ArrayList<>(pickedCities));
            }
        });
        mPickedSegments.addChangeListener(new RealmChangeListener<RealmResults<PickedSegment>>() {
            @Override
            public void onChange(RealmResults<PickedSegment> segments) {
                mAdapter.setPickedSegments(new ArrayList<>(segments));
            }
        });
        mAdapter.setPicked(new ArrayList<>(mPickedCities), new ArrayList<>(mPickedSegments));
    }

    /**
     * Fetch supplier info
     */
    private void fetchSupplier() {
        SupplierQuery supplierQuery = SupplierQuery.builder()
                .build();

        mSupplierCall = mApplication.getApolloClient()
                .query(supplierQuery)
                .cacheControl(CacheControl.CACHE_FIRST);
        mSupplierCall.enqueue(dataCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
        if (mSupplierCall != null) {
            mSupplierCall.cancel();
        }
    }

    private void initViews(View v) {
        mAdapter = new AccountAdapter(this);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), R.drawable.account_item_divider));

        mAdapter.setSupplier(mRealm.where(Supplier.class).findFirst());
    }

    /* The SupplierQuery API call callback */
    private ApolloCall.Callback<SupplierQuery.Data> dataCallback = new ApolloCall.Callback<SupplierQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<SupplierQuery.Data> response) {
            if (!response.hasErrors()) {
                if (response.data() != null && response.data().supplier() != null) {

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLocalDatabase(response.data().supplier());
                            }
                        });
                    }
                }

            } else {

            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {

        }
    };

    @Override
    public void onSubscribeClick() {
        Intent intent = new Intent(getActivity(), SubscriptionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPaymentClick() {
        startActivity(new Intent(getActivity(), PaymentsActivity.class));
    }

    @Override
    public void onResendEmailClick() {

        RequestSupplierVerificationMutation mutation = RequestSupplierVerificationMutation.builder()
                .email(SettingsUtils.getUserDefaultEmail(getActivity()))
                .build();

        ApolloCall<RequestSupplierVerificationMutation.Data> call = mApplication.getApolloClient()
                .mutate(mutation)
                .cacheControl(CacheControl.NETWORK_ONLY);

        call.enqueue(new ApolloCall.Callback<RequestSupplierVerificationMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<RequestSupplierVerificationMutation.Data> response) {
                if (!response.hasErrors()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.setVerificationEmailResent(true);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ApiUtils.ApiError error = ApiUtils.getFirstValidError(getActivity(),
                                    response.errors());

                            if (error != null) {
                                Toast.makeText(getActivity(), error.isFromResources() ?
                                getString(error.getMessageRes()) : error.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                LOG_DEBUG(TAG, e.getMessage());
            }
        });
    }

    @Override
    public void onDefineEmailClick() {
        showInputEmailDialog();
    }

    @Override
    public void onChangePickedCitiesClick() {
        Intent intent = new Intent(getActivity(), ChangePickedActivity.class);
        intent.putExtra(ChangePickedActivity.VIEW, ChangePickedActivity.CITIES);
        startActivity(intent);
    }

    @Override
    public void onChangePickedSegmentsClick() {
        Intent intent = new Intent(getActivity(), ChangePickedActivity.class);
        intent.putExtra(ChangePickedActivity.VIEW, ChangePickedActivity.SEGMENTS);
        startActivity(intent);
    }

    @Override
    public void onAboutItemClick(int itemId) {
        if (itemId == R.id.terms) {
            Toast.makeText(getActivity(), "Show terms.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Show licenses.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLogoutClick() {
        Toast.makeText(getActivity(), "Logout button", Toast.LENGTH_SHORT).show();
    }

    private void showInputEmailDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(InputEmailDialog.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new InputEmailDialog()
                .withCallback(new InputEmailDialog.InputEmailCallback() {
            @Override
            public void onConfirm(final String typedEmail) {
                UpdateSupplierEmailMutation mutation = UpdateSupplierEmailMutation.builder()
                        .email(typedEmail)
                        .build();

                ApolloCall<UpdateSupplierEmailMutation.Data> call = mApplication.getApolloClient()
                        .mutate(mutation)
                        .cacheControl(CacheControl.NETWORK_ONLY);

                call.enqueue(new ApolloCall.Callback<UpdateSupplierEmailMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<UpdateSupplierEmailMutation.Data> response) {
                        if (!response.hasErrors()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //mAdapter.setEmail(typedEmail, false);
                                    mAdapter.setVerificationEmailResent(true);
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ApiUtils.ApiError error = ApiUtils.getFirstValidError(getActivity(),
                                            response.errors());

                                    if (error != null) {
                                        Toast.makeText(getActivity(), error.isFromResources() ?
                                                        getString(error.getMessageRes()) : error.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        LOG_DEBUG(TAG, e.getMessage());
                    }
                });
            }
        });
        newFragment.show(ft, InputEmailDialog.TAG);
    }

    private void updateLocalDatabase(SupplierQuery.Supplier supplier) {

        if (supplier == null) {
            return;
        }

        Supplier model = Supplier.copyToRealmFromGraphQL(supplier);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(model);
        mRealm.commitTransaction();
    }
}
