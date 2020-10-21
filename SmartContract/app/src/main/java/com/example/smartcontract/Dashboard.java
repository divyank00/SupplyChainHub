package com.example.smartcontract;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.smartcontract.functions.AddUser;
import com.example.smartcontract.functions.GetUserDetails;
import com.example.smartcontract.functions.MakePackLot;
import com.example.smartcontract.mapUsers.MapActivity;
import com.example.smartcontract.models.ListenerModel;
import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.models.SingleContractModel;
import com.example.smartcontract.oldCode.Adapter;
import com.example.smartcontract.oldCode.AllFunctions;
import com.example.smartcontract.viewModel.ProductLotViewModel;
import com.example.smartcontract.viewModel.SingleContractViewModel;
import com.google.android.gms.maps.model.Dash;
import com.google.zxing.integration.android.IntentIntegrator;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.json.JSONArray;
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
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
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

public class Dashboard extends AppCompatActivity {

    ProgressBar productDetailsLoader, userDetailsLoader;
    LinearLayout productDetails, ownerClick, userDetails, parentClick, ownerRoleLinearLayout;
    TextView companyName, productName, productCategory, owner, userRole, userParent, userChildren, currentQuantity, ownerText;
    SwitchCompat ownerRole;
    String contractAddress, contractOwnerAddress;
    TaskRunner taskRunner;
    DashboardAdapter dashboardAdapter;
    List<ListenerModel> listenerModelList;
    ShimmerRecyclerView userFunctions;
    int userRoleInt;
    boolean toggleFlag = true;

    IntentIntegrator qrScanLotId, qrScanProductId;
    EditText productId, lotId;

    String abi;

