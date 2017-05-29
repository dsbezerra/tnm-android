package com.tnmlicitacoes.app.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Installation {

    @SerializedName("deviceToken")
    @Expose
    public String deviceToken;
    @SerializedName("deviceType")
    @Expose
    public String deviceType;
    @SerializedName("deviceId")
    @Expose
    public String deviceId;
    @SerializedName("plano")
    @Expose
    public String subscription;
    @SerializedName("activationDate")
    @Expose
    public Date activationDate;
    @SerializedName("phone")
    @Expose
    public String phone;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("segmentoIds")
    @Expose
    public List<String> categoriesId = new ArrayList<>();
}
