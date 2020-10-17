package com.example.smartcontract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText pk, ca, abi,seed;
    Button btn;

    public static final String SHAR_PREF = "sharedPref";
    public static final String KEY = "key";
    public static final String SEED = "seed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pk = findViewById(R.id.privateKey);
        ca = findViewById(R.id.contractAddress);
        abi = findViewById(R.id.abi);
        btn = findViewById(R.id.button);
        seed = findViewById(R.id.seed);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = "";
                if(pk.getText().toString().isEmpty() && seed.getText().toString().isEmpty()){

                    Toast.makeText(MainActivity.this, "Either enter the private key or seed phrase", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ca.getText().toString().isEmpty() || abi.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Fill Data!", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPreferences = getSharedPreferences(SHAR_PREF,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if(!pk.getText().toString().isEmpty()){

                    key = pk.getText().toString().trim();
                    editor.putString(KEY,key);
                }
                else if(!seed.getText().toString().isEmpty()){
                    key = seed.getText().toString().trim();
                    editor.putString(SEED,key);
                }

                Intent i = new Intent(MainActivity.this,AllFunctions.class);
                data.privateKey = key;
                data.contractAddress = ca.getText().toString().trim();
                data.abiCode = abi.getText().toString().trim();
                startActivity(i);
            }
        });
    }
}