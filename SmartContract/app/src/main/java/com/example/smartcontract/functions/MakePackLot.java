package com.example.smartcontract.functions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.smartcontract.Data;
import com.example.smartcontract.R;
import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.viewModel.ProductLotViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MakePackLot extends AppCompatActivity {

    String contractAddress;
    TaskRunner taskRunner;
    EditText lotIdMade, productIds, lotIdPack, lotIdsSale, unitPrice;
    Button made, pack, sale;
    ImageView lotIdIV, productIdsIV, lotIdPackIV, lotIdsSaleIV;
    private IntentIntegrator qrScanLotId, qrScanProductIds, qrScanLotIdPack, qrScanLotIdsSale;
    boolean trackByProductId, trackByLotId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_lot);
        getSupportActionBar().setTitle("New Lots");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
        trackByProductId = intent.getBooleanExtra("trackByProductId", true);
        trackByLotId = intent.getBooleanExtra("trackByLotId", true);
        taskRunner = new TaskRunner();
        lotIdMade = findViewById(R.id.lotId);
        productIds = findViewById(R.id.productIds);
        lotIdPack = findViewById(R.id.lotIdPack);
        made = findViewById(R.id.button);
        pack = findViewById(R.id.packBtn);
        sale = findViewById(R.id.putBtn);
        lotIdIV = findViewById(R.id.scanLotId);
        lotIdsSale = findViewById(R.id.lotIdsSale);
        unitPrice = findViewById(R.id.unitPrice);
        productIdsIV = findViewById(R.id.scanProductIds);
        lotIdPackIV = findViewById(R.id.scanLotIdPack);
        lotIdsSaleIV = findViewById(R.id.lotIdsSaleIV);
        qrScanLotId = new IntentIntegrator(this).setRequestCode(1);
        qrScanProductIds = new IntentIntegrator(this).setRequestCode(2);
        qrScanLotIdPack = new IntentIntegrator(this).setRequestCode(3);
        qrScanLotIdsSale = new IntentIntegrator(this).setRequestCode(4);
        lotIdIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanLotId.initiateScan();
            }
        });
        productIdsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanProductIds.initiateScan();
            }
        });
        lotIdPackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanLotIdPack.initiateScan();
            }
        });
        lotIdsSaleIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanLotIdsSale.initiateScan();
            }
        });
        made.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lotIdMade.setError(null);
                productIds.setError(null);
                if (lotIdMade.getText().toString().trim().isEmpty()) {
                    lotIdMade.setError("Mandatory Field");
                } else if (productIds.getText().toString().trim().isEmpty()) {
                    productIds.setError("Mandatory Field");
                } else {
                    executeMakeLotGas(lotIdMade.getText().toString().trim(), productIds.getText().toString().trim());
                }
            }
        });
        pack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lotIdPack.setError(null);
                if (lotIdPack.getText().toString().trim().isEmpty()) {
                    lotIdPack.setError("Mandatory Field");
                } else {
                    executePackLotGas(lotIdPack.getText().toString().trim());
                }
            }
        });
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lotIdsSale.setError(null);
                unitPrice.setError(null);
                if (lotIdsSale.getText().toString().trim().isEmpty()) {
                    lotIdsSale.setError("Mandatory Field");
                } else if(unitPrice.getText().toString().trim().isEmpty()){
                    unitPrice.setError("Mandatory Field");
                }else {
                    executeForSaleLotByManufacturerGas(lotIdsSale.getText().toString().trim(),unitPrice.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                lotIdMade.setText(contents);
                lotIdMade.setSelection(lotIdMade.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                String productIdsET = productIds.getText().toString();
                if (productIdsET.trim().isEmpty()) {
                    productIdsET += contents;
                } else {
                    if (productIdsET.trim().charAt(productIdsET.trim().length() - 1) == ',') {
                        productIdsET += contents;
                    } else {
                        productIdsET += ", " + contents;
                    }
                }
                productIds.setText(productIdsET);
                productIds.setSelection(productIds.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                lotIdPack.setText(contents);
                lotIdPack.setSelection(lotIdPack.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                String lotIdsET = lotIdsSale.getText().toString();
                if (lotIdsET.trim().isEmpty()) {
                    lotIdsET += contents;
                } else {
                    if (lotIdsET.trim().charAt(lotIdsET.trim().length() - 1) == ',') {
                        lotIdsET += contents;
                    } else {
                        lotIdsET += ", " + contents;
                    }
                }
                lotIdsSale.setText(lotIdsET);
                lotIdsSale.setSelection(productIds.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void executeMakeLotGas(String lotId, String productIds) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new makeLotGas(lotId, productIds), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(result.getData() + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                executeMakeLot(lotId, productIds);
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
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeMakeLot(String lotId, String productIds) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new makeLot(lotId, productIds), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus()) {
                showTransactionDialog(true, result.getData(), null);
            } else {
                showTransactionDialog(false, result.getData(), result.getErrorMsg());
            }
        });
    }

    private void executePackLotGas(String lotId) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new packLotGas(lotId), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(result.getData() + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                executePackLot(lotId);
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
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executePackLot(String lotId) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new packLot(lotId), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus()) {
                showTransactionDialog(true, result.getData(), null);
            } else {
                showTransactionDialog(false, result.getData(), result.getErrorMsg());
            }
        });
    }

    private void executeForSaleLotByManufacturerGas(String lotIds, String price) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new forSaleLotByManufacturerGas(lotIds, price), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(result.getData() + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                executeForSaleLotByManufacturer(lotIds, price);
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
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeForSaleLotByManufacturer(String lotIds, String price) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new forSaleLotByManufacturer(lotIds, price), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus()) {
                showTransactionDialog(true, result.getData(), null);
            } else {
                showTransactionDialog(false, result.getData(), result.getErrorMsg());
            }
        });
    }

    public static class TaskRunner {
        //        private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Executor executor = new ThreadPoolExecutor(5, 128, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        private final Handler handler = new Handler(Looper.getMainLooper());

        public interface Callback<R> {
            void onComplete(R result);
        }

        public <R> void executeAsync(Callable<R> callable, AddUser.TaskRunner.Callback<R> callback) {
            executor.execute(() -> {
                try {
                    final R result = callable.call();
                    handler.post(() -> {
                        callback.onComplete(result);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    class makeLotGas implements Callable<WriteObject> {

        String lotId, productIds;

        public makeLotGas(String lotId, String productIds) {
            this.lotId = lotId;
            this.productIds = productIds;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(lotId));
                List<Utf8String> list = new ArrayList<>();
                list.add(new Utf8String("Garbage"));
                String[] arr = productIds.split(",");
                for (int j = 0; j < arr.length; j++) {
                    if (!arr[j].trim().isEmpty())
                        list.add(new Utf8String(arr[j].trim()));
                }
                inputAsync.add(new DynamicArray(Utf8String.class, list));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("makeLot", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);

                Transaction t = Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, txData);
                BigInteger val = web3j.ethEstimateGas(t).send().getAmountUsed();
                Log.d("Address Gas Used: ", val + "");
                Log.d("Address Gas Price: ", "" + 4.1 * 1e-9 + " Ether");
                Double txnFee = val.intValue() * 4.1 * 1e-9;
                DecimalFormat df = new DecimalFormat("#.##########");
                df.setRoundingMode(RoundingMode.CEILING);
                result = new WriteObject(true, df.format(txnFee) + " Ether", null);
            } catch (Exception e) {
                result = new WriteObject(false, null, "You don't have required permissions!");
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class makeLot implements Callable<WriteObject> {

        String lotId, productIds;

        public makeLot(String lotId, String productIds) {
            this.lotId = lotId;
            this.productIds = productIds;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(lotId));
                List<Utf8String> list = new ArrayList<>();
                list.add(new Utf8String("Garbage"));
                List<String> firebaseList = new ArrayList<>();
                String[] arr = productIds.split(",");
                for (int j = 0; j < arr.length; j++) {
                    if (!arr[j].trim().isEmpty()) {
                        firebaseList.add(arr[j].trim());
                        list.add(new Utf8String(arr[j].trim()));
                    }
                }
                inputAsync.add(new DynamicArray(Utf8String.class, list));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Map<String, String> map = new HashMap<>();
                if (trackByLotId)
                    map.put(lotId, contractAddress);
                if (trackByProductId) {
                    for (String prodId : firebaseList) {
                        map.put(prodId, contractAddress);
                    }
                }

                Function function = new Function("makeLot", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);
                TransactionManager txManager = new FastRawTransactionManager(web3j, credentials);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProductLotViewModel productLotViewModel = new ProductLotViewModel();
                        productLotViewModel.addMap(map).observe(MakePackLot.this, new Observer<ObjectModel>() {
                            @Override
                            public void onChanged(ObjectModel objectModel) {

                            }
                        });
                    }
                });
                if (txReceipt.isStatusOK()) {
                    result = new WriteObject(true, txHash, null);
                } else {
                    result = new WriteObject(false, txHash, txReceipt.getRevertReason());
                }
            } catch (Exception e) {
                result = new WriteObject(false, null, e.getMessage());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class packLotGas implements Callable<WriteObject> {

        String lotId;

        public packLotGas(String lotId) {
            this.lotId = lotId;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(lotId));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("packLot", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);

                Transaction t = Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, txData);
                BigInteger val = web3j.ethEstimateGas(t).send().getAmountUsed();
                Log.d("Address Gas Used: ", val + "");
                Log.d("Address Gas Price: ", "" + 4.1 * 1e-9 + " Ether");
                Double txnFee = val.intValue() * 4.1 * 1e-9;
                DecimalFormat df = new DecimalFormat("#.##########");
                df.setRoundingMode(RoundingMode.CEILING);
                result = new WriteObject(true, df.format(txnFee) + " Ether", null);
            } catch (Exception e) {
                result = new WriteObject(false, null, "You don't have required permissions!");
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class packLot implements Callable<WriteObject> {

        String lotId;

        public packLot(String lotId) {
            this.lotId = lotId;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(lotId));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("packLot", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);
                TransactionManager txManager = new FastRawTransactionManager(web3j, credentials);
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
                if (txReceipt.isStatusOK()) {
                    result = new WriteObject(true, txHash, null);
                } else {
                    Log.d("Address Revert: ", txReceipt.getRevertReason());
                    result = new WriteObject(false, txHash, txReceipt.getRevertReason());
                }
            } catch (Exception e) {
                result = new WriteObject(false, null, e.getMessage());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class forSaleLotByManufacturerGas implements Callable<WriteObject> {

        String lotIds, price;

        public forSaleLotByManufacturerGas(String lotIds, String price) {
            this.lotIds = lotIds;
            this.price = price;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<Utf8String> list = new ArrayList<>();
                String[] arr = lotIds.split(",");
                for (int j = 0; j < arr.length; j++) {
                    if (!arr[j].trim().isEmpty())
                        list.add(new Utf8String(arr[j].trim()));
                }
                inputAsync.add(new DynamicArray(Utf8String.class, list));
                inputAsync.add(new Uint256(Long.parseLong(price)));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("forSaleLotByManufacturer", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);

                Transaction t = Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, txData);
                BigInteger val = web3j.ethEstimateGas(t).send().getAmountUsed();
                Log.d("Address Gas Used: ", val + "");
                Log.d("Address Gas Price: ", "" + 4.1 * 1e-9 + " Ether");
                Double txnFee = val.intValue() * 4.1 * 1e-9;
                DecimalFormat df = new DecimalFormat("#.##########");
                df.setRoundingMode(RoundingMode.CEILING);
                result = new WriteObject(true, df.format(txnFee) + " Ether", null);
            } catch (Exception e) {
                result = new WriteObject(false, null, "You don't have required permissions!");
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class forSaleLotByManufacturer implements Callable<WriteObject> {

        String lotIds, price;

        public forSaleLotByManufacturer(String lotIds, String price) {
            this.lotIds = lotIds;
            this.price = price;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<Utf8String> list = new ArrayList<>();
                String[] arr = lotIds.split(",");
                for (int j = 0; j < arr.length; j++) {
                    if (!arr[j].trim().isEmpty())
                        list.add(new Utf8String(arr[j].trim()));
                }
                inputAsync.add(new DynamicArray(Utf8String.class, list));
                inputAsync.add(new Uint256(Long.parseLong(price)));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("forSaleLotByManufacturer", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters

                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);
                TransactionManager txManager = new FastRawTransactionManager(web3j, credentials);
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
                if (txReceipt.isStatusOK()) {
                    result = new WriteObject(true, txHash, null);
                } else {
                    result = new WriteObject(false, txHash, txReceipt.getRevertReason());
                }
            } catch (Exception e) {
                result = new WriteObject(false, null, e.getMessage());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class WriteObject {
        private boolean status;

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public WriteObject() {
        }

        public WriteObject(boolean status, String data, String errorMsg) {
            this.status = status;
            this.data = data;
            this.errorMsg = errorMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        private String errorMsg;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        private String data;
    }

    private void showTransactionDialog(boolean successful, String txnHash, String errorMsg) {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.transaction_dialog);
            ImageView txnHashCopy = dialog.findViewById(R.id.txnHashCopy);
            TextView txnHashTV = dialog.findViewById(R.id.txnHash);
            TextView txnUrl = dialog.findViewById(R.id.txnUrl);
            TextView txnError = dialog.findViewById(R.id.txnError);
            LottieAnimationView animationView = dialog.findViewById(R.id.animationView);
            if (txnHash != null && !txnHash.isEmpty()) {
                txnHashTV.setText(txnHash);
                txnUrl.setText(getResources().getString(R.string.txnEndpoint) + txnHash);
            } else {
                txnHashCopy.setVisibility(View.GONE);
                txnHashTV.setVisibility(View.GONE);
                txnUrl.setVisibility(View.GONE);
            }
            if (successful) {
                animationView.setAnimation(R.raw.success);
                txnError.setVisibility(View.GONE);
            } else {
                animationView.setAnimation(R.raw.error);
                txnError.setText("Error: " + errorMsg != null ? errorMsg : "Something went wrong!");
                txnError.setVisibility(View.VISIBLE);
            }
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
            txnHashCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PublicAddress", txnHash);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MakePackLot.this, "Transaction Hash copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.d("Address Error", e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}