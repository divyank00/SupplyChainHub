package com.example.smartcontract;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.LinkedList;
import java.util.List;

public class AddUser extends AppCompatActivity {

    String contractAddress;
    Integer userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
        userRole = intent.getIntExtra("userRole",data.userRoles.size());
        NiceSpinner spinner = (NiceSpinner) findViewById(R.id.nice_spinner);
        List<String> dataSet = data.userRoles.subList(userRole+1,data.userRoles.size()-1);
        spinner.attachDataSource(dataSet);
        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {

            }
        });
    }
}