    boolean trackByProductId = false;
    boolean trackByLotId = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Intent intent = getIntent();
        setGlobal(intent);
        getContract();
    }

    private void setGlobal(Intent intent) {
        taskRunner = new TaskRunner();
        contractAddress = intent.getStringExtra("contractAddress");
        productDetailsLoader = findViewById(R.id.productDetailsLoader);
        productDetails = findViewById(R.id.productDetails);
        companyName = findViewById(R.id.companyName);
        productName = findViewById(R.id.productName);
        productCategory = findViewById(R.id.productCategory);
        owner = findViewById(R.id.owner);
        ownerClick = findViewById(R.id.ownerClick);
        userDetailsLoader = findViewById(R.id.userDetailsLoader);
        userDetails = findViewById(R.id.userDetails);
        parentClick = findViewById(R.id.parentClick);
        userRole = findViewById(R.id.userRole);
        userParent = findViewById(R.id.userParent);
        userChildren = findViewById(R.id.userChildren);
        currentQuantity = findViewById(R.id.currentQuantity);
        ownerText = findViewById(R.id.ownerText);
        ownerRole = findViewById(R.id.ownerRole);
        ownerRoleLinearLayout = findViewById(R.id.ownerRoleLinearLayout);
        userFunctions = findViewById(R.id.userFunctions);
        listenerModelList = new ArrayList<>();
        dashboardAdapter = new DashboardAdapter(Dashboard.this, listenerModelList);
        userFunctions.setAdapter(dashboardAdapter);
        userFunctions.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        userFunctions.showShimmerAdapter();
    }

    private void getContract() {
        SingleContractViewModel singleContractViewModel = new SingleContractViewModel();
        singleContractViewModel.getContract(contractAddress).observe(this, new Observer<ObjectModel>() {
            @Override
            public void onChanged(ObjectModel objectModel) {
                if (objectModel.isStatus()) {
                    if (objectModel.getObj() != null) {
                        try {
                            abi = ((SingleContractModel) objectModel.getObj()).getAbi();
                            JSONArray obj = new JSONArray(abi);
                            List<JSONObject> functions = new ArrayList<>();
                            for (int i = 0; i < obj.length(); i++) {
                                if (((JSONObject) obj.get(i)).optString("type").equals("function")) {
                                    if(((JSONObject) obj.get(i)).optString("name").equals("trackProductByProductId"))
                                        trackByProductId = true;
                                    if(((JSONObject) obj.get(i)).optString("name").equals("trackProductByLotId"))
                                        trackByLotId = true;
                                    functions.add((JSONObject) obj.get(i));
                                }
                            }
                            executeGetUserRolesArray();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(Dashboard.this, "Smart-Contract doesn't exist!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Dashboard.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void executeGetUserRolesArray() {
        taskRunner.executeAsync(new getUserRolesArray(), (result) -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    Data.userRoles.clear();
                    List<Utf8String> roles = (List<Utf8String>) result.getData().get(0).getValue();
                    Data.userRoles.add("Owner");
                    for (int i = 1; i < roles.size(); i++) {
                        Data.userRoles.add(roles.get(i).toString());
                    }
                    Log.d("Address Roles", Data.userRoles.toString() + "");
                    executeGetSmartContractDetails();
                }
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeGetSmartContractDetails() {
        taskRunner.executeAsync(new getSmartContractDetails(), (result) -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    companyName.setText(result.getData().get(0).getValue().toString());
                    productName.setText(result.getData().get(1).getValue().toString());
                    if (!result.getData().get(2).getValue().toString().trim().isEmpty()) {
                        productCategory.setText("(" + result.getData().get(2).getValue().toString() + ")");
                    } else {
                        productCategory.setVisibility(View.GONE);
                    }
                    contractOwnerAddress = result.getData().get(4).getValue().toString();
                    owner.setText("Owner: " + result.getData().get(3).getValue().toString() + "\n(" + contractOwnerAddress + ")");
                    ownerClick.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("PublicAddress", result.getData().get(4).getValue().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(Dashboard.this, "Owner Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    ownerClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("PublicAddress", result.getData().get(4).getValue().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(Dashboard.this, "Owner Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    productDetailsLoader.setVisibility(View.GONE);
                    productDetails.setVisibility(View.VISIBLE);
                    executeGetUserDetails();
                }
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void executeGetUserDetails() {
        taskRunner.executeAsync(new getUserDetails(), (result) -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    if (result.getData().get(1).getValue().toString().isEmpty()) {
                        setProfileDialog();
                    }
                    userRoleInt = Integer.parseInt(result.getData().get(0).getValue().toString());
                    if (contractOwnerAddress != null && contractOwnerAddress.equals(Data.publicKey)) {
                        userRole.setText("You are the owner!");
                        ownerText.setText("Are you a " + Data.userRoles.get(1).toString() + " also?");
                        ownerText.setVisibility(View.VISIBLE);
                        ownerRoleLinearLayout.setVisibility(View.VISIBLE);
                        parentClick.setVisibility(View.GONE);
                        if (userRoleInt != 0) {
                            ownerRole.setChecked(true);
                            ownerRole.setEnabled(false);
                            currentQuantity.setText("Quantity: " + result.getData().get(6).getValue().toString());
                        } else {
                            ownerRole.setChecked(false);
                            ownerRole.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked && toggleFlag) {
                                        ownerRole.setChecked(false);
                                        executeMakeOwnerAsNextRoleGas();
                                    }
                                }
                            });
                            currentQuantity.setVisibility(View.GONE);
                        }
                    } else {
                        userRole.setText("You are a " + Data.userRoles.get(userRoleInt) + "!");
                        currentQuantity.setText("Quantity: " + result.getData().get(6).getValue().toString());
                        if (userRoleInt == 1) {
                            parentClick.setVisibility(View.GONE);
                        } else {
                            userParent.setText("Your " + Data.userRoles.get(userRoleInt - 1) + ":\n" + result.getData().get(4).getValue().toString());
                            parentClick.setVisibility(View.VISIBLE);
                            parentClick.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("PublicAddress", result.getData().get(4).getValue().toString());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(Dashboard.this, Data.userRoles.get(userRoleInt - 1) + " Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            });
                        }
                    }
                    if (userRoleInt == Data.userRoles.size() - 1) {
                        userChildren.setVisibility(View.GONE);
                    } else {
                        String childAdd = result.getData().get(5).getValue().toString();
                        childAdd = childAdd.substring(1, childAdd.length() - 1);
                        if (childAdd.isEmpty()) {
                            userChildren.setVisibility(View.GONE);
                        } else {
                            String text = Data.userRoles.get(userRoleInt + 1) + "s: ";
                            String[] arr = childAdd.split(",");
                            for (int i = 0; i < arr.length - 1; i++) {
                                text += arr[i].trim() + ",\n";
                            }
                            text += arr[arr.length - 1];
                            userChildren.setText(text);
                            userChildren.setVisibility(View.VISIBLE);
                        }
                    }
                    userDetailsLoader.setVisibility(View.GONE);
                    userDetails.setVisibility(View.VISIBLE);
                    if (userRoleInt < Data.userRoles.size() - 1) {
                        listenerModelList.add(new ListenerModel("Add User", "You can add user in the Smart-Contract!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), AddUser.class);
                                if (contractOwnerAddress.equals(Data.publicKey)) {
                                    intent.putExtra("userRole", 0);
                                } else {
                                    intent.putExtra("userRole", userRoleInt);
                                }
                                intent.putExtra("contractAddress", contractAddress);
                                startActivity(intent);
                            }
                        }));
                    }
                    listenerModelList.add(new ListenerModel("Get User Details", "You can get details of any user in the Smart-Contract!", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), GetUserDetails.class);
                            intent.putExtra("contractAddress", contractAddress);
                            startActivity(intent);
                        }
                    }));
                    if (userRoleInt == 1) {
                        listenerModelList.add(new ListenerModel("Add & Pack Lots", "You have to add the lots which have just been made and update once they are packed!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), MakePackLot.class);
                                intent.putExtra("contractAddress", contractAddress);
                                intent.putExtra("trackByProductId",trackByProductId);
                                intent.putExtra("trackByLotId",trackByLotId);
                                startActivity(intent);
                            }
                        }));
                    }
                    listenerModelList.add(new ListenerModel("Other Functions", "You can call rest of the available functions from here!", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), AllFunctions.class);
                            intent.putExtra("contractAddress", contractAddress);
                            intent.putExtra("abi", abi);
                            startActivity(intent);
                        }
                    }));
                    userFunctions.hideShimmerAdapter();
                    dashboardAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void executeMakeOwnerAsNextRoleGas() {
        final KProgressHUD progressHUD = KProgressHUD.create(Dashboard.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new makeOwnerAsNextRoleGas(), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(Dashboard.this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(result.getData() + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                executeMakeOwnerAsNextRole();
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

    private void executeMakeOwnerAsNextRole() {
        final KProgressHUD progressHUD = KProgressHUD.create(Dashboard.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new makeOwnerAsNextRole(), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus()) {
                listenerModelList.add(2, new ListenerModel("Add & Pack Lots", "You have to add the lots which have just been made and update once they are packed!", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MakePackLot.class);
                        intent.putExtra("contractAddress", contractAddress);
                        startActivity(intent);
                    }
                }));
                dashboardAdapter.notifyDataSetChanged();
                toggleFlag = false;
                ownerRole.setEnabled(false);
                ownerRole.setChecked(true);
                showTransactionDialog(true, result.getData(), null);
            } else {
                showTransactionDialog(false, result.getData(), result.getErrorMsg());
            }
        });
    }

    private void executeSetUserDetailsGas(String name, String lat, String lon, Dialog dialog) {
        final KProgressHUD progressHUD = KProgressHUD.create(Dashboard.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new setUserDetailsGas(name, lat, lon), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                MaterialDialog mDialog = new MaterialDialog.Builder(Dashboard.this)
                        .setTitle("Confirm Transaction?")
                        .setMessage(result.getData() + " will be deducted!")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                executeSetUserDetails(name, lat, lon, dialog);
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

    private void executeSetUserDetails(String name, String lat, String lon, Dialog dialog) {
        final KProgressHUD progressHUD = KProgressHUD.create(Dashboard.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new setUserDetails(name, lat, lon), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus()) {
                dialog.dismiss();
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

        public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
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

    class getUserRolesArray implements Callable<Object> {

        @Override
        public Object call() {
            Object result = new Object();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<DynamicArray<Utf8String>>() {
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
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                Toast.makeText(Dashboard.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class getSmartContractDetails implements Callable<Object> {

        @Override
        public Object call() {
            Object result = new Object();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
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
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                Toast.makeText(Dashboard.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class getUserDetails implements Callable<Object> {

        @Override
        public Object call() {
            Object result = new Object();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Address(Data.publicKey));
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
                        Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                Toast.makeText(Dashboard.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class makeOwnerAsNextRoleGas implements Callable<WriteObject> {

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("makeOwnerAsNextRole", // Function name
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

    class makeOwnerAsNextRole implements Callable<WriteObject> {

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("makeOwnerAsNextRole", // Function name
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

    class setUserDetailsGas implements Callable<WriteObject> {

        String name, lat, lon;

        public setUserDetailsGas(String name, String lat, String lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(name));
                inputAsync.add(new Utf8String(lat));
                inputAsync.add(new Utf8String(lon));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("setUserDetails", // Function name
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
                result = new WriteObject(false, null, e.getMessage());
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    class setUserDetails implements Callable<WriteObject> {

        String name, lat, lon;

        public setUserDetails(String name, String lat, String lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public WriteObject call() {
            WriteObject result = new WriteObject();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(name));
                inputAsync.add(new Utf8String(lat));
                inputAsync.add(new Utf8String(lon));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("setUserDetails", // Function name
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
                    Toast.makeText(Dashboard.this, "Transaction Hash copied to clipboard!", Toast.LENGTH_SHORT).show();
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

    private void setProfileDialog() {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.set_profile_dialog);
            EditText name = dialog.findViewById(R.id.name);
            EditText lat = dialog.findViewById(R.id.lat);
            EditText lon = dialog.findViewById(R.id.lon);
            Button confirm = dialog.findViewById(R.id.button);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().trim().isEmpty()) {
                        name.setError("Mandatory Field!");
                    } else if (lat.getText().toString().isEmpty()) {
                        lat.setError("Mandatory Field!");
                    } else if (lon.getText().toString().isEmpty()) {
                        lon.setError("Mandatory Field!");
                    } else {
                        executeSetUserDetailsGas(name.getText().toString(), lat.getText().toString(), lon.getText().toString(), dialog);
                    }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOut:
                showLogoutDialog();
                return true;
            case R.id.profile:
                showProfileDialog();
                return true;
            case R.id.scan:
                showBarcodeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLogoutDialog() {
        MaterialDialog mDialog = new MaterialDialog.Builder(Dashboard.this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setCancelable(true)
                .setPositiveButton("Confirm", new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(Dashboard.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        clearSharedPref();
                        finish();
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

    void showBarcodeDialog() {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.scan_dialog);
            ImageView scanProductId = dialog.findViewById(R.id.scanProductId);
            ImageView scanLotId = dialog.findViewById(R.id.scanLotId);
            productId = dialog.findViewById(R.id.productId);
            lotId = dialog.findViewById(R.id.lotId);
            LinearLayout productTrack = dialog.findViewById(R.id.productTrack);
            LinearLayout lotTrack = dialog.findViewById(R.id.lotTrack);
            ProgressBar mainLoader = dialog.findViewById(R.id.mainLoader);
            RelativeLayout orLayout = dialog.findViewById(R.id.orLayout);
            Button track = dialog.findViewById(R.id.button);
            if(!trackByLotId && !trackByProductId){
                productTrack.setVisibility(View.GONE);
                lotTrack.setVisibility(View.GONE);
                orLayout.setVisibility(View.GONE);
                mainLoader.setVisibility(View.VISIBLE);
                track.setVisibility(View.GONE);
            }else if(trackByLotId && trackByProductId){
                productTrack.setVisibility(View.VISIBLE);
                lotTrack.setVisibility(View.VISIBLE);
                orLayout.setVisibility(View.VISIBLE);
                mainLoader.setVisibility(View.GONE);
                track.setVisibility(View.VISIBLE);
            }else if(trackByProductId){
                productTrack.setVisibility(View.VISIBLE);
                lotTrack.setVisibility(View.GONE);
                orLayout.setVisibility(View.GONE);
                mainLoader.setVisibility(View.GONE);
                track.setVisibility(View.VISIBLE);
            }else{
                productTrack.setVisibility(View.GONE);
                lotTrack.setVisibility(View.VISIBLE);
                orLayout.setVisibility(View.GONE);
                mainLoader.setVisibility(View.GONE);
                track.setVisibility(View.VISIBLE);
            }
            ProgressBar trackLoader = dialog.findViewById(R.id.trackLoader);
            qrScanProductId = new IntentIntegrator(this).setRequestCode(8);
            qrScanLotId = new IntentIntegrator(this).setRequestCode(9);
            ProductLotViewModel productLotViewModel = new ProductLotViewModel();
            scanProductId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    qrScanProductId.initiateScan();
                }
            });
            scanLotId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    qrScanLotId.initiateScan();
                }
            });
            track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productId.setError(null);
                    lotId.setError(null);
                    if (productId.getText().toString().trim().isEmpty() && lotId.getText().toString().trim().isEmpty()) {
                        productId.setError("Mandatory Field!");
                        lotId.setError("Mandatory Field!");
                    } else if (!productId.getText().toString().trim().isEmpty()) {
                        track.setVisibility(View.GONE);
                        trackLoader.setVisibility(View.VISIBLE);
                        productLotViewModel.getAddress(productId.getText().toString().trim()).observe(Dashboard.this, new Observer<ObjectModel>() {
                            @Override
                            public void onChanged(ObjectModel objectModel) {
                                if (objectModel.isStatus()) {
                                    String _contractAddress = (String) objectModel.getObj();
                                    if (_contractAddress.equals(contractAddress)) {
                                        Intent intent = new Intent(Dashboard.this, MapActivity.class);
                                        intent.putExtra("contractAddress", contractAddress);
                                        intent.putExtra("productId", productId.getText().toString().trim());
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(Dashboard.this, "Product doesn't belong to this Smart-Contract!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Dashboard.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                trackLoader.setVisibility(View.GONE);
                                track.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        track.setVisibility(View.GONE);
                        trackLoader.setVisibility(View.VISIBLE);
                        productLotViewModel.getAddress(lotId.getText().toString().trim()).observe(Dashboard.this, new Observer<ObjectModel>() {
                            @Override
                            public void onChanged(ObjectModel objectModel) {
                                if (objectModel.isStatus()) {
                                    String _contractAddress = (String) objectModel.getObj();
                                    if (_contractAddress.equals(contractAddress)) {
                                        Intent intent = new Intent(Dashboard.this, MapActivity.class);
                                        intent.putExtra("contractAddress", contractAddress);
                                        intent.putExtra("productId", productId.getText().toString().trim());
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(Dashboard.this, "Lot doesn't belong to this Smart-Contract!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Dashboard.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                trackLoader.setVisibility(View.GONE);
                                track.setVisibility(View.VISIBLE);
                            }
                        });
                    }
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
            e.printStackTrace();
        }
    }

    void showProfileDialog() {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.profile_dialog);
            LinearLayout copyAddress = dialog.findViewById(R.id.copyAddress);
            TextView address = dialog.findViewById(R.id.publicAddress);
            TextView balance = dialog.findViewById(R.id.currentBalance);
            ProgressBar balLoader = dialog.findViewById(R.id.balLoader);
            address.setText(Data.publicKey);
            copyAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PublicAddress", address.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(Dashboard.this, "Public Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
            copyAddress.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PublicAddress", address.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(Dashboard.this, "Public Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            TaskRunner taskRunner = new TaskRunner();
            taskRunner.executeAsync(new getAccBal(), (result) -> {
                balance.setText(result);
                balLoader.setVisibility(View.GONE);
                balance.setVisibility(View.VISIBLE);
            });
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class getAccBal implements Callable<String> {

        @Override
        public String call() {
            String result = "";
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/55697f31d7db4e0693f15732b7e10e08"));

                EthGetBalance balanceResult = web3j.ethGetBalance(Data.publicKey, DefaultBlockParameterName.LATEST).send();
                BigInteger wei = balanceResult.getBalance();
                result = wei.doubleValue() / 1e18 + " ETH";
            } catch (Exception e) {
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    void clearSharedPref() {
        SharedPreferences preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 8) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                productId.setText(contents);
                productId.setSelection(productId.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 9) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                lotId.setText(contents);
                lotId.setSelection(productId.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}