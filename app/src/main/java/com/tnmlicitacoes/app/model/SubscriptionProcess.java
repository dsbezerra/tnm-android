package com.tnmlicitacoes.app.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SubscriptionProcess implements Parcelable {

    /*
     * The picked card id and basic info
     * NOTE(diego): We don't use the stripe Card class here because it doesn't implement
     * the Parcelable interface
     * */
    private String mCardId;
    private String mCardBrand;
    private String mCardLast4;

    /* The picked plan by */
    private SubscriptionPlan mPlan;

    /* The interval in months (used only with custom plans) */
    private int mInterval = -1;

    /* Whether the subscription process is in its confirm stage or not */
    private boolean mIsConfirming = false;

    public SubscriptionProcess() { }

    public boolean hasPickedCard() {
        return this.mCardId != null && this.mCardBrand != null && this.mCardLast4 != null;
    }

    public boolean hasPickedPlan() {
        return this.mPlan != null;
    }

    /**
     * Sets the card
     */
    public void setCard(String id, String brand, String last4) {
        setCardId(id);
        setCardBrand(brand);
        setCardLast4(last4);
    }

    /**
     * Sets the picked card id
     */
    public void setCardId(String cardId) {
        this.mCardId = cardId;
    }

    /**
     * Gets the picked card id
     */
    public String getCardId() {
        return this.mCardId;
    }

    /**
     * Sets the card brand
     */
    public void setCardBrand(String brand) {
        this.mCardBrand = brand;
    }

    /**
     * Gets the card brand
     * */
    public String getCardBrand() {
        return mCardBrand;
    }

    /**
     * Sets the card last 4 digits
     */
    public void setCardLast4(String last4) {
        this.mCardLast4 = last4;
    }

    /**
     * Gets the card last 4 digits
     */
    public String getCardLast4() {
        return mCardLast4;
    }

    /**
     * Sets the picked plan
     */
    public void setPlan(SubscriptionPlan plan) {
        this.mPlan = plan;
    }

    /**
     * Gets the picked plan
     */
    public SubscriptionPlan getPlan() {
        return this.mPlan;
    }

    /**
     * Whether the process is in review state or not
     */
    public boolean isConfirming() {
        return this.mIsConfirming;
    }

    /**
     * Sets the confirming value
     * @param value the new value
     */
    public void setConfirming(boolean value) {
        this.mIsConfirming = value;
    }

    /**
     * Defines the interval in months which the user will be charged
     * @param months the number of months
     */
    public void setInterval(int months) {
        this.mInterval = months;
    }

    /**
     * Get the interval in months which the user will be charged
     * @return the number of months
     */
    public int getInterval() {
        return this.mInterval;
    }

    /**
     * Sets the segment quantity for a custom plan
     * @param quantity the new quantity
     * @return true on success, otherwise false is returned
     */
    public boolean setCustomSegmentQuantity(int quantity) {
        if (mPlan != null && mPlan.isCustom()) {
            mPlan.setSegmentQuantity(quantity);
            return  true;
        }
        return false;
    }

    /**
     * Sets the city quantity for a custom plan
     * @param quantity the new quantity
     * @return true on success, otherwise false is returned
     */
    public boolean setCustomCityQuantity(int quantity) {
        if (mPlan != null && mPlan.isCustom()) {
            mPlan.setCityQuantity(quantity);
            return  true;
        }
        return false;
    }

    /**
     * Clears the subscription process to initial state
     */
    public void clear() {
        mPlan = null;
        mCardId = null;
        mCardBrand = null;
        mCardLast4 = null;
        mInterval = -1;
    }

    /**
     * Clears the card info
     */
    public void clearCard() {
        this.mCardId = null;
        this.mCardBrand = null;
        this.mCardLast4 = null;
    }

    /**
     * Clears the plan
     */
    public void clearPlan() {
        this.mPlan = null;
    }

    protected SubscriptionProcess(Parcel in) {
        mCardId = in.readString();
        mCardBrand = in.readString();
        mCardLast4 = in.readString();
        mPlan = (SubscriptionPlan) in.readValue(SubscriptionPlan.class.getClassLoader());
        mInterval = in.readInt();
        mIsConfirming = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCardId);
        dest.writeString(mCardBrand);
        dest.writeString(mCardLast4);
        dest.writeValue(mPlan);
        dest.writeInt(mInterval);
        dest.writeByte((byte) (mIsConfirming ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SubscriptionProcess> CREATOR
            = new Parcelable.Creator<SubscriptionProcess>() {
        @Override
        public SubscriptionProcess createFromParcel(Parcel in) {
            return new SubscriptionProcess(in);
        }

        @Override
        public SubscriptionProcess[] newArray(int size) {
            return new SubscriptionProcess[size];
        }
    };
}