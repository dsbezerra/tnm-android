package com.tnmlicitacoes.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.PurchaseEvent;
import com.crashlytics.android.answers.SearchEvent;
import com.crashlytics.android.answers.SignUpEvent;
import com.crashlytics.android.answers.StartCheckoutEvent;
import com.tnmlicitacoes.app.billing.Purchase;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;

import static com.tnmlicitacoes.app.utils.BillingUtils.getSubscription;

/**
 * Utils
 */

public class Utils {

    public static final String[] BASE64_PIECES = {
            "b>d77HeRUC_>uVrMsmWv^uh6kcilu>W_F(@hn74o6mnFoJVNCFVFE",
            "2~@3~DvgLK\u007Fc_gmvOVahP)0JwDv5",
            "obmFBLuTOucDbmLTOFf2a|uTpdL0iQ\\Vt`DnGB1Pp@sqP1d}OhWh3O\\gMp6PTrkW4s\u007Fnrd1VJhpRt3RdpIm|BBJ`b\u007F34HL",
            "`1FiKfN03voOoR^P^pG|SRAu5b",
            "An1sJvQsBlA1`ydu5urBfBw5",
            "cOKlTxLUw;Ntr{@cA3M-NJwlM;uS1RZ3Txf;EPOO3UQmaN7[tRTFlCXoLHKQz21C2v--5HndSJ",
            "4W9j8DIji5lHqs`nIQ5Q[KyPBTOC.sCk8@xh",
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoWkJqc"
    };


    private static final String TAG = "Utils";

    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
    public static final long MONTH_IN_MILLIS = WEEK_IN_MILLIS * 4;
    public static final long YEAR_IN_MILLIS = MONTH_IN_MILLIS * 12;

    public static final String USER_EMAIL_PARAM = "user";

    public static final String FILE_LINK_PARAM = "pdflink";

    public static final String FILE_NAME_PARAM = "pdfname";

    public static final String AGENCY_NAME_PARAM = "orgao";

    public static final int MAX_EMAILS = 5;

    public static Intent sendContactEmail(String subject, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"contato@tnmlicitacoes.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT   , text);
        return i;
    }

    public static String decode(String[] pieces) {
        String reverseString = "";
        for(int i = 0; i < pieces.length; ++i) {
            char[] chars = pieces[i].toCharArray();
            for(int j = 0; j < chars.length; ++j) {
                chars[j] = (char) (chars[j] ^ pieces.length - i - 1);
            }
            if(i == pieces.length - 1) {
                reverseString += String.valueOf(chars);
                break;
            }
            reverseString += String.valueOf(chars) + (char) 0x2B;
        }

        String decodeString = "";
        String[] splitted = reverseString.split("\\+");
        for(int i = splitted.length - 1; i >= 0; --i) {
            if(i == 0) {
                decodeString += splitted[i];
                break;
            }

            decodeString += splitted[i] + (char) 0x2B;
        }

        return decodeString;
    }

    public static String decode(String string, int value) {
        char[] chars = string.toCharArray();
        for(int c = 0; c < chars.length; ++c) {
            chars[c] = (char) (chars[c] ^ value);
        }
        return String.valueOf(chars);
    }

    public static boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    public static Intent createPlayStoreIntent(Context context) {
        Intent intent;
        final String appPackageName = context.getPackageName();
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        } catch (android.content.ActivityNotFoundException anfe) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
        }
        return intent;
    }

    /*
    public static String generatePayload(Context context) {
        String payload = "";
        final String GOOGLE_ACCOUNT = "com.google";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                if(account.type.equals(GOOGLE_ACCOUNT)) {
                    payload = account.name + " tipo: " + account.type;
                    Log.d("generatePayload()", account.name + " type: " + account.type);
                    break;
                }
            }
        }

        return payload;
    }*/
}
