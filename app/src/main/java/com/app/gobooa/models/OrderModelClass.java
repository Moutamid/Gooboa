package com.app.gobooa.models;

import java.util.List;

//This java class is used to declare all the required variables for an order to be fetched
// and displayed in list.. This class also declared getter and setter methods to get and set
// values of these variables
public class OrderModelClass {
    private int id;
    private String status;
    private String paymentMethod;
    private String dateCreated;
    private String firstName;
    private String lastName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String phone;
    private List<MetaDataModelClass> metaDataList;
    private List<ProductModelClass> lineItemsList;
    private String currency;
    private String total;

    public OrderModelClass() {}

    public OrderModelClass(int id, String status, String paymentMethod, String dateCreated, String firstName,
                           String lastName, String address1, String address2, String city, String state, String postalCode,
                           String phone, List<MetaDataModelClass> metaDataList, List<ProductModelClass> lineItemsList, String currency, String total) {
        this.id = id;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.dateCreated = dateCreated;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.phone = phone;
        this.metaDataList = metaDataList;
        this.lineItemsList = lineItemsList;
        this.currency= currency;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public List<MetaDataModelClass> getMetaDataList() {
        return metaDataList;
    }

    public List<ProductModelClass> getLineItemsList() {
        return lineItemsList;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTotal() {
        return total;
    }
}
