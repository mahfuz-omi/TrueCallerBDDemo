package com.example.omi.truecallerbddemo.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by omi on 12/29/2016.
 */

public class CheckPhoneNoRequest implements Serializable {


    @SerializedName("phone_no")
    private String phone_no;

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }
}
