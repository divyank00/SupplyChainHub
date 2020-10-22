package com.example.smartcontract.models;

public class TrackModel {

    String userAddress;
    String sellingPrice;
    String buyingPrice;
    String transactionHash;

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
