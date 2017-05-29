package com.tnmlicitacoes.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.tnmlicitacoes.app.interfaces.OnSmsListener;
import com.tnmlicitacoes.app.utils.AndroidUtilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsBroadcastListener extends BroadcastReceiver {

    public static OnSmsListener sListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(AndroidUtilities.sIsWaitingSms) {

            String verificationCode = "";

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                String smsText = "";
                for (int i = 0; i < msgs.length; i++) {
                    smsText += msgs[i].getMessageBody();
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
                if(b != null) {
                    SmsMessage[] msgs;
                    // Get pdus object (protocol data unit)
                    Object[] pdus = (Object[]) b.get("pdus");

                    if(pdus != null) {
                        // Determine array of SmsMessage
                        msgs = new SmsMessage[pdus.length];

                        String smsText = "";
                        for(int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            smsText += msgs[i].getMessageBody();
                        }

                        Pattern p = Pattern.compile("\\d+");
                        Matcher m = p.matcher(smsText);
                        while (m.find()) {
                            verificationCode = m.group();
                        }
                    }
                }
            }

            if(sListener != null)
                sListener.onSmsReceived(verificationCode);
        }
    }
}
