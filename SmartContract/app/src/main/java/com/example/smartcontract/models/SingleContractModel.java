package com.example.smartcontract.models;

public class SingleContractModel {

    String address, abi;

    public SingleContractModel() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public SingleContractModel(String address, String abi) {
        this.address = address;
        this.abi = abi;
    }
}
