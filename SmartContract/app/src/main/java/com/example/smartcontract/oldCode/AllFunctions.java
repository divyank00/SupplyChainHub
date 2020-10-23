package com.example.smartcontract.oldCode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.smartcontract.R;
import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.models.SingleContractModel;
import com.example.smartcontract.viewModel.SingleContractViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllFunctions extends AppCompatActivity {

    RecyclerView rV;
    Adapter adapter;
    String address;
//        SingleContractViewModel singleContractViewModel;
    ProgressBar loader;
    List<String> usedFunctions;
    String abi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_functions);
        getSupportActionBar().setTitle("Other Functions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rV = findViewById(R.id.rV);
        loader = findViewById(R.id.loader);
        Intent intent = getIntent();
        abi = intent.getStringExtra("abi");
        address = intent.getStringExtra("contractAddress");
//        singleContractViewModel = new SingleContractViewModel();
        usedFunctions = new ArrayList<>();
        setUsedFunctions();
        getContract();
    }

    private void setUsedFunctions() {
        usedFunctions.add("getUserRolesArray");
        usedFunctions.add("getSmartContractDetails");
        usedFunctions.add("getUserDetails");
        usedFunctions.add("setUserDetails");
        usedFunctions.add("makeOwnerAsNextRole");
        usedFunctions.add("makeLot");
        usedFunctions.add("packLot");
        usedFunctions.add("addChildUser");
        usedFunctions.add("addOtherUser");
        usedFunctions.add("getOwner");
        usedFunctions.add("getUserRole");
        usedFunctions.add("trackProductByLotId");
        usedFunctions.add("trackProductByProductId");
        usedFunctions.add("checkIsUser");
        usedFunctions.add("setProductFinalSellingPrice");
        usedFunctions.add("forSaleLotByManufacturer");
    }

    void getContract() {
//        singleContractViewModel.getContract(address).observe(this, new Observer<ObjectModel>() {
//            @Override
//            public void onChanged(ObjectModel objectModel) {
//                if (objectModel.isStatus()) {
//                    if (objectModel.getObj() != null) {
        try {
//                            String abi = ((SingleContractModel) objectModel.getObj()).getAbi();
            JSONArray obj = new JSONArray(abi);
            List<JSONObject> functions = new ArrayList<>();
            for (int i = 0; i < obj.length(); i++) {
                if (((JSONObject) obj.get(i)).optString("type").equals("function")) {
                    if (!usedFunctions.contains(((JSONObject) obj.get(i)).optString("name")))
                        functions.add((JSONObject) obj.get(i));
                }
            }
            loader.setVisibility(View.GONE);
            adapter = new Adapter(AllFunctions.this, functions, address);
            rV.setAdapter(adapter);
            rV.setLayoutManager(new LinearLayoutManager(AllFunctions.this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
//                    } else {
//                        loader.setVisibility(View.GONE);
//                        Toast.makeText(AllFunctions.this, "Supply Chain doesn't exist!", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    loader.setVisibility(View.GONE);
//                    Toast.makeText(AllFunctions.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}