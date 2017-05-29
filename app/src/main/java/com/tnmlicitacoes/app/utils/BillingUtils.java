package com.tnmlicitacoes.app.utils;

import android.content.Context;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.domain.Subscription;

import java.util.Arrays;
import java.util.List;

public class BillingUtils {

    public static boolean sIsTrialActive = false;

    public static final int SUBSCRIPTION_MAX_ITEMS_BASIC = 1;

    public static final int SUBSCRIPTION_MAX_ITEMS_DEFAULT = 3;

    public static final int SUBSCRIPTION_MAX_ITEMS_PREMIUM = Integer.MAX_VALUE;

    public static int SUBSCRIPTION_MAX_ITEMS = SUBSCRIPTION_MAX_ITEMS_PREMIUM;

    public static final String SKU_SUBSCRIPTION_TRIAL = "subscription_trial";

    public static final String SKU_SUBSCRIPTION_LEGACY = "premium_correct";

    public static final String SKU_SUBSCRIPTION_BASIC = "subscription_basic";

    public static final String SKU_SUBSCRIPTION_DEFAULT = "subscription_default";

    public static final String SKU_SUBSCRIPTION_PREMIUM = "subscription_premium";

    private static final int[] sFeatures = {
            R.string.sub_feature_one,
            R.string.sub_feature_two,
            R.string.sub_feature_three
    };

    private static final int[] sFeaturesUnlimited = {
            R.string.sub_feature_one,
            R.string.sub_feature_two,
            R.string.sub_feature_three,
            R.string.sub_feature_four
    };

    public static List<Subscription> getSubscriptions(Context context) {
        return Arrays.asList(
                new Subscription(context, R.string.sub_basic_name, R.string.sub_basic_description,
                        SKU_SUBSCRIPTION_BASIC, SUBSCRIPTION_MAX_ITEMS_BASIC, 4.99f, sFeatures),

                new Subscription(context, R.string.sub_default_name, R.string.sub_default_description,
                        SKU_SUBSCRIPTION_DEFAULT, SUBSCRIPTION_MAX_ITEMS_DEFAULT, 9.99f, sFeatures),

                new Subscription(context, R.string.sub_premium_name, R.string.sub_premium_description,
                        SKU_SUBSCRIPTION_PREMIUM, SUBSCRIPTION_MAX_ITEMS_PREMIUM, 19.99f, sFeaturesUnlimited)
        );
    }

    public static Subscription getSubscription(Context context, String sku) {
        List<Subscription> subscriptions = getSubscriptions(context);
        for(Subscription sub : subscriptions) {
            if(sub.getSku().equals(sku)) {
                return sub;
            }
        }
        return null;
    }

    public static void setDefaultMaxItems(Context context) {
        String sku = SettingsUtils.getBillingState(context);
        if(sku.equals(SKU_SUBSCRIPTION_BASIC) || sku.equals(SKU_SUBSCRIPTION_LEGACY)) {
            SUBSCRIPTION_MAX_ITEMS = SUBSCRIPTION_MAX_ITEMS_BASIC;
        } else if (sku.equals(SKU_SUBSCRIPTION_DEFAULT)) {
            SUBSCRIPTION_MAX_ITEMS = SUBSCRIPTION_MAX_ITEMS_DEFAULT;
        } else if (sku.equals(SKU_SUBSCRIPTION_PREMIUM)) {
            SUBSCRIPTION_MAX_ITEMS = SUBSCRIPTION_MAX_ITEMS_PREMIUM;
        }
    }

    public static String getMaxText(int quantity) {
        if(quantity == SUBSCRIPTION_MAX_ITEMS_PREMIUM) {
            return "ilimitado";
        }
        return quantity + "";
    }

    private BillingUtils() {}

}
