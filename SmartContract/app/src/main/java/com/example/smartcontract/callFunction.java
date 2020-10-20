package com.example.smartcontract;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class callFunction extends AppCompatActivity {

    RecyclerView input;
    Button call;
    AdapterInput adapterInput;
    TextView outputResult;
    TextView inputTV, outputTV;
    JSONObject obj = null;
    List<Type> inputAsync;
    List<TypeReference<?>> outputAsync;
    String contractAddress;
    LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_function);
        input = findViewById(R.id.input);
        outputResult = findViewById(R.id.output);
        call = findViewById(R.id.call);
        inputTV = findViewById(R.id.inputTV);
        outputTV = findViewById(R.id.outputTV);
        animationView = findViewById(R.id.animationView);
        inputAsync = new ArrayList<>();
        outputAsync = new ArrayList<>();
        Intent intent = getIntent();
        String object = intent.getStringExtra("object");
        contractAddress = intent.getStringExtra("contractAddress");
        try {
            obj = new JSONObject(object);
            getSupportActionBar().setTitle(obj.optString("name"));
            if (obj.optString("stateMutability").equals("view")) {
                call.setText("READ");
                call.setBackgroundColor(0xFF2eb82e);
            } else {
                call.setText("WRITE");
                call.setBackgroundColor(0xFF3366ff);
            }
            List<JSONObject> inputs = new ArrayList<>();
            for (int i = 0; i < obj.optJSONArray("inputs").length(); i++) {
                inputs.add((JSONObject) obj.optJSONArray("inputs").get(i));
            }
            if (inputs.isEmpty()) {
                inputTV.setVisibility(View.GONE);
            }
            adapterInput = new AdapterInput(this, inputs);
            input.setAdapter(adapterInput);
            input.setLayoutManager(new LinearLayoutManager(this));

            List<JSONObject> outputs = new ArrayList<>();
            for (int i = 0; i < obj.optJSONArray("outputs").length(); i++) {
                outputs.add((JSONObject) obj.optJSONArray("outputs").get(i));
            }
            if (outputs.size() == 0) {
                outputTV.setVisibility(View.GONE);
                outputResult.setVisibility(View.GONE);
            }
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputAsync.clear();
                    outputAsync.clear();
                    try {
                        for (int i = 0; i < inputs.size(); i++) {
                            View view = input.getChildAt(i);
                            if (view != null) {
                                TextView textView = (TextView) view.findViewById(R.id.value);
                                String text = textView.getText().toString();
                                switch (inputs.get(i).optString("type")) {
                                    case "string":
                                        inputAsync.add(new Utf8String(text));
                                        break;
                                    case "address":
                                        inputAsync.add(new Address(text));
                                        break;
                                    case "uint256":
                                        inputAsync.add(new Uint256((long) Double.parseDouble(text)));
                                        break;
                                    case "string[]":
                                        List<Utf8String> list = new ArrayList<>();
                                        String[] arr = text.split(",");
                                        for (int j = 0; j < arr.length; j++) {
                                            list.add(new Utf8String(arr[j].trim()));
                                        }
                                        inputAsync.add(new DynamicArray(Utf8String.class, list));
                                        break;
                                }
                            }
                        }
                        for (int i = 0; i < outputs.size(); i++) {
                            switch (outputs.get(i).optString("type")) {
                                case "string":
                                    outputAsync.add(new TypeReference<Utf8String>() {
                                    });
                                    break;
                                case "address":
                                    outputAsync.add(new TypeReference<Address>() {
                                    });
                                    break;
                                case "uint256":
                                    outputAsync.add(new TypeReference<Uint256>() {
                                    });
                                    break;
                                case "int256":
                                    outputAsync.add(new TypeReference<Int256>() {
                                    });
                                    break;
                                case "string[]":
                                    outputAsync.add(new TypeReference<DynamicArray<Utf8String>>() {
                                    });
                                    break;
                                case "address[]":
                                    outputAsync.add(new TypeReference<DynamicArray<Address>>() {
                                    });
                                    break;
                                case "tuple":
                                    outputAsync.add(new TypeReference<org.web3j.abi.datatypes.DynamicStruct>() {
                                    });
                                    break;
                            }
                        }
                        for (int i = 0; i < inputAsync.size(); i++) {
                            Log.d("Address Input: ", inputAsync.get(i).getValue().toString());
                        }
                        if (obj != null && obj.optString("stateMutability").equals("view")) {
                            read read = new read();
                            read.execute();
                        } else {
                            calculateGas calculateGas = new calculateGas();
                            calculateGas.execute();
                        }
                    } catch (Exception e) {
                        Toast.makeText(callFunction.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    class read extends AsyncTask<Void, Void, String> {
        final KProgressHUD progressHUD = KProgressHUD.create(callFunction.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideKeyboard();
            progressHUD.show();
        }

        protected String doInBackground(Void... params) {
            String result = "";
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                Function function = new Function(obj.optString("name"), // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    for (int i = 0; i < results.size(); i++) {
                        result += results.get(i).getValue().toString() + "\n";
                    }
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressHUD.dismiss();
            outputResult.setText(result);
        }
    }

    class calculateGas extends AsyncTask<Void, Void, String> {

        final KProgressHUD progressHUD = KProgressHUD.create(callFunction.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideKeyboard();
            progressHUD.show();
        }

        protected String doInBackground(Void... params) {
            String result = "";
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                Function function = new Function(obj.optString("name"), // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);

                Log.d("Address Public: ", credentials.getAddress() + "");
                Log.d("Address Encode: ", txData + "");
                Log.d("Address Contract: ", contractAddress + "");
                Transaction t = Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, txData);
                BigInteger val = web3j.ethEstimateGas(t).send().getAmountUsed();
                Log.d("Address Gas Used: ", val + "");
                Log.d("Address Gas Price: ", "" + 4.1 * 1e-9 + " Ether");
                Double txnFee = val.intValue() * 4.1 * 1e-9;
                DecimalFormat df = new DecimalFormat("#.##########");
                df.setRoundingMode(RoundingMode.CEILING);
                result = df.format(txnFee) + " Ether";
                Log.d("Address Fee: ", result);
            } catch (MessageDecodingException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(callFunction.this, "You don't have desired permissions!", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("Address Error: ",e.toString());
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String txnFee) {
            super.onPostExecute(txnFee);
            progressHUD.dismiss();
            if (!txnFee.isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(callFunction.this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(txnFee + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                write write = new write();
                                write.execute();
                            }
                        })
                        .setNegativeButton("Cancel", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        })
                        .build();
                mDialog.show();
            }
        }
    }

    class write extends AsyncTask<Void, Void, String> {

        final KProgressHUD progressHUD = KProgressHUD.create(callFunction.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD.show();
        }

        protected String doInBackground(Void... params) {
            String result = "";
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                Function function = new Function(obj.optString("name"), // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);
                Log.d("Address Encode W", txData);
                TransactionManager txManager = new FastRawTransactionManager(web3j, credentials);

                // Send transaction
                String txHash = txManager.sendTransaction(
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        contractAddress,
                        txData,
                        BigInteger.ZERO).getTransactionHash();

                TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                        web3j,
                        TransactionManager.DEFAULT_POLLING_FREQUENCY,
                        TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
                TransactionReceipt txReceipt = receiptProcessor.waitForTransactionReceipt(txHash);
                result = txHash;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (txReceipt.isStatusOK()) {
                            animationView.setAnimation(R.raw.success);
                        } else {
                            animationView.setAnimation(R.raw.error);
                        }
                        animationView.setVisibility(View.VISIBLE);
                        animationView.playAnimation();
                    }
                });
                Log.d("Address", txHash);
                // Wait for transaction to be mined
            } catch (Exception e) {
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressHUD.dismiss();
            if (!result.isEmpty()) {
                outputTV.setText("Transaction Hash: ");
                outputResult.setText(result + "\n\nhttps://rinkeby.etherscan.io/tx/" + result);
                outputTV.setVisibility(View.VISIBLE);
                outputResult.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}