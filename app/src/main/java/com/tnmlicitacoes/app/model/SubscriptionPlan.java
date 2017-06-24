package com.tnmlicitacoes.app.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.tnmlicitacoes.app.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SubscriptionPlan implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            BASIC,
            DEFAULT,
            CUSTOM
    })
    public @interface Plan { }
    public static final String BASIC = "basic";
    public static final String DEFAULT = "default";
    public static final String CUSTOM = "custom";

    /* Item price */
    public static final float ITEM_PRICE = 2.50f;

    /* Minimum price for a plan */
    private static final float MIN_PRICE = 2.50f;

    /* Price for the basic plan */
    private static final float BASIC_PRICE = 5.00f;
    /* Price for the default plan */
    private static final float DEFAULT_PRICE = 15.00f;

    /* Minimum possible quantity */
    private static final int MIN_QUANTITY = 1;

    /* Invalid quantity */
    public static final int INVALID_QUANTITY = 0;
    /* Max basic plan quantity */
    public static final int BASIC_MAX_QUANTITY = 1;
    /* Max default plan quantity */
    public static final int DEFAULT_MAX_QUANTITY = 3;

    /* The subscription id */
    private String mId;

    /* The subscription plan name */
    private String mName;

    /* The subscription plan description */
    private String mDescription;

    /* The subscription plan price */
    private float mPrice;

    /* The subscription plan segment quantity */
    private int mSegmentQuantity;

    /* The subscription plan city quantity */
    private int mCityQuantity;

    /* Whether the plan is custom or not */
    private boolean mIsCustom;

    public SubscriptionPlan() {

    }

    public SubscriptionPlan(
            String id,
            String name,
            String description,
            float price,
            int segmentQuantity,
            int cityQuantity,
            boolean isCustom) {
        this.mId = id;
        this.mName = name;
        this.mDescription = description;
        this.mPrice = price;
        this.mSegmentQuantity = segmentQuantity;
        this.mCityQuantity = cityQuantity;
        this.mIsCustom = isCustom;
    }

    public SubscriptionPlan(
            String id,
            String name,
            String description) {
        this(
                id,
                name,
                description,
                MIN_PRICE,
                MIN_QUANTITY,
                MIN_QUANTITY,
                true);
    }

    public SubscriptionPlan(
            String id,
            String name,
            String description,
            float price,
            int segmentQuantity,
            int cityQuantity) {
        this(
                id,
                name,
                description,
                price,
                segmentQuantity,
                cityQuantity,
                false);
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public float getPrice() {
        if (mIsCustom) {
            return ITEM_PRICE * (mCityQuantity + mSegmentQuantity);
        }
        return mPrice;
    }

    /* Returns an instance of the basic subscription plan */
    public static SubscriptionPlan getBasicPlan(Context context) {
        return new SubscriptionPlan(
                BASIC,
                context.getString(R.string.plan_basic_name),
                context.getString(R.string.plan_basic_description),
                BASIC_PRICE,
                BASIC_MAX_QUANTITY,
                BASIC_MAX_QUANTITY
        );
    }

    /* Returns an instance of the default subscription plan */
    public static SubscriptionPlan getDefaultPlan(Context context) {
        return new SubscriptionPlan(
                DEFAULT,
                context.getString(R.string.plan_default_name),
                context.getString(R.string.plan_default_description),
                DEFAULT_PRICE,
                DEFAULT_MAX_QUANTITY,
                DEFAULT_MAX_QUANTITY
        );
    }

    /* Returns an instance of the custom subscription plan */
    public static SubscriptionPlan getCustomPlan(Context context) {
        return new SubscriptionPlan(
                CUSTOM,
                context.getString(R.string.plan_custom_name),
                context.getString(R.string.plan_custom_description)
        );
    }

    /**
     * Get max quantity for basic and default plans
     */
    public int getMaxQuantity(String planId) {
        if (planId == null) {
            return INVALID_QUANTITY;
        }

        switch (planId) {
            case BASIC:
                return BASIC_MAX_QUANTITY;
            case DEFAULT:
                return DEFAULT_MAX_QUANTITY;
            default:
                return INVALID_QUANTITY;
        }
    }

    public int getSegmentQuantity() {
        if (mId.equals(BASIC) || mId.equals(DEFAULT)) {
            return getMaxQuantity(mId);
        } else {
            return mSegmentQuantity;
        }
    }

    public int getCityQuantity() {
        if (mId.equals(BASIC) || mId.equals(DEFAULT)) {
            return getMaxQuantity(mId);
        } else {
            return mCityQuantity;
        }
    }

    public void setCityQuantity(int quantity) {
        this.mCityQuantity = quantity;
    }

    public void setSegmentQuantity(int quantity) {
        this.mSegmentQuantity = quantity;
    }

    public boolean isCustom() {
        return mIsCustom || mId.equals(CUSTOM);
    }

    protected SubscriptionPlan(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mDescription = in.readString();
        mPrice = in.readFloat();
        mSegmentQuantity = in.readInt();
        mCityQuantity = in.readInt();
        mIsCustom = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeFloat(mPrice);
        dest.writeInt(mSegmentQuantity);
        dest.writeInt(mCityQuantity);
        dest.writeByte((byte) (mIsCustom ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SubscriptionPlan> CREATOR
            = new Parcelable.Creator<SubscriptionPlan>() {
        @Override
        public SubscriptionPlan createFromParcel(Parcel in) {
            return new SubscriptionPlan(in);
        }

        @Override
        public SubscriptionPlan[] newArray(int size) {
            return new SubscriptionPlan[size];
        }
    };
}
