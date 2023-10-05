package com.app.gobooa.models;

//This java class is used to declare all the required variables for user to be fetched from firebase
// real time and store in this class object. This class also declared getter and setter methods to get and set
// values of these variables

public class UserModeClass {
    private String clientKey;
    private String clientSecret;
    private String domain;
    private String email;

    public UserModeClass() {}

    public UserModeClass(String clientKey, String clientSecret, String domain, String email) {
        this.clientKey = clientKey;
        this.clientSecret = clientSecret;
        this.domain = domain;
        this.email = email;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getDomain() {
        return domain;
    }

    public String getEmail() {
        return email;
    }
}
