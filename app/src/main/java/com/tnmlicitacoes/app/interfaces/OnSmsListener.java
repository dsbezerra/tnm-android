package com.tnmlicitacoes.app.interfaces;

public interface OnSmsListener {
    /**
     * Callback called when sms arrive with a verification code
     * @param verificationCode
     */
    void onSmsReceived(String verificationCode);
}
