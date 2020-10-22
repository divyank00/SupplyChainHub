package com.example.smartcontract.models;

public class TrackModel {

    String userAddress;
    String sellingPrice;
    String buyingPrice;
    String transactionHash;
    String name;
    String lat,lon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public TrackModel(String userAddress) {
        this.userAddress = userAddress;
    }

    public TrackModel(String userAddress, String sellingPrice, String buyingPrice, String transactionHash) {
        this.userAddress = userAddress;
        this.sellingPrice = sellingPrice;
        this.buyingPrice = buyingPrice;
        this.transactionHash = transactionHash;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(String sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(String buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
