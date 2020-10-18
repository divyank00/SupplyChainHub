package com.example.smartcontract.models;

public class ContractModel {

    String address, name;

    public ContractModel() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContractModel(String address, String name) {
        this.address = address;
        this.name = name;
    }
}
