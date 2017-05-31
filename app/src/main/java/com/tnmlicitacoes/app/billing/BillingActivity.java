package com.tnmlicitacoes.app.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.model.Subscription;
import com.tnmlicitacoes.app.ui.activity.AccountConfigurationActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.BillingUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;
import com.tnmlicitacoes.app.verifynumber.VerifyNumberActivity;

import java.util.ArrayList;
import java.util.List;

import static com.tnmlicitacoes.app.utils.BillingUtils.SKU_SUBSCRIPTION_BASIC;
import static com.tnmlicitacoes.app.utils.BillingUtils.SKU_SUBSCRIPTION_DEFAULT;
import static com.tnmlicitacoes.app.utils.BillingUtils.SKU_SUBSCRIPTION_LEGACY;
import static com.tnmlicitacoes.app.utils.BillingUtils.SKU_SUBSCRIPTION_PREMIUM;
import static com.tnmlicitacoes.app.utils.BillingUtils.SUBSCRIPTION_MAX_ITEMS_BASIC;
import static com.tnmlicitacoes.app.utils.BillingUtils.SUBSCRIPTION_MAX_ITEMS_DEFAULT;
import static com.tnmlicitacoes.app.utils.BillingUtils.getSubscription;
import static com.tnmlicitacoes.app.utils.BillingUtils.getSubscriptions;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class BillingActivity extends AppCompatActivity {

    private static final String TAG = "BillingActivity";

    private static final int RC_REQUEST = 40584;

    private boolean mSubscribedToPremiumFeatures = false;

    private boolean mIsChangingSubscription = false;

    private String mPurchasedSku = "";

    private List<String> mOldSkus;

    private int mPreviousState;

    private int mCurrentView = 0;

    private int mDy;

    private IabHelper mBillingHelper;

    private SubscriptionAdapter mSubscriptionAdapter;

    private Activity mActivity;

    private ProgressBar mBillingProgressBar;

    private RecyclerView mBillingRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        setupToolbar();

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mIsChangingSubscription = b.getBoolean("IS_CHANGING_SUBSCRIPTION");
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(mIsChangingSubscription);
                actionBar.setDisplayShowHomeEnabled(mIsChangingSubscription);
            }
        }

        mActivity = this;

        mSubscriptionAdapter = new SubscriptionAdapter();

        if(BuildConfig.DEBUG)
            LOG_DEBUG(TAG, "Setting subscription items");
        mSubscriptionAdapter.setItems(getSubscriptions(getApplicationContext()));

        setupViews();
        setupViewsListeners();

        setupInAppBilling();
    }

    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBillingHelper != null) {
            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Disposing IAB Helper...");
            mBillingHelper.dispose();
            mBillingHelper = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_billing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_contact) {

            Intent i = Utils.sendContactEmail("[Assinatura]", "");

            try {
                startActivity(Intent.createChooser(i, getString(R.string.send_email_contact)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, getString(R.string.no_email_clients_installed), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        if(BuildConfig.DEBUG)
            LOG_DEBUG(TAG, "Setting up views");
        mBillingProgressBar = (ProgressBar) findViewById(R.id.billingProgressBar);
        mBillingRecyclerView = (RecyclerView) findViewById(R.id.billingRecyclerView);

        mBillingProgressBar.setIndeterminate(true);

        mBillingRecyclerView.setHasFixedSize(true);
        mBillingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBillingRecyclerView.setAdapter(mSubscriptionAdapter);

        setWaitMode(true);

        if(BuildConfig.DEBUG)
            LOG_DEBUG(TAG, "Finished setting up views");
    }

    private void setupViewsListeners() {
        mBillingRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // Save the vertical scroll amount to determine direction
                mDy = dy;
                mPreviousState = recyclerView.getScrollState();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                int firstVisibleItem = llm.findFirstVisibleItemPosition();
                int lastVisibleItem = llm.findLastVisibleItemPosition();

                if (firstVisibleItem == 0 || lastVisibleItem == 4) {
                    return;
                }

                // TODO(diego): Remove when unlimited plan becomes available
                if(firstVisibleItem == 3 || lastVisibleItem == 3) {
                    return;
                }

                // If scroll is going up, then snap to near first visible item
                if (mDy < 0 && newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    if (firstVisibleItem == mCurrentView)
                        return;

                    mCurrentView = firstVisibleItem;
                    recyclerView.smoothScrollToPosition(mCurrentView);

                    // If scroll is going down, then snap to near last visible item
                } else if (mDy > 0 && newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    if (lastVisibleItem == mCurrentView) {
                        return;
                    }
                    mCurrentView = lastVisibleItem;
                    recyclerView.smoothScrollToPosition(mCurrentView);

                    // If the scroll wasn't a fling
                } else if (mPreviousState == RecyclerView.SCROLL_STATE_DRAGGING && newState == RecyclerView.SCROLL_STATE_IDLE) {

                    // Determine direction of scroll by visible views
                    // If first view is not visible then the scroll was down
                    if (llm.findFirstCompletelyVisibleItemPosition() == RecyclerView.NO_POSITION && firstVisibleItem == mCurrentView) {
                        if (mCurrentView == lastVisibleItem) {
                            return;
                        }
                        mCurrentView = llm.findLastVisibleItemPosition();
                        recyclerView.smoothScrollToPosition(mCurrentView);

                        // If last view is not visible then the scroll was up
                    } else if (llm.findLastCompletelyVisibleItemPosition() == RecyclerView.NO_POSITION && lastVisibleItem == mCurrentView) {
                        if (mCurrentView == firstVisibleItem) {
                            return;
                        }

                        mCurrentView = llm.findFirstVisibleItemPosition();
                        recyclerView.smoothScrollToPosition(mCurrentView);
                    }
                }
            }
        });
    }



    private void setupInAppBilling() {

        if(mBillingHelper == null) {

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Creating IAB helper.");
            mBillingHelper = new IabHelper(BillingActivity.this, Utils.decode(Utils.BASE64_PIECES));

            mBillingHelper.enableDebugLogging(false);

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Starting setup.");
            try {
                mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    @Override
                    public void onIabSetupFinished(IabResult result) {
                        if(!result.isSuccess()) {
                            if(BuildConfig.DEBUG)
                                LOG_DEBUG(TAG, "Problem setting up in-app billing: " + result);
                            return;
                        }

                        if (mBillingHelper == null) return;

                        if(BuildConfig.DEBUG)
                            LOG_DEBUG(TAG, "Setup successful. Querying inventory.");
                        mBillingHelper.queryInventoryAsync(mGotInventoryListener);
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Query inventory finished.");

            if (mBillingHelper == null) return;

            if (result.isFailure()) {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "Failed to query inventory: " + result);
                return;
            }

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium plan?
            Purchase currentSubscription = null;
            Purchase legacySubPurchase = inventory.getPurchase(SKU_SUBSCRIPTION_LEGACY);
            Purchase basicSubPurchase = inventory.getPurchase(SKU_SUBSCRIPTION_BASIC);
            Purchase defaultSubPurchase = inventory.getPurchase(SKU_SUBSCRIPTION_DEFAULT);
            Purchase unlimitedSubPurchase = inventory.getPurchase(SKU_SUBSCRIPTION_PREMIUM);


            mOldSkus = new ArrayList<>();

            if(legacySubPurchase != null) {
                currentSubscription = legacySubPurchase;
                mOldSkus.add(legacySubPurchase.getSku());
            }

            if(basicSubPurchase != null) {
                currentSubscription = basicSubPurchase;
                mOldSkus.add(basicSubPurchase.getSku());
            }

            if (defaultSubPurchase != null) {
                currentSubscription = defaultSubPurchase;
                mOldSkus.add(defaultSubPurchase.getSku());
            }

            if (unlimitedSubPurchase != null) {
                currentSubscription = unlimitedSubPurchase;
                mOldSkus.add(unlimitedSubPurchase.getSku());
            }

            if(currentSubscription == null) {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "User does not have Premium subscriptions");
                    SettingsUtils.putString(BillingActivity.this, SettingsUtils.PREF_BILLING_STATE, "");

            } else if(Utils.verifyDeveloperPayload(currentSubscription)){
                mSubscribedToPremiumFeatures = true;
                mPurchasedSku = currentSubscription.getSku();
            }

            if(mSubscribedToPremiumFeatures) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BillingActivity.this);
                preferences.edit().remove(SettingsUtils.PREF_IS_TRIAL_EXPIRED).apply();
            }

            // If user is premium
            if(mSubscribedToPremiumFeatures && !mIsChangingSubscription) {
                SettingsUtils.putString(BillingActivity.this, SettingsUtils.PREF_BILLING_STATE, mPurchasedSku);
                Subscription subscription = getSubscription(getApplicationContext(), mPurchasedSku);
                if(subscription != null) {
                    SettingsUtils.putString(BillingActivity.this,
                            SettingsUtils.PREF_BILLING_SUB_NAME,
                            subscription.getName());
                    BillingUtils.SUBSCRIPTION_MAX_ITEMS = subscription.getQuantity();
                }
                goToInitialConfig();
            }
            // If user is not premium but is in trial phase
            else if(BillingUtils.sIsTrialActive && !mIsChangingSubscription) {
                SettingsUtils.putString(BillingActivity.this,
                        SettingsUtils.PREF_BILLING_STATE,
                        BillingUtils.SKU_SUBSCRIPTION_TRIAL);
                SettingsUtils.putString(BillingActivity.this,
                        SettingsUtils.PREF_BILLING_SUB_NAME,
                        "Trial");
                BillingUtils.SUBSCRIPTION_MAX_ITEMS = SUBSCRIPTION_MAX_ITEMS_BASIC;
                goToInitialConfig();
            }
            else {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "Initial inventory query finished; enabling main UI.");

                setWaitMode(false);
            }
        }
    };

    // Callback for when a purchase is finished
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (mBillingHelper == null) return;

            if (result.isFailure()) {
                if (result.getResponse() != IabHelper.IABHELPER_USER_CANCELLED) {
                    if(BuildConfig.DEBUG)
                        LOG_DEBUG(TAG, "User cancelled: " + result);
                }
                return;
            }

            if (!Utils.verifyDeveloperPayload(purchase)) {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "Error purchasing. Authenticity verification failed.");
                return;
            }

            LOG_DEBUG(TAG, "Purchase successful.");

            String purchasedSku = purchase.getSku();
            if (mPurchasedSku.equals(SKU_SUBSCRIPTION_BASIC)) {
                LOG_DEBUG(TAG, "Basic premium subscription purchased.");
                // Add analytics
            }
            else if (mPurchasedSku.equals(SKU_SUBSCRIPTION_DEFAULT)) {
                LOG_DEBUG(TAG, "Default premium subscription purchased.");
                // Add analytics
            }
            else if (mPurchasedSku.equals(SKU_SUBSCRIPTION_PREMIUM)) {
                LOG_DEBUG(TAG, "Premium subscription purchased.");
                // Add analytics
            }

            Subscription subscription = getSubscription(BillingActivity.this, purchasedSku);
            if(subscription != null) {
                SettingsUtils.putString(BillingActivity.this, SettingsUtils.PREF_BILLING_SUB_NAME, subscription.getName());
                SettingsUtils.putString(BillingActivity.this, SettingsUtils.PREF_BILLING_STATE, purchasedSku);
                BillingUtils.SUBSCRIPTION_MAX_ITEMS = subscription.getQuantity();
            }

            if(!mIsChangingSubscription) {
                goToInitialConfig();
            } else {
                finish();
            }
        }
    };

    private void goToVerifyNumber() {
        startActivity(AndroidUtilities
                .getInstance(getApplicationContext())
                .createClearStackIntent(VerifyNumberActivity.class)
        );
        finish();
    }

    private void goToInitialConfig() {
        startActivity(AndroidUtilities
                .getInstance(getApplicationContext())
                .createClearStackIntent(AccountConfigurationActivity.class)
        );
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(BuildConfig.DEBUG)
            LOG_DEBUG(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mBillingHelper == null)
            return;

        // Pass on the activity result to the helper for handling
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    private void setWaitMode(boolean isWaiting) {
        mBillingRecyclerView.setVisibility(isWaiting ? View.GONE : View.VISIBLE);
        mBillingProgressBar.setVisibility(isWaiting ? View.VISIBLE : View.GONE);
    }

    public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Subscription> mItems = new ArrayList<>();

        private Context mContext = getApplicationContext();

        private static final int ITEM_INFO = 0;

        private static final int ITEM_SUBSCRIPTION = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            RecyclerView.ViewHolder viewHolder;

            if(viewType == ITEM_INFO) {
                View v = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_billing_info, parent, false);
                viewHolder = new InfoTextViewHolder(v);

            } else {
                View v = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_subscription, parent, false);
                viewHolder = new VH(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof VH) {

                VH itemViewHolder = (VH) holder;

                final Subscription subscription = mItems.get(position - 1);
                int quantity = subscription.getQuantity();

                boolean needDescription = true;
                if(quantity == SUBSCRIPTION_MAX_ITEMS_BASIC) {
                    itemViewHolder.itemsText.setText(quantity + "");
                } else if(quantity == SUBSCRIPTION_MAX_ITEMS_DEFAULT) {
                    itemViewHolder.itemsText.setText(mContext.getString(R.string.sub_default_quantity, quantity));
                } else if (quantity == BillingUtils.SUBSCRIPTION_MAX_ITEMS_PREMIUM) {
                    needDescription = false;
                    itemViewHolder.itemsText.setText(mContext.getString(R.string.sub_premium_description));
                    itemViewHolder.itemsText.setTypeface(null, Typeface.BOLD);
                }

                itemViewHolder.subscritionName.setText(subscription.getName());
                itemViewHolder.priceText.setText(subscription.getPrice() + "");
                if(needDescription) {
                    itemViewHolder.descriptionText.setVisibility(View.VISIBLE);
                    itemViewHolder.descriptionText.setText(subscription.getDescription());
                } else {
                    itemViewHolder.descriptionText.setVisibility(View.GONE);
                }

                int[] features = subscription.getFeatures();
                itemViewHolder.featuresLayout.removeAllViews();
                for(int i = 0; i < features.length; i++) {
                    TextView textView = new TextView(mContext);
                    textView.setPadding(10, 10, 10, 10);
                    textView.setText(mContext.getString(features[i]));
                    textView.setGravity(Gravity.LEFT);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
                    textView.setTextColor(0xff565656);
                    textView.setCompoundDrawablePadding(AndroidUtilities.dp(mActivity, 4.0f));
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_feature_check, 0, 0, 0);
                    itemViewHolder.featuresLayout.addView(textView);
                }


                if(mPurchasedSku.equals(subscription.getSku())) {
                    itemViewHolder.subButton.setEnabled(false);
                    itemViewHolder.subButton.setTextColor(mContext.getResources().getColor(R.color.colorGrey));
                    itemViewHolder.subButton.setText("Assinado");
                } else {
                    itemViewHolder.subButton.setEnabled(true);
                    itemViewHolder.subButton.setTextColor(mContext.getResources().getColor(android.R.color.white));
                    itemViewHolder.subButton.setText("Assinar");
                }


                itemViewHolder.subButton.setEnabled(!mPurchasedSku.equals(subscription.getSku()));

                if(position == 3) {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    colorMatrix.setSaturation(0.0f);

                    Paint paint = new Paint();
                    ColorMatrixColorFilter cmColorFilter = new ColorMatrixColorFilter(colorMatrix);
                    paint.setColorFilter(cmColorFilter);
                    paint.setAlpha(100);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        itemViewHolder.itemView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
                        itemViewHolder.subButton.setText("Em breve");
                        itemViewHolder.subButton.setEnabled(false);
                        itemViewHolder.subButton.setClickable(false);
                    } else {
                        itemViewHolder.subButton.setText("Em breve");
                        itemViewHolder.subButton.setEnabled(false);
                        itemViewHolder.subButton.setClickable(false);
                    }
                }

                itemViewHolder.subButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String sku = subscription.getSku();

                        if (!mBillingHelper.subscriptionsSupported()) {
                            // Add analytics
                            return;
                        }

                        // Add analytics

                        // TODO(diego): Generate a valid payload that works for user in any device
                        String payload = "";

                        if(mOldSkus.size() == 0) {
                            mBillingHelper.launchSubscriptionPurchaseFlow(mActivity,
                                    sku, RC_REQUEST,
                                    mPurchaseFinishedListener
                            );
                        }
                        else {
                            mBillingHelper.launchPurchaseFlow(mActivity,
                                    sku, IabHelper.ITEM_TYPE_SUBS,
                                    mOldSkus, RC_REQUEST, mPurchaseFinishedListener, payload
                            );
                        }
                    }
                });

            } else if (holder instanceof InfoTextViewHolder) {

                InfoTextViewHolder itemViewHolder = (InfoTextViewHolder) holder;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemViewHolder.itemView.setElevation(AndroidUtilities.dp(mActivity, 6.0f));
                } else {
                    itemViewHolder.itemView.setBackgroundResource(R.drawable.stroke_rectangle);
                }

                if(position == 0) {
                    itemViewHolder.textView.setText(Html.fromHtml(getString(R.string.sub_top_info_text).trim()));
                } else if (position == 4) {
                    itemViewHolder.textView.setText(Html.fromHtml(getString(R.string.sub_bottom_info_text).trim()));
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0 || position == 4) {
                return ITEM_INFO;
            } else {
                return ITEM_SUBSCRIPTION;
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size() + 2;
        }

        public void setItems(List<Subscription> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        public class VH extends RecyclerView.ViewHolder {

            public LinearLayout featuresLayout;
            public TextView itemsText;
            public TextView subscritionName;
            public TextView priceText;
            public TextView descriptionText;
            public Button subButton;

            public VH(View itemView) {
                super(itemView);
                featuresLayout = (LinearLayout) itemView.findViewById(R.id.featuresLayout);
                itemsText = (TextView) itemView.findViewById(R.id.itemsText);
                subscritionName = (TextView) itemView.findViewById(R.id.subscriptionName);
                priceText = (TextView) itemView.findViewById(R.id.priceText);
                descriptionText = (TextView) itemView.findViewById(R.id.descriptionText);
                subButton = (Button) itemView.findViewById(R.id.subButton);
            }
        }

        public class InfoTextViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public InfoTextViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.infoBillingText);
            }
        }
    }
}
