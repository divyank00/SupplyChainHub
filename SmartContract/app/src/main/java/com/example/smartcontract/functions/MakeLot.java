package com.example.smartcontract.functions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.smartcontract.Data;
import com.example.smartcontract.R;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.List;

public class MakeLot extends AppCompatActivity {

    String contractAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_lot);
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
//        taskRunner = new AddUser.TaskRunner();
//        userRole = intent.getIntExtra("userRole", Data.userRoles.size());
//        parentAddressTV = findViewById(R.id.parentAddressTV);
//        parentAddress = findViewById(R.id.parentAddress);
//        userAddress = findViewById(R.id.userAddress);
//        spinner = findViewById(R.id.nice_spinner);
//        button = findViewById(R.id.button);
//        button.setBackgroundColor(0xFF3366ff);
//        List<String> dataSet = Data.userRoles.subList(userRole + 1, Data.userRoles.size() - 1);
//        spinner.attachDataSource(dataSet);
//        spinner.setSelectedIndex(0);
//        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
//            @Override
//            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
//                if (position == 0) {
//                    parentAddressTV.setVisibility(View.GONE);
//                    parentAddress.setVisibility(View.GONE);
//                    parentAddress.setText("");
//                } else {
//                    parentAddressTV.setVisibility(View.VISIBLE);
//                    parentAddress.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (spinner.getSelectedIndex() == 0) {
//                    if (userAddress.getText().toString().trim().isEmpty()) {
//                        userAddress.setError("Mandatory Field!");
//                    } else {
//                        executeAddChildUserGas(userAddress.getText().toString().trim());
//                    }
//                } else {
//                    if (userAddress.getText().toString().trim().isEmpty()) {
//                        userAddress.setError("Mandatory Field!");
//                    } else if (parentAddress.getText().toString().trim().isEmpty()) {
//                        parentAddress.setError("Mandatory Field!");
//                    } else {
//                        executeAddOtherUserGas(userAddress.getText().toString().trim(), parentAddress.getText().toString().trim(), userRole + 1 + spinner.getSelectedIndex());
//                    }
//                }
//            }
//        });
    }
}