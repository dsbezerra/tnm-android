package com.tnmlicitacoes.app.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Attachment implements Serializable {
    @SerializedName("titulo")
    @Expose
    public String title;
    @SerializedName("link")
    @Expose
    public String downloadLink;
    @SerializedName("id")
    @Expose
    public String id;

    /**
     * No args constructor for use in serialization
     * */
    public Attachment() {
    }


}
