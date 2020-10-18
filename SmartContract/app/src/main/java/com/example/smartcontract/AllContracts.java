package com.example.smartcontract;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.models.ContractModel;
import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.viewModel.AllContractsViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AllContracts extends AppCompatActivity {

    RecyclerView rV;
    contractsAdapter adapter;
    Button button;
    List<ContractModel> mList;
    ProgressBar loader;
    AllContractsViewModel allContractsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contracts);
        rV = findViewById(R.id.rV);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        loader = findViewById(R.id.loader);
        mList = new ArrayList<>();
        adapter = new contractsAdapter(this, mList);
        rV.setLayoutManager(new LinearLayoutManager(this));
        rV.setAdapter(adapter);
        allContractsViewModel = new AllContractsViewModel();
        fetchData();
    }

    void showDialog() {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.activity_add_contract);
            EditText address = dialog.findViewById(R.id.contractAddress);
            EditText name = dialog.findViewById(R.id.name);
            Button b = dialog.findViewById(R.id.button);
            ProgressBar loader = dialog.findViewById(R.id.loader);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (address.getText().toString().trim().isEmpty()) {
                        address.setError("Enter the contract address!");
                        return;
                    }
                    if (name.getText().toString().trim().isEmpty()) {
                        name.setError("Enter a suitable name!");
                        return;
                    }
                    for(ContractModel contractModel : mList){
                        if(contractModel.getAddress().equals(address.getText().toString().trim())){
                            Toast.makeText(AllContracts.this, "Following Supply-Chain already exists!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    b.setVisibility(View.GONE);
                    loader.setVisibility(View.VISIBLE);
                    allContractsViewModel.addContract(address.getText().toString().trim(), name.getText().toString().trim())
                            .observe(AllContracts.this, new Observer<ObjectModel>() {
                                @Override
                                public void onChanged(ObjectModel objectModel) {
                                    if (objectModel.isStatus()) {
                                        mList.add(0, (ContractModel) objectModel.getObj());
                                        adapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(AllContracts.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        loader.setVisibility(View.GONE);
                                        b.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void fetchData() {
        allContractsViewModel.getContracts().observe(this, new Observer<ObjectModel>() {
            @Override
            public void onChanged(ObjectModel objectModel) {
                if (objectModel.isStatus()) {
                    loader.setVisibility(View.GONE);
                    mList.addAll((Collection<? extends ContractModel>) objectModel.getObj());
                } else {
                    Toast.makeText(AllContracts.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}