package com.tnmlicitacoes.app.main.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.TnmApplication;
import com.tnmlicitacoes.app.apollo.CancelSubscriptionMutation;
import com.tnmlicitacoes.app.apollo.RequestSupplierVerificationMutation;
import com.tnmlicitacoes.app.apollo.SupplierQuery;
import com.tnmlicitacoes.app.apollo.UpdateSupplierEmailMutation;
import com.tnmlicitacoes.app.changepicked.ChangePickedActivity;
import com.tnmlicitacoes.app.interfaces.AccountListener;
import com.tnmlicitacoes.app.interfaces.AuthStateListener;
import com.tnmlicitacoes.app.main.MainActivity;
import com.tnmlicitacoes.app.model.realm.LocalSupplier;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.subscription.PaymentsActivity;
import com.tnmlicitacoes.app.subscription.SubscriptionActivity;
import com.tnmlicitacoes.app.ui.adapter.AccountAdapter;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.ApiUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class AccountFragment extends BaseFragment implements AccountListener, AuthStateListener,
        MainActivity.MainContent {

    public static final String TAG = "AccountFragment";

    /** Account items adapter */
    private AccountAdapter mAdapter;

    /** Displays the account items */
    private RecyclerView mRecyclerView;

    /** Displays the progress bar */
    private ProgressBar mProgressBar;

    /** The progress dialog */
    private ProgressDialog mProgressDialog;

    /** The app singleton */
    private TnmApplication mApplication;

    /** Realm instance */
    private Realm mRealm;

    /** Picked cities */
    private RealmResults<PickedCity> mPickedCities;

    /** Picked segments */
    private RealmResults<PickedSegment> mPickedSegments;

    // TODO(diego): Temp hack, figure a better way to do this, maybe use startActivityForResult instead of startActivity
    public static boolean sShouldUpdate = false;

    /** Realm listener */
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object realmObject) {
            if (realmObject instanceof LocalSupplier) {
                mAdapter.setSupplier((LocalSupplier) realmObject);
            }
        }
    };

    /* Supplier query call */
    private ApolloQueryCall<SupplierQuery.Data> mSupplierCall;

    /** Cancel subscription mutation call */
    private ApolloMutationCall<CancelSubscriptionMutation.Data> mCancelSubscriptionCall;

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

    @Override
    public void onResume() {
        super.onResume();
        if (sShouldUpdate) {
            fetchSupplier();
        }
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

        mProgressBar.setVisibility(View.VISIBLE);
        SupplierQuery supplierQuery = SupplierQuery.builder()
                .build();

        mSupplierCall = mApplication.getApolloClient()
                .query(supplierQuery);
        mSupplierCall.enqueue(dataCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
        if (mSupplierCall != null) {
            mSupplierCall.cancel();
        }
        if (mCancelSubscriptionCall != null) {
            mCancelSubscriptionCall.cancel();
        }
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    private void initViews(View v) {
        mAdapter = new AccountAdapter(this);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), R.drawable.account_item_divider));

        mAdapter.setSupplier(mRealm.where(LocalSupplier.class).findFirst());
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
                                mProgressBar.setVisibility(View.GONE);
                                updateLocalDatabase(response.data().supplier());
                                mAdapter.notifyDataSetChanged();
                                sShouldUpdate = false;
                            }
                        });
                    }
                }
            } else {
                // TODO(diego): Handle this
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    };

    @Override
    public void onSubscribeClick(boolean isActive, boolean cancelAtPeriodEnd) {
        if (!isActive || cancelAtPeriodEnd) {
            Intent intent = new Intent(getActivity(), SubscriptionActivity.class);
            startActivity(intent);
        } else {

            mProgressDialog = AndroidUtilities.createProgressDialog(getContext(),
                    getString(R.string.cancel_subscription_progress_message), true, false);
            mProgressDialog.show();

            CancelSubscriptionMutation cancelSubscription = CancelSubscriptionMutation.builder()
                    .build();
            mCancelSubscriptionCall = mApplication.getApolloClient()
                    .mutate(cancelSubscription);
            mCancelSubscriptionCall.enqueue(new ApolloCall.Callback<CancelSubscriptionMutation.Data>() {
                @Override
                public void onResponse(@Nonnull Response<CancelSubscriptionMutation.Data> response) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;

                    if (!response.hasErrors() && response.data().cancelSubscription()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),
                                            R.string.cancel_subscription_success_message,
                                            Toast.LENGTH_SHORT).show();
                                    fetchSupplier();
                                }
                            });
                        }
                    } else {
                        // TODO(diego): Handle this
                    }
                }

                @Override
                public void onFailure(@Nonnull ApolloException e) {
                    // TODO(diego): Handle this
                }
            });
        }
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
                .mutate(mutation);

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
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://tnmlicitacoes.com/politica-de-privacidade.html"))
            );
        } else {
            // Toast.makeText(getActivity(), "Show licenses.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLogoutClick() {
        // Toast.makeText(getActivity(), "Logout button", Toast.LENGTH_SHORT).show();
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
                        .mutate(mutation);

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

        LOG_DEBUG(TAG, supplier.toString());

        LocalSupplier model = LocalSupplier.copyToRealmFromGraphQL(supplier);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(model);
        mRealm.commitTransaction();
    }

    @Override
    public int getMenuId() {
        return R.id.action_account;
    }

    @Override
    public String getTitle() {
        if (isAdded()) {
            return getString(R.string.title_activity_main_account);
        }

        return null;
    }

    @Override
    public boolean shouldDisplayElevationOnAppBar() {
        return true;
    }

    @Override
    public boolean shouldCollapseAppBarOnScroll() {
        return false;
    }

    @Override
    public void onAuthChanged() {
        // TODO(diego): See what to do here
    }
}
