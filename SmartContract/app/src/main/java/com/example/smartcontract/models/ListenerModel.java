package com.example.smartcontract.models;

import android.view.View;

public class ListenerModel {
    String name,description;
    View.OnClickListener clickListener;

    public ListenerModel(String name, String description, View.OnClickListener clickListener) {
        this.name = name;
        this.description = description;
        this.clickListener = clickListener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public View.OnClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
