package com.example.omi.truecallerbddemo.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by omi on 12/14/2016.
 */

public class Contact implements Serializable {


    @SerializedName("number")
    public String number;

    @SerializedName("name")
    public String name;

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {

        return number;
    }

    public String getName() {
        return name;
    }

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }
}
