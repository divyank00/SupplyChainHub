package com.example.smartcontract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    SingleContractViewModel singleContractViewModel;
    ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_functions);
        rV = findViewById(R.id.rV);
        loader = findViewById(R.id.loader);
        Intent intent =getIntent();
        address = intent.getStringExtra("contractAddress");
        singleContractViewModel = new SingleContractViewModel();
        getContract();
    }

    void getContract(){
        singleContractViewModel.getContract(address).observe(this, new Observer<ObjectModel>() {
            @Override
            public void onChanged(ObjectModel objectModel) {
                if(objectModel.isStatus()){
                    if(objectModel.getObj()!=null) {
                        try {
                            String abi = ((SingleContractModel) objectModel.getObj()).getAbi();
                            JSONArray obj = new JSONArray(abi);
                            List<JSONObject> functions = new ArrayList<>();
                            for (int i = 0; i < obj.length(); i++) {
                                if (((JSONObject) obj.get(i)).optString("type").equals("function")) {
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
                    }
                }else{
                    loader.setVisibility(View.GONE);
                    Toast.makeText(AllFunctions.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}