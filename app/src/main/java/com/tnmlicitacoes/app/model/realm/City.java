package com.tnmlicitacoes.app.model.realm;

import com.tnmlicitacoes.app.NoticeByIdQuery;
import com.tnmlicitacoes.app.NoticesQuery;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class City extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private String state;

    public City() {

    }

    public City(String id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public static City copyToRealmFromGraphQL(NoticesQuery.City city) {
        return new City(city.id(), city.name(), city.state().name());
    }

    public static City copyToRealmFromGraphQL(NoticeByIdQuery.City city) {
        return new City(city.id(), city.name(), city.state().name());
    }
}
