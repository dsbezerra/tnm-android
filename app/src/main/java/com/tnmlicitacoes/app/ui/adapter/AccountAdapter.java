package com.tnmlicitacoes.app.ui.adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.apollo.SegmentsQuery;
import com.tnmlicitacoes.app.interfaces.AccountListener;
import com.tnmlicitacoes.app.model.realm.LocalSupplier;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.model.realm.PickedSegment;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.main.account.AccountFragment;
import com.tnmlicitacoes.app.ui.widget.SimpleDividerItemDecoration;
import com.tnmlicitacoes.app.verifynumber.InputNumberFragment;
import com.transitionseverywhere.TransitionManager;

import java.util.ArrayList;
import java.util.List;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

// TODO(diego): Replace these individual member variables with one single LocalSupplier object
public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {

    /* The logging tag */
    private static final String TAG = "AccountAdapter";

    /* View type count */
    private static int VIEW_TYPE_COUNT                      = 0;

    /* Subscription view type */
    private static final int VIEW_TYPE_SUBSCRIPTION         = VIEW_TYPE_COUNT++;

    /* E-mail view type */
    private static final int VIEW_TYPE_EMAIL                = VIEW_TYPE_COUNT++;

    /* Picked cities view type */
    private static final int VIEW_TYPE_PICKED_CITIES        = VIEW_TYPE_COUNT++;

    /* Picked segments view type */
    private static final int VIEW_TYPE_PICKED_SEGMENTS      = VIEW_TYPE_COUNT++;

    /* About view type */
    private static final int VIEW_TYPE_ABOUT                = VIEW_TYPE_COUNT++;

    /* Logout view type */
    // private static final int VIEW_TYPE_LOGOUT               = VIEW_TYPE_COUNT++;

    /* Max visible items in picked sections */
    private static final int MAX_VISIBLE_PICKED_ITEMS       = 3;

    /* Account state listener */
    private AccountListener mAccountListener;

    /* The supplier model */
    private LocalSupplier mLocalSupplier;

    /* Indicates whether the verification e-mail was sent or not */
    private boolean mVerificationEmailResent = false;

    /* Holds the three first picked cities */
    private List<PickedCity> mPickedCities = new ArrayList<>();

    /* Holds the three first picked segments */
    private List<PickedSegment> mPickedSegments = new ArrayList<>();

    private SegmentAdapter mSegmentAdapter;

    public AccountAdapter(Fragment fragment) {
        try {
            if(fragment instanceof AccountFragment) {
                mAccountListener = (AccountListener) fragment;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement AccountListener");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder VH;

        if (viewType == VIEW_TYPE_SUBSCRIPTION) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_account_subscription, parent, false);
            VH = new SubscriptionViewHolder(v);

        } else if (viewType == VIEW_TYPE_EMAIL) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_account_email, parent, false);
            VH = new EmailViewHolder(v);

        } else if (viewType == VIEW_TYPE_PICKED_CITIES) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_account_picked_cities, parent, false);
            VH = new PickedCitiesViewHolder(v);

        } else if (viewType == VIEW_TYPE_PICKED_SEGMENTS) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_account_picked_segments, parent, false);
            VH = new PickedSegmentsViewHolder(v);

        } else if (viewType == VIEW_TYPE_ABOUT) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_account_about, parent, false);
            VH = new AboutViewHolder(v);

        } else {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_account_logout, parent, false);
            VH = new LogoutViewHolder(v);
        }

        return VH;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mLocalSupplier == null) {
            return;
        }

        final Context context = holder.itemView.getContext();

        TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);

        if (holder instanceof SubscriptionViewHolder) {
            SubscriptionViewHolder subscriptionHolder = (SubscriptionViewHolder) holder;

            String subscribeText = getSubscribeButtonText(context);
            subscriptionHolder.mSubscribeButton.setText(subscribeText);

            String planName = null;
            int segNum = mLocalSupplier.getSegNum();
            int cityNum = mLocalSupplier.getCityNum();
            if (segNum == SubscriptionPlan.BASIC_MAX_QUANTITY && cityNum ==
                    SubscriptionPlan.BASIC_MAX_QUANTITY) {
                planName = context.getString(R.string.plan_basic_name);
            } else if (segNum == SubscriptionPlan.DEFAULT_MAX_QUANTITY &&
                    cityNum == SubscriptionPlan.DEFAULT_MAX_QUANTITY) {
                planName = context.getString(R.string.plan_default_name);
            } else {
                planName = context.getString(R.string.plan_custom_name);
            }
            subscriptionHolder.mSubscriptionName.setText(planName);
            subscriptionHolder.mPhoneNumber.setText(InputNumberFragment.formatPhone(mLocalSupplier.getPhone()));
        }
        else if (holder instanceof EmailViewHolder) {
            EmailViewHolder emailHolder = (EmailViewHolder) holder;

            String email = mLocalSupplier.getEmail();
            boolean isActivated = mLocalSupplier.isActivated();
            if (email == null) {
                emailHolder.mEmailText.setText(R.string.account_email_not_defined);
                emailHolder.mResendButton.setVisibility(View.GONE);
                emailHolder.mVerifiedText.setText(R.string.account_email_required_message);
                emailHolder.mVerifiedText.setTextColor(Color.argb(255, 255, 0, 0));
                emailHolder.mVerifiedIcon.setColorFilter(Color.argb(255, 255, 0, 0));
                emailHolder.mVerifiedIcon.setImageResource(R.drawable.ic_info_outline);
                emailHolder.mDefineEmail.setVisibility(View.VISIBLE);
            } else {
                emailHolder.mDefineEmail.setVisibility(View.GONE);
                emailHolder.mVerifiedText.setVisibility(View.VISIBLE);
                if (isActivated) {
                    emailHolder.mResendButton.setVisibility(View.GONE);
                    emailHolder.mVerifiedText.setText(R.string.account_email_verified);
                    emailHolder.mVerifiedText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    emailHolder.mVerifiedIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                    emailHolder.mVerifiedIcon.setImageResource(R.drawable.ic_check);
                } else if (mVerificationEmailResent){
                    emailHolder.mResendButton.setVisibility(View.GONE);
                    emailHolder.mResendEmailText.setVisibility(View.VISIBLE);
                    emailHolder.mVerifiedText.setVisibility(View.GONE);
                } else {
                    emailHolder.mVerifiedText.setText(R.string.account_email_not_verified);
                    emailHolder.mVerifiedText.setTextColor(Color.argb(255, 255, 0, 0));
                    emailHolder.mVerifiedIcon.setColorFilter(Color.argb(255, 255, 0, 0));
                    emailHolder.mVerifiedIcon.setImageResource(R.drawable.ic_info_outline);
                    emailHolder.mResendButton.setVisibility(View.VISIBLE);
                }
                emailHolder.mEmailText.setText(email);
            }

        } else if (holder instanceof PickedCitiesViewHolder) {
            PickedCitiesViewHolder pickedCitiesViewHolder = (PickedCitiesViewHolder) holder;
            String picked = "";

            // Add selected cities
            final int size = mPickedCities.size();
            if (size == 0) {
                picked = "Nenhuma cidade escolhida.";
            } else {
                for (int i = 0; i < mPickedCities.size(); i++) {
                    picked += mPickedCities.get(i).getName();
                    if (i == size - 1) {
                        break;
                    }

                    if (i + 1 == MAX_VISIBLE_PICKED_ITEMS) {
                        picked += ",...";
                        break;
                    }

                    if (i == size - 2) {
                        picked += " e ";
                        continue;
                    }

                    picked += ", ";
                }
            }

            pickedCitiesViewHolder.mTextPickedCities.setText(picked);

        } else if (holder instanceof PickedSegmentsViewHolder) {

            PickedSegmentsViewHolder pickedSegmentsViewHolder = (PickedSegmentsViewHolder) holder;
            if (mSegmentAdapter == null) {
                mSegmentAdapter = new SegmentAdapter(context);
                mSegmentAdapter.setIsHighlight(true);
            }

            int size = Math.min(MAX_VISIBLE_PICKED_ITEMS, mPickedSegments.size());
            List<SegmentsQuery.Edge> segments = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                PickedSegment segment = mPickedSegments.get(i);
                SegmentsQuery.Node node = new SegmentsQuery.Node("Segment", segment.getId(),
                        segment.getName(), segment.getIcon(), segment.getDefaultImg(),
                        segment.getMqdefault(), segment.getHqdefault());
                segments.add(new SegmentsQuery.Edge("SegmentEdge", node, null));
            }
            mSegmentAdapter.setItems(segments);
            pickedSegmentsViewHolder.mPickedSegments.setAdapter(mSegmentAdapter);

        } else if (holder instanceof AboutViewHolder) {
            AboutViewHolder aboutViewHolder = (AboutViewHolder) holder;
            aboutViewHolder.mVersion.setText(BuildConfig.VERSION_NAME);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_SUBSCRIPTION;
        } else if (position == 1) {
            return VIEW_TYPE_EMAIL;
        } else if (position == 2) {
            return VIEW_TYPE_PICKED_CITIES;
        } else if (position == 3) {
            return VIEW_TYPE_PICKED_SEGMENTS;
        } else /* if (position == 4) */ {
            return VIEW_TYPE_ABOUT;
        }
        /* else {
            return VIEW_TYPE_LOGOUT;
        } */
    }

    @Override
    public int getItemCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public void onClick(View v) {
        if (mAccountListener == null) {
            return;
        }

        final int id = v.getId();
        switch (id) {
            // Subscription
            case R.id.subscribeBtn:
                mAccountListener.onSubscribeClick(
                        mLocalSupplier.isActiveSubscription(),
                        mLocalSupplier.isCancelAtPeriodEnd()
                );
                break;

            // E-mail
            case R.id.defineEmail:
                mAccountListener.onDefineEmailClick();
                break;
            case R.id.resendButton:
                mAccountListener.onResendEmailClick();
                break;

            // Picked cities
            case R.id.changeCitiesBtn:
                mAccountListener.onChangePickedCitiesClick();
                break;

            // Picked segments
            case R.id.pickedSegments:
            case R.id.changeSegmentsBtn:
                mAccountListener.onChangePickedSegmentsClick();
                break;

            // About section items
            case R.id.terms:
                mAccountListener.onAboutItemClick(id);
                break;
            // case R.id.openSource:

            // Logout
            case R.id.logoutBtn:
                mAccountListener.onLogoutClick();
                break;

            default:
                // Empty
                LOG_DEBUG(TAG, id + "");
                break;
        }
    }

    /**
     * Sets the new value for the verification email resent flag
     * @param value true or false value
     */
    public void setVerificationEmailResent(boolean value) {
        this.mVerificationEmailResent = value;
        notifyItemChanged(VIEW_TYPE_EMAIL);
    }

    public void setSupplier(LocalSupplier localSupplier) {
        this.mLocalSupplier = localSupplier;
        notifyDataSetChanged();
    }

    public void setPicked(ArrayList<PickedCity> cities, ArrayList<PickedSegment> segments) {
        this.mPickedCities = cities;
        this.mPickedSegments = segments;
        notifyItemChanged(VIEW_TYPE_PICKED_CITIES);
        notifyItemChanged(VIEW_TYPE_PICKED_SEGMENTS);
    }

    public void setPickedCities(ArrayList<PickedCity> cities) {
        this.mPickedCities = cities;
        notifyItemChanged(VIEW_TYPE_PICKED_CITIES);
    }

    public void setPickedSegments(ArrayList<PickedSegment> segments) {
        this.mPickedSegments = segments;
        notifyItemChanged(VIEW_TYPE_PICKED_SEGMENTS);
    }

    /**
     * Gets the text for the subscribe button
     */
    private String getSubscribeButtonText(Context context) {
        if (mLocalSupplier.isCancelAtPeriodEnd()) {
            return context.getString(R.string.reactive_subscription_btn);
        } else if (mLocalSupplier.isActiveSubscription()) {
            return context.getString(R.string.cancel_subscription_btn);
        } else {
            return context.getString(R.string.subscribe_btn);
        }
    }

    private class SubscriptionViewHolder extends RecyclerView.ViewHolder {

        private TextView mPhoneNumber;
        private TextView mSubscriptionName;
        private Button mSubscribeButton;
        private ListView mListView;

        public SubscriptionViewHolder(View itemView) {
            super(itemView);
            mPhoneNumber = (TextView) itemView.findViewById(R.id.phoneText);
            mSubscriptionName = (TextView) itemView.findViewById(R.id.subscriptionName);
            mSubscribeButton = (Button) itemView.findViewById(R.id.subscribeBtn);
            mListView = (ListView) itemView.findViewById(R.id.subscriptionList);
            mSubscribeButton.setOnClickListener(AccountAdapter.this);


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(itemView.getContext(),
                    android.R.layout.simple_list_item_1);
            adapter.add("Pagamento");
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        mAccountListener.onPaymentClick();
                    }
                }
            });
        }
    }

    private class EmailViewHolder extends RecyclerView.ViewHolder {

        private TextView mEmailText;
        private TextView mVerifiedText;
        private TextView mResendEmailText;
        private ImageView mVerifiedIcon;
        private Button mDefineEmail;
        private Button mResendButton;

        public EmailViewHolder(View itemView) {
            super(itemView);
            mEmailText = (TextView) itemView.findViewById(R.id.emailText);
            mVerifiedText = (TextView) itemView.findViewById(R.id.verifiedText);
            mResendEmailText = (TextView) itemView.findViewById(R.id.resendText);
            mVerifiedIcon = (ImageView) itemView.findViewById(R.id.verifiedIcon);
            mResendButton = (Button) itemView.findViewById(R.id.resendButton);
            mDefineEmail = (Button) itemView.findViewById(R.id.defineEmail);
            mResendButton.setOnClickListener(AccountAdapter.this);
            mDefineEmail.setOnClickListener(AccountAdapter.this);
        }
    }

    /**
     * The picked cities row view holder
     */
    private class PickedCitiesViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextPickedCities;
        private Button mChangeButton;

        public PickedCitiesViewHolder(View itemView) {
            super(itemView);
            mTextPickedCities = (TextView) itemView.findViewById(R.id.pickedCities);
            mChangeButton = (Button) itemView.findViewById(R.id.changeCitiesBtn);
            mChangeButton.setOnClickListener(AccountAdapter.this);
        }
    }

    /**
     * The picked segments row view holder
     */
    private class PickedSegmentsViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView mPickedSegments;
        private Button mChangeButton;

        public PickedSegmentsViewHolder(View itemView) {
            super(itemView);
            mPickedSegments = (RecyclerView) itemView.findViewById(R.id.pickedSegments);
            mChangeButton = (Button) itemView.findViewById(R.id.changeSegmentsBtn);
            mChangeButton.setOnClickListener(AccountAdapter.this);
            mPickedSegments.setLayoutManager(new LinearLayoutManager(itemView.getContext(),
                    LinearLayoutManager.VERTICAL, false));
            mPickedSegments.addItemDecoration(new SimpleDividerItemDecoration(itemView.getContext(),
                    R.drawable.segment_item_divider));
            mPickedSegments.setHasFixedSize(true);
        }
    }

    /**
     * The about section row view holder
     */
    private class AboutViewHolder extends RecyclerView.ViewHolder {

        private TextView mVersion;
        // private LinearLayout mOpenSourceLicenses;
        private LinearLayout mTermsAndPolicies;

        public AboutViewHolder(View itemView) {
            super(itemView);
            mVersion = (TextView) itemView.findViewById(R.id.version);
            // mOpenSourceLicenses = (LinearLayout) itemView.findViewById(R.id.openSource);
            mTermsAndPolicies = (LinearLayout) itemView.findViewById(R.id.terms);

            // mOpenSourceLicenses.setClickable(true);
            mTermsAndPolicies.setClickable(true);

            // mOpenSourceLicenses.setOnClickListener(AccountAdapter.this);
            mTermsAndPolicies.setOnClickListener(AccountAdapter.this);
        }
    }

    /**
     * Logout row view holder
     */
    private class LogoutViewHolder extends RecyclerView.ViewHolder {

        private Button mLogoutButton;

        public LogoutViewHolder(View itemView) {
            super(itemView);
            mLogoutButton = (Button) itemView.findViewById(R.id.logoutBtn);
            mLogoutButton.setOnClickListener(AccountAdapter.this);
        }
    }
}
