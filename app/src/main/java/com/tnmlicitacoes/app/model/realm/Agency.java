package com.tnmlicitacoes.app.model.realm;

import com.tnmlicitacoes.app.NoticesQuery;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Agency extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private String abbr;
    private City city;

    public Agency() {

    }

    public Agency(String id, String name, String abbr, City city) {
        this.id = id;
        this.name = name;
        this.abbr = abbr;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public static Agency mapToRealmFromGraphQL(NoticesQuery.Agency agency) {
        return new Agency(agency.id(), agency.name(), agency.abbr(),
                City.copyToRealmFromGraphQL(agency.city()));
    }
}
