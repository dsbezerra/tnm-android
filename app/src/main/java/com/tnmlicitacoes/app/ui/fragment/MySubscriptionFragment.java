package com.tnmlicitacoes.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.billing.BillingActivity;
import com.tnmlicitacoes.app.ui.activity.ChangeChosenActivity;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

public class MySubscriptionFragment extends Fragment implements View.OnClickListener {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_account, container, false);
        initViews(v);
        initViewListeners();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View v) {
        mContentLayout = (LinearLayout) v.findViewById(R.id.contentLinearLayout);
        mGoToStore = (Button) v.findViewById(R.id.btnAppStore);

        mLayoutInflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        String billingName = SettingsUtils.getBillingSubName(getContext());
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
                Intent intent = new Intent(getContext(), BillingActivity.class);
                intent.putExtra("IS_CHANGING_SUBSCRIPTION", true);
                startActivity(intent);
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Alterar plano");
            } break;

            case Buttons.CITIES:
            {
                Intent intent = new Intent(getContext(), ChangeChosenActivity.class);
                intent.putExtra(ChangeChosenActivity.FRAGMENT_ID, ChangeChosenActivity.CITIES_CHANGE_FRAGMENT);
                startActivity(intent);
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Alterar cidades");
            } break;

            case Buttons.CATEGORIES:
            {
                Intent intent = new Intent(getContext(), ChangeChosenActivity.class);
                intent.putExtra(ChangeChosenActivity.FRAGMENT_ID, ChangeChosenActivity.CATEGORIES_CHANGE_FRAGMENT);
                startActivity(intent);
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Alterar segmentos");
            } break;

            case Buttons.STORE:
            {
                startActivity(Utils.createPlayStoreIntent(getContext()));
                //AnalyticsUtils.fireEvent(getApplicationContext(), "Cancelamento de assintura", "Botão ir para o aplicativo");
            } break;

            default:
                break;
        }
    }
}
