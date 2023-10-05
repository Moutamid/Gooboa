package com.app.gobooa.models;

import java.util.List;

//This java class is used to declare all variables of products of orders
//This class also declared getter and setter methods to get and set
//// values of these variables
public class ProductModelClass {
    private int id;
    private String name;
    private int qty;
    private double subTotal;
    private List<MetaDataModelClass> extraData;


    public ProductModelClass() {}

    public ProductModelClass(int id, String name, int qty, double subTotal, List<MetaDataModelClass> extraData) {
        this.id = id;
        this.name = name;
        this.qty = qty;
        this.subTotal = subTotal;
        this.extraData = extraData;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQty() {
        return qty;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public List<MetaDataModelClass> getExtraData() {
        return extraData;
    }
}
