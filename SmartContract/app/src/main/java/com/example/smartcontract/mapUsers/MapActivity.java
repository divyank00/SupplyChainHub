package com.example.smartcontract.mapUsers;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.smartcontract.Data;
import com.example.smartcontract.R;
import com.example.smartcontract.models.TrackModel;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
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

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity {

    ProgressBar productDetailsLoader;
    TextView title, lotIdTV, owner, state;
    ShimmerRecyclerView flow;
    LinearLayout productDetails, ownerClick;
    TaskRunner taskRunner;
    String contractAddress, productId, lotId;
    List<Address> userAddress;
    CheckBox checkBox;
    LinearLayout additionInfoLL;
    EditText finalAmt, privateAddress;
    CardView additionInfo;
    Button submit;
    boolean isPermitted;
    List<TrackModel> models;
    TrackUserAdapter adapter;
    TextView detailsError, trackError, pathTV;
    CardView pathCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        taskRunner = new TaskRunner();
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
        productId = intent.getStringExtra("productId");
        lotId = intent.getStringExtra("lotId");
        isPermitted = intent.getBooleanExtra("isPermitted", false);
        userAddress = new ArrayList<>();
        productDetails = findViewById(R.id.productDetails);
        ownerClick = findViewById(R.id.ownerClick);
        productDetailsLoader = findViewById(R.id.productDetailsLoader);
        title = findViewById(R.id.title);
        lotIdTV = findViewById(R.id.lotIdTV);
        owner = findViewById(R.id.owner);
        state = findViewById(R.id.state);
        flow = findViewById(R.id.flow);
        additionInfo = findViewById(R.id.additionInfo);
        checkBox = findViewById(R.id.checkBox);
        additionInfoLL = findViewById(R.id.additionInfoLL);
        finalAmt = findViewById(R.id.finalAmt);
        privateAddress = findViewById(R.id.privateAddress);
        submit = findViewById(R.id.button);
        models = new ArrayList<>();
        adapter = new TrackUserAdapter(MapActivity.this, models);
        flow.setLayoutManager(new LinearLayoutManager(MapActivity.this));
        flow.setAdapter(adapter);
        flow.showShimmerAdapter();
        detailsError = findViewById(R.id.detailsError);
        trackError = findViewById(R.id.trackError);
        pathTV = findViewById(R.id.pathTV);
        pathCard = findViewById(R.id.pathCard);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    additionInfoLL.setVisibility(View.VISIBLE);
                } else {
                    additionInfoLL.setVisibility(View.GONE);
                    finalAmt.setText("");
                    privateAddress.setText("");
                }
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (finalAmt.getText().toString().trim().isEmpty()) {
                        finalAmt.setError("Mandatory Field");
                    } else if (privateAddress.getText().toString().trim().isEmpty()) {
                        privateAddress.setError("Mandatory Field");
                    } else {
                        long amount = Long.parseLong(finalAmt.getText().toString());
                        executeSetProductFinalSellingPriceGas(productId, privateAddress.getText().toString().trim(), Long.toString(amount));
                    }
                } catch (Exception e) {
                    Toast.makeText(MapActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (productId != null) {
            title.setText("PRODUCT DETAILS");
            executeTrackProductByProductId();
        } else {
            title.setText("LOT DETAILS");
            executeTrackProductByLotId();
        }
    }

    private void executeTrackProductByProductId() {
        taskRunner.executeAsync(new trackProductByProductId(), result -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    lotIdTV.setText("Lot Id: " + result.getData().get(0).getValue());
                    List<Address> userAddresses = (List<Address>) result.getData().get(2).getValue();
                    List<Uint256> buyingPrices = (List<Uint256>) result.getData().get(3).getValue();
                    List<Uint256> sellingPrices = (List<Uint256>) result.getData().get(4).getValue();
                    List<Utf8String> transactionHashes = (List<Utf8String>) result.getData().get(3).getValue();
                    for (int i = 0; i < userAddresses.size(); i++) {
                        TrackModel model = new TrackModel(userAddresses.get(i).toString().trim(), buyingPrices.get(i).toString().trim(), sellingPrices.get(i).toString(), transactionHashes.get(i).toString());
                        models.add(model);
                    }
                    adapter.notifyDataSetChanged();
                    flow.hideShimmerAdapter();
                    if (userAddresses.isEmpty()) {
                        owner.setVisibility(View.GONE);
                    } else {
                        owner.setText("Current Owner: " + userAddresses.get(userAddresses.size() - 1).toString());
                        ownerClick.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("PublicAddress", userAddresses.get(userAddresses.size() - 1).toString());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(MapActivity.this, "Current Owner's Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        });
                    }
                    if (Data.userRoles.size() == userAddresses.size() && ((BigInteger) result.getData().get(1).getValue()).intValue() == Data.State.size() - 1) {
                        state.setText("(" + Data.State.get(((BigInteger) result.getData().get(1).getValue()).intValue()) + ")\nNote: This item can als0 be sold to a customer!");
                    } else {
                        state.setText("(" + Data.State.get(((BigInteger) result.getData().get(1).getValue()).intValue()) + ")");
                    }
                    productDetailsLoader.setVisibility(View.GONE);
                    detailsError.setVisibility(View.GONE);
                    productDetails.setVisibility(View.VISIBLE);
                    if (!isPermitted) {
                        additionInfo.setVisibility(View.VISIBLE);
                    }
//                    executeTrackPrices(result.getData().get(0).getValue().toString());
                }
            } else {
                detailsError.setText("Error: " + result.getErrorMsg());
                productDetailsLoader.setVisibility(View.GONE);
                detailsError.setVisibility(View.VISIBLE);
                pathCard.setVisibility(View.GONE);
            }
        });
    }

    private void executeTrackProductByLotId() {
        taskRunner.executeAsync(new trackProductByLotId(), result -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    if (((BigInteger) result.getData().get(1).getValue()).intValue() == 0) {
                        detailsError.setText("Lot is unidentified!");
                        productDetailsLoader.setVisibility(View.GONE);
                        detailsError.setVisibility(View.VISIBLE);
                        pathCard.setVisibility(View.GONE);
                    } else {
                        String productIds = result.getData().get(0).getValue().toString();
                        productIds = productIds.substring(1, productIds.length() - 1);
                        lotIdTV.setText("Products Ids: " + productIds);
                        List<Address> userAddresses = (List<Address>) result.getData().get(2).getValue();
                        List<Uint256> buyingPrices = (List<Uint256>) result.getData().get(3).getValue();
                        List<Uint256> sellingPrices = (List<Uint256>) result.getData().get(4).getValue();
                        List<Utf8String> transactionHashes = (List<Utf8String>) result.getData().get(3).getValue();
                        for (int i = 0; i < userAddresses.size(); i++) {
                            TrackModel model = new TrackModel(userAddresses.get(i).toString().trim(), buyingPrices.get(i).toString().trim(), sellingPrices.get(i).toString(), transactionHashes.get(i).toString());
                            models.add(model);
                        }
                        adapter.notifyDataSetChanged();
                        flow.hideShimmerAdapter();
                        if (userAddresses.isEmpty()) {
                            owner.setVisibility(View.GONE);
                        } else {
                            owner.setText("Current Owner: " + userAddresses.get(userAddresses.size() - 1).toString());
                            ownerClick.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("PublicAddress", userAddresses.get(userAddresses.size() - 1).toString());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(MapActivity.this, "Current Owner's Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            });
                        }
                        Toast.makeText(this, result.getData().get(1).getValue().toString(), Toast.LENGTH_SHORT).show();
                        if (Data.userRoles.size() == userAddresses.size() && ((int) result.getData().get(1).getValue()) == Data.State.size() - 1) {
                            state.setText("(" + Data.State.get(((BigInteger) result.getData().get(1).getValue()).intValue()) + ")\nNote: This item can be sold by now!");
                        } else {
                            state.setText("(" + Data.State.get(((BigInteger) result.getData().get(1).getValue()).intValue()) + ")");
                        }
                        productDetailsLoader.setVisibility(View.GONE);
                        detailsError.setVisibility(View.GONE);
                        productDetails.setVisibility(View.VISIBLE);
//                        executeTrackPrices(lotId);
                    }
                }
            } else {
                detailsError.setText("Error: " + result.getErrorMsg());
                productDetailsLoader.setVisibility(View.GONE);
                detailsError.setVisibility(View.VISIBLE);
                pathCard.setVisibility(View.GONE);
            }
        });
    }

