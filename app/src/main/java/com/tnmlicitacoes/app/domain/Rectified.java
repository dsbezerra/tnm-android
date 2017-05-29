package com.tnmlicitacoes.app.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Rectified implements Serializable {
    @SerializedName("status")
    @Expose
    public boolean status;
    @SerializedName("descricao")
    @Expose
    public String description;

    /*
    public Rectified() {
    }

    public Rectified(boolean status, String description) {
        this.mStatus = status;
        this.mDescription = description;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        this.mStatus = status;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }
*/
}