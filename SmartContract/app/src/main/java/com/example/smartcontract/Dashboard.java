package com.example.smartcontract;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
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
import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;

public class Dashboard extends AppCompatActivity {

    ProgressBar productDetailsLoader;
    LinearLayout productDetails, ownerClick;
    TextView companyName, productName, productCategory, owner;
    String contractAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
        productDetailsLoader = findViewById(R.id.productDetailsLoader);
        productDetails = findViewById(R.id.productDetails);
        companyName = findViewById(R.id.companyName);
        productName = findViewById(R.id.productName);
        productCategory = findViewById(R.id.productCategory);
        owner = findViewById(R.id.owner);
        ownerClick = findViewById(R.id.ownerClick);
        getUserRolesArray getUserRolesArray = new getUserRolesArray();
        getUserRolesArray.execute();
        getSmartContractDetails getSmartContractDetails = new getSmartContractDetails();
        getSmartContractDetails.execute();
    }

    class getUserRolesArray extends AsyncTask<Void, Void, List<Type>> {

        protected List<Type> doInBackground(Void... params) {
            List<Type> result = new ArrayList<>();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<DynamicArray<Address>>() {
                });
                Function function = new Function("getUserRolesArray", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                } else {
                    Log.d("Address Reason: ", ethCall.getRevertReason());
                }
            } catch (Exception e) {
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<Type> results) {
            super.onPostExecute(results);
            if (!results.isEmpty()) {
                data.userRoles = (String[]) results.get(0).getValue();
                Log.d("Address Roles", data.userRoles.length+"");
            }
        }
    }

    class getSmartContractDetails extends AsyncTask<Void, Void, List<Type>> {

        protected List<Type> doInBackground(Void... params) {
            List<Type> result = new ArrayList<>();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Address>() {
                });
                Function function = new Function("getSmartContractDetails", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                } else {
                    Log.d("Address Reason: ", ethCall.getRevertReason());
                }
            } catch (Exception e) {
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<Type> results) {
            super.onPostExecute(results);
            if (!results.isEmpty()) {
                companyName.setText(results.get(0).getValue().toString());
                productName.setText(results.get(1).getValue().toString());
                if (!results.get(2).getValue().toString().trim().isEmpty()) {
                    productCategory.setText("(" + results.get(2).getValue().toString() + ")");
                }else{
                    productCategory.setVisibility(View.GONE);
                }
                owner.setText("Owner: " + results.get(3).getValue().toString() + "\n(" + results.get(4).getValue().toString() + ")");
                ownerClick.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("PublicAddress", results.get(4).getValue().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(Dashboard.this, "Owner Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                ownerClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("PublicAddress", results.get(4).getValue().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(Dashboard.this, "Owner Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            productDetailsLoader.setVisibility(View.GONE);
            productDetails.setVisibility(View.VISIBLE);
        }
    }
}