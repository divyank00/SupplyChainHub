package com.example.smartcontract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllFunctions extends AppCompatActivity {

    RecyclerView rV;
    Adapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_functions);
        String abi = data.abiCode;
        rV = findViewById(R.id.rV);
        try {
            JSONArray obj = new JSONArray(abi);
            List<JSONObject> functions = new ArrayList<>();
            for (int i = 0; i < obj.length(); i++) {
                if (((JSONObject) obj.get(i)).optString("type").equals("function")){
                    functions.add((JSONObject) obj.get(i));
                }
            }
            adapter = new Adapter(this,functions);
            rV.setAdapter(adapter);
            rV.setLayoutManager(new LinearLayoutManager(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}