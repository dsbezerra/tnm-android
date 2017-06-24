package com.tnmlicitacoes.app.interfaces;


import com.stripe.android.model.Card;
import com.tnmlicitacoes.app.model.SubscriptionPlan;

public interface OnSubscriptionListener {

    void onAdvanceButtonClicked();

    void onCardSelected(Card card);

    void onPlanSelected(SubscriptionPlan plan);


}
