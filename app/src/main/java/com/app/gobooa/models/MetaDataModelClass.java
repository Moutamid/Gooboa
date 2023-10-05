package com.app.gobooa.models;

//This java class is used to declare variables of meta data tag of an order.
//This class also declared getter and setter methods to get and set
//// values of these variables
public class MetaDataModelClass {
    private String key;
    private String value;

    public MetaDataModelClass() {}

    public MetaDataModelClass(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
