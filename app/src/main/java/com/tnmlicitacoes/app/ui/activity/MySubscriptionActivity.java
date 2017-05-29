package com.tnmlicitacoes.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.billing.BillingActivity;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

public class MySubscriptionActivity extends BaseBottomNavigationActivity implements View.OnClickListener {

    private LinearLayout mContentLayout;

    private Button mGoToStore;

    private LayoutInflater mLayoutInflater;

    private View mItemMySubscription;

    private interface Buttons {
        int SUBSCRIPTION = 0;
        int CITIES       = 1;
        int CATEGORIES   = 2;
        int STORE        = 3;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // We need  to put this before the super because the super gets a reference
        // from the xml layout
        setContentView(R.layout.activity_my_subscription);
        super.onCreate(savedInstanceState);
        setupToolbar("Meu plano");
        initViews();
        initViewListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        populateViews();
    }

    @Override
    protected void setupToolbar(String title) {
        super.setupToolbar(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mContentLayout = (LinearLayout) findViewById(R.id.contentLinearLayout);
        mGoToStore = (Button) findViewById(R.id.btnAppStore);

        mLayoutInflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void initViewListeners() {
        mGoToStore.setTag(Buttons.STORE);
        mGoToStore.setOnClickListener(this);
    }

    private void populateViews() {

        if(mContentLayout.getChildCount() != 0) {
            mContentLayout.removeAllViews();
        }

        TextView itemHeader;
        TextView itemName;
        Button changeButton;

        // My Plan View
        mItemMySubscription = inflateItemView();
        itemHeader = (TextView) mItemMySubscription.findViewById(R.id.itemHeader);
        itemName = (TextView) mItemMySubscription.findViewById(R.id.itemName);
        changeButton = (Button) mItemMySubscription.findViewById(R.id.changeButton);

        itemHeader.setText(getString(R.string.current_subscription_text));

        String billingName = SettingsUtils.getBillingSubName(this);
        if(billingName == null || billingName.isEmpty() || billingName.equals(SettingsUtils.STRING_DEFAULT)) {
            //long days = 30 - (System.currentTimeMillis() - SettingsUtils.getActivationDateFromPrefs(this)) / Utils.DAY_IN_MILLIS;
            itemName.setText("Avaliação gratuita");
            changeButton.setText("VIRAR PREMIUM");
        } else {
            itemName.setText(billingName);
            changeButton.setText("ALTERAR");
        }

        changeButton.setTag(Buttons.SUBSCRIPTION);
        changeButton.setOnClickListener(this);
        mContentLayout.addView(mItemMySubscription);

        // Chosen capitals
        mItemMySubscription = inflateItemView();
        itemHeader = (TextView) mItemMySubscription.findViewById(R.id.itemHeader);
        itemName = (TextView) mItemMySubscription.findViewById(R.id.itemName);
        changeButton = (Button) mItemMySubscription.findViewById(R.id.changeButton);

        itemHeader.setText(getString(R.string.chosen_cities_text));
        //itemName.setText(getChosenCapitalsText());
        changeButton.setTag(Buttons.CITIES);
        changeButton.setOnClickListener(this);
        mContentLayout.addView(mItemMySubscription);

        // Chosen categories
        mItemMySubscription = inflateItemView();
        itemHeader = (TextView) mItemMySubscription.findViewById(R.id.itemHeader);
        itemName = (TextView) mItemMySubscription.findViewById(R.id.itemName);
        changeButton = (Button) mItemMySubscription.findViewById(R.id.changeButton);

        itemHeader.setText(getString(R.string.chosen_categories_text));
        //itemName.setText(getChosenCategoriesText());
        changeButton.setTag(Buttons.CATEGORIES);
        changeButton.setOnClickListener(this);
        mContentLayout.addView(mItemMySubscription);
    }

    private View inflateItemView() {
        return mLayoutInflater.inflate(R.layout.item_my_subscription, null);
    }


    @Override
    public void onClick(View view) {

        int tag = (int) view.getTag();

        switch (tag) {

            case Buttons.SUBSCRIPTION:
            {
                Intent intent = new Intent(this, BillingActivity.class);
                intent.putExtra("IS_CHANGING_SUBSCRIPTION", true);
                startActivity(intent);
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Alterar plano");
            } break;

            case Buttons.CITIES:
            {
                Intent intent = new Intent(this, ChangeChosenActivity.class);
                intent.putExtra(ChangeChosenActivity.FRAGMENT_ID, ChangeChosenActivity.CITIES_CHANGE_FRAGMENT);
                startActivity(intent);
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Alterar cidades");
            } break;

            case Buttons.CATEGORIES:
            {
                Intent intent = new Intent(this, ChangeChosenActivity.class);
                intent.putExtra(ChangeChosenActivity.FRAGMENT_ID, ChangeChosenActivity.CATEGORIES_CHANGE_FRAGMENT);
                startActivity(intent);
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Alterar segmentos");
            } break;

            case Buttons.STORE:
            {
                startActivity(Utils.createPlayStoreIntent(this));
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Cancelamento de assintura", "Botão ir para o aplicativo");
            } break;

            default:
                break;
        }
    }
}
