package com.example.smartcontract.models;

import java.util.Map;

public class ProductModel {

    public Map<String, String> getProductIds() {
        return productIds;
    }

    public void setProductIds(Map<String, String> productIds) {
        this.productIds = productIds;
    }

    public ProductModel(Map<String, String> productIds) {
        this.productIds = productIds;
    }

    Map<String, String> productIds;

}
