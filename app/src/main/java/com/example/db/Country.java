package com.example.db;

import org.litepal.crud.DataSupport;

/**
 * Created by yy on 2018/1/27.
 */

public class Country extends DataSupport {
    private int id;
    private String countryName;
    private int cityId;//城市ID

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
