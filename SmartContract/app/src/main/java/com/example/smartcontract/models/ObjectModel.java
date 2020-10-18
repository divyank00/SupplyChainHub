package com.example.smartcontract.models;

public class ObjectModel {
    boolean status;
    Object obj;
    String message;

    public ObjectModel() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean erstatusr) {
        this.status = status;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ObjectModel(boolean status, Object obj, String message) {
        this.status = status;
        this.obj = obj;
        this.message = message;
    }
}
