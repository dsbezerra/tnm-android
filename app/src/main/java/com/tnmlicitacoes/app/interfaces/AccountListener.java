package com.tnmlicitacoes.app.interfaces;

public interface AccountListener {

    /**
     * Called when the user clicks in the button to subscribe or cancel the subscription
     * @param isActive whether the subscription is active or not
     *
     *                 if isActive equals true we show a dialog explaining how the cancellation works
     *                 if false we send the user to the SubscriptionActivity
     *
     * @param cancelAtPeriodEnd whether there is a pending cancellation
     */
    void onSubscribeClick(boolean isActive, boolean cancelAtPeriodEnd);

    /**
     * Called when the user clicks on the payment list item
     * TODO(diego): Replace this with a single method like OnAccountItemClick
     */
    void onPaymentClick();

    /**
     * Called when the user clicks to resend the verification email
     */
    void onResendEmailClick();

    /**
     * Called when the user clicks to define a new email
     */
    void onDefineEmailClick();

    /**
     * Called when the user clicks to change picked cities
     */
    void onChangePickedCitiesClick();

    /**
     * Called when the user clicks to change picked segments
     */
    void onChangePickedSegmentsClick();

    /**
     * Called when the users clicks in a item on the About section
     * NOTE(diego): This has nothing to do with the user account, we can put this
     * in another interface or handle direct at the adapter.
     */
    void onAboutItemClick(int itemId);

    /**
     * Called when the user clicks in the logout button
     */
    void onLogoutClick();
}
