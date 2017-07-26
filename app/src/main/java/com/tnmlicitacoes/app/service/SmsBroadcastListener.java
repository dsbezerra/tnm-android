package com.tnmlicitacoes.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.tnmlicitacoes.app.interfaces.OnSmsListener;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class listens for SMS messages (if allowed by the user) and
 * finds the the verification code within the sms to verify the number automatically
 */
public class SmsBroadcastListener extends BroadcastReceiver {

    /* The sms listener interface */
    public static OnSmsListener sListener;

    /* The protocol data unit objects retriever key */
    private final String PDUS = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {

        // We just need to do this if the user is waiting for the sms
        if (SettingsUtils.isWaitingForSms(context)) {

            String verificationCode = "";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                String smsText = "";
                for (int i = 0; i < messages.length; i++) {
                    smsText += messages[i].getMessageBody();
                }

                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(smsText);
                while (m.find()) {
                    verificationCode = m.group();
                }
            }
            // For older Android APIs we need to do manually...
            else {
                // Get intent extras
                Bundle b = intent.getExtras();
                if (b != null && !b.isEmpty()) {
                    SmsMessage[] messages;
                    // Get PDUs object (protocol data unit)
                    Object[] protocolDataUnits = (Object[]) b.get(PDUS);

                    if (protocolDataUnits != null) {
                        // Determine array of SmsMessage
                        messages = new SmsMessage[protocolDataUnits.length];

                        String smsText = "";
                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) protocolDataUnits[i]);
                            smsText += messages[i].getMessageBody();
                        }

                        Pattern p = Pattern.compile("\\d+");
                        Matcher m = p.matcher(smsText);
                        while (m.find()) {
                            verificationCode = m.group();
                        }
                    }
                }
            }

            // If we have a listener and we have a verification code notify the listener
            if (sListener != null && !TextUtils.isEmpty(verificationCode)) {
                sListener.onSmsReceived(verificationCode);
            }
        }
    }
}