//    private void executeTrackPrices(String lotId) {
//        taskRunner.executeAsync(new trackPrices(lotId), (result) -> {
//            if (result.isStatus()) {
//                if (!result.getData().isEmpty()) {
//                    List<Uint256> buyingPrices = (List<Uint256>) result.getData().get(0).getValue();
//                    List<Uint256> sellingPrices = (List<Uint256>) result.getData().get(1).getValue();
//                    for (int i = 0; i < models.size(); i++) {
//                        models.get(i).setBuyingPrice(buyingPrices.get(i).toString());
//                        models.get(i).setSellingPrice(sellingPrices.get(i).toString());
//                    }
//                    adapter.notifyDataSetChanged();
//                    flow.hideShimmerAdapter();
//                }
//            } else {
//                trackError.setText("Error: " + result.getErrorMsg());
//                trackError.setVisibility(View.VISIBLE);
//                flow.setVisibility(View.GONE);
//                pathTV.setVisibility(View.GONE);
//            }
//        });
//    }

    private void executeGetUserDetails(String trackAddress) {
        taskRunner.executeAsync(new getUserDetails(trackAddress), result -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {

                }
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void executeSetProductFinalSellingPriceGas(String productId, String privateAddress, String amount) {
        final KProgressHUD progressHUD = KProgressHUD.create(MapActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new setProductFinalSellingPriceGas(productId, privateAddress, amount), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(MapActivity.this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(result.getData() + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                executeSetProductFinalSellingPrice(productId, privateAddress, amount);
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

    private void executeSetProductFinalSellingPrice(String productId, String privateAddress, String amount) {
        final KProgressHUD progressHUD = KProgressHUD.create(MapActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new setProductFinalSellingPrice(productId, privateAddress, amount), (result) -> {
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

        public <R> void executeAsync(Callable<R> callable, TaskRunner.Callback<R> callback) {
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

    class trackProductByProductId implements Callable<Object> {

        @Override
        public Object call() throws Exception {
            Object result = new Object();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(productId));
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Uint256>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Address>>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Utf8String>>() {
                });
                Function function = new Function("trackProductByProductId", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                result = new Object(false, null, e.toString());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class trackProductByLotId implements Callable<Object> {

        @Override
        public Object call() throws Exception {
            Object result = new Object();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(lotId));
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<DynamicArray<Utf8String>>() {
                });
                outputAsync.add(new TypeReference<Uint256>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Address>>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Utf8String>>() {
                });
                Function function = new Function("trackProductByLotId", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    Log.d("Address Decode", FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()).toString());
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                result = new Object(false, null, e.toString());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

//    class trackPrices implements Callable<Object> {
//
//        String lotId;
//
//        public trackPrices(String lotId) {
//            this.lotId = lotId;
//        }
//
//        @Override
//        public Object call() throws Exception {
//            Object result = new Object();
//            try {
//                // Connect to the node
//                System.out.println("Connecting to Ethereum ...");
//                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));
//
//                // Contract and functions
//                List<Type> inputAsync = new ArrayList<>();
//                inputAsync.add(new Utf8String(lotId));
//                List<TypeReference<?>> outputAsync = new ArrayList<>();
//                outputAsync.add(new TypeReference<DynamicArray<Uint256>>() {
//                });
//                outputAsync.add(new TypeReference<DynamicArray<Uint256>>() {
//                });
//
//                Function function = new Function("trackPrices", // Function name
//                        inputAsync,  // Function input parameters
//                        outputAsync); // Function returned parameters
//                Log.d("Address Output: ", outputAsync.size() + "");
//                String encodedFunction = FunctionEncoder.encode(function);
//                EthCall ethCall = web3j.ethCall(
//                        Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, encodedFunction),
//                        DefaultBlockParameterName.LATEST)
//                        .sendAsync().get();
//                if (!ethCall.isReverted()) {
//                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
//                } else {
//                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
//                }
//            } catch (Exception e) {
//                result = new Object(false, null, e.toString());
//                Log.d("Address Error: ", e.toString());
//                e.printStackTrace();
//            }
//            return result;
//        }
//    }

    class getUserDetails implements Callable<Object> {

        String addressTemp;

        getUserDetails(String add) {
            addressTemp = add;
        }

        @Override
        public Object call() throws Exception {
            Object result = new Object();

            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Address(addressTemp));
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<Uint256>() {
                });
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Address>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Address>>() {
                });
                outputAsync.add(new TypeReference<Uint256>() {
                });
                Function function = new Function("getUserDetails", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                result = new Object(false, null, e.toString());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class setProductFinalSellingPriceGas implements Callable<WriteObject> {

        String productId, privateKey, amount;

        public setProductFinalSellingPriceGas(String productId, String privateKey, String amount) {
            this.productId = productId;
            this.privateKey = privateKey;
            this.amount = amount;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(productId));
                inputAsync.add(new Uint256(Long.parseLong(amount)));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("setProductFinalSellingPrice", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String txData = FunctionEncoder.encode(function);

                Transaction t = Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, txData);
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

    class setProductFinalSellingPrice implements Callable<WriteObject> {

        String productId, privateKey, amount;

        public setProductFinalSellingPrice(String productId, String privateKey, String amount) {
            this.productId = productId;
            this.privateKey = privateKey;
            this.amount = amount;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                Credentials credentials = Credentials.create(privateKey);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(productId));
                inputAsync.add(new Uint256(Long.parseLong(amount)));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("setProductFinalSellingPrice", // Function name
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

    class Object {
        private List<Type> data;
        private boolean status;

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public Object() {
        }

        public List<Type> getData() {
            return data;
        }

        public void setData(List<Type> data) {
            this.data = data;
        }

        public Object(boolean status, List<Type> data, String errorMsg) {
            this.data = data;
            this.status = status;
            this.errorMsg = errorMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        private String errorMsg;
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
                txnUrl.setText("https://rinkeby.etherscan.io/tx/" + txnHash);
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
                    Toast.makeText(MapActivity.this, "Transaction Hash copied to clipboard!", Toast.LENGTH_SHORT).show();
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

}