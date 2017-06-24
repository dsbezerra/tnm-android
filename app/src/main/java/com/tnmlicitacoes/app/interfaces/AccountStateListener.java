package com.tnmlicitacoes.app.interfaces;

public interface AccountStateListener {

    /**
     * Triggered when the user clicks to subscribe
     */
    void onSubscribeClick();

    /**
     * Triggered when the user clicks in the payment list item
     */
    void onPaymentClick();

    /**
     * Triggered when the user clicks to resend the verification email
     */
    void onResendEmailClick();

    /**
     * Triggered when the user clicks to set a email
     */
    void onDefineEmailClick();

    /**
     * Triggered when the user clicks to change the picked cities
     */
    void onChangePickedCitiesClick();

    /**
     * Triggered when the user clicks to change the picked segments
     */
    void onChangePickedSegmentsClick();

    /**
     * Triggered when the user clicks on a item of the About section
     * NOTE(diego): This has nothing to do with the user account, we can put this
     * in another interface or handle direct at the adapter.
     */
    void onAboutItemClick(int itemId);

    /**
     * Triggered when the user clicks to logout
     */
    void onLogoutClick();
}
