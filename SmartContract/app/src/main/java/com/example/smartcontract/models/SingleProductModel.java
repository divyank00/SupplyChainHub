package com.example.smartcontract.models;

public class SingleProductModel {

    String name, contractAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public SingleProductModel(String name, String contractAddress) {
        this.name = name;
        this.contractAddress = contractAddress;
    }
}
