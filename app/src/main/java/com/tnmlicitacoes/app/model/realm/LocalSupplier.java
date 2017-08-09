package com.tnmlicitacoes.app.model.realm;

import android.text.TextUtils;

import com.tnmlicitacoes.app.apollo.SupplierQuery;
import com.tnmlicitacoes.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LocalSupplier extends RealmObject {

    @PrimaryKey
    private String id;

    /** The supplier registration date */
    private Date createdAt;

    /* The supplier phone number */
    private String phone;

    /* The supplier name */
    private String name;

    /* The supplier email */
    private String email;

    /* The supplier subscription city num */
    private int cityNum;

    /* The supplier subscription segment num */
    private int segNum;

    /* Whether the email of the supplier is confirmed or not */
    private boolean activated;

    /* The default supplier card used in subscription */
    private String defaultCard;

    /** Whether the subscription is active or not */
    private boolean activeSubscription;

    /** The subscription period start date */
    private Date currentPeriodStart;

    /** The subscription period end date */
    private Date currentPeriodEnd;

    /** The subscription status in Stripe's system */
    private String subscriptionStatus;

    /** Whether the subscription was canceled or not */
    private boolean cancelAtPeriodEnd;

    /* The supplier list of cards */
    private RealmList<Card> cards;

    /**
     * NOTE(diego):
     * Realm doesn't support primitive lists so we use a string to store all ids
     **/
    private String cities;
    private String segments;

    public LocalSupplier() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public int getSegNum() {
        return segNum;
    }

    public void setSegNum(int segNum) {
        this.segNum = segNum;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getDefaultCard() {
        return defaultCard;
    }

    public void setDefaultCard(String defaultCard) {
        this.defaultCard = defaultCard;
    }

    public boolean isActiveSubscription() {
        return activeSubscription;
    }

    public void setActiveSubscription(boolean activeSubscription) {
        this.activeSubscription = activeSubscription;
    }

    public Date getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public void setCurrentPeriodStart(Date currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    public Date getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(Date currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public RealmList<Card> getCards() {
        return cards;
    }

    public void setCards(RealmList<Card> cards) {
        this.cards = cards;
    }

    public static LocalSupplier copyToRealmFromGraphQL(SupplierQuery.Supplier supplier) {
        LocalSupplier result = new LocalSupplier();
        result.setId(supplier.id());
        result.setName(supplier.name());
        result.setPhone(supplier.phone());
        result.setEmail(supplier.email());
        result.setDefaultCard(supplier.defaultCard());

        if (supplier.activated() != null) {
            result.setActivated(supplier.activated());
        }

        if (supplier.cityNum() != null) {
            result.setCityNum(supplier.cityNum());

        }

        if (supplier.segNum() != null) {
            result.setSegNum(supplier.segNum());
        }

        if (supplier.activeSubscription() != null) {
            result.setActiveSubscription(supplier.activeSubscription());
        }

        if (supplier.currentPeriodStart() != null) {
            result.setCurrentPeriodStart(DateUtils.parse(supplier.currentPeriodStart().toString()));
        }

        if (supplier.currentPeriodEnd() != null) {
            result.setCurrentPeriodEnd(DateUtils.parse(supplier.cancelAtPeriodEnd().toString()));
        }

        if (!TextUtils.isEmpty(supplier.subscriptionStatus())) {
            result.setSubscriptionStatus(supplier.subscriptionStatus());
        }

        if (supplier.cancelAtPeriodEnd() != null) {
            result.setCancelAtPeriodEnd(supplier.cancelAtPeriodEnd());
        }

        return result;
    }

    public String getCities() {
        return cities;
    }

    public void setCities(String cities) {
        this.cities = cities;
    }

    public String getSegments() {
        return segments;
    }

    public void setSegments(String segments) {
        this.segments = segments;
    }

    public List<String> getCitiesIds() {
        if (TextUtils.isEmpty(getCities())) {
            return null;
        }

        return new ArrayList<>(Arrays.asList(getCities().split(";")));
    }

    public List<String> getSegmentsIds() {
        if (TextUtils.isEmpty(getSegments())) {
            return null;
        }

        return new ArrayList<>(Arrays.asList(getSegments().split(";")));
    }
}
