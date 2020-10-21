package com.example.smartcontract.functions;

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

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.smartcontract.Data;
import com.example.smartcontract.R;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AddUser extends AppCompatActivity {

    String contractAddress;
    Integer userRole;
    NiceSpinner spinner;
    TextView parentAddressTV;
    EditText parentAddress, userAddress;
    TaskRunner taskRunner;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
        taskRunner = new TaskRunner();
        userRole = intent.getIntExtra("userRole", Data.userRoles.size());
        parentAddressTV = findViewById(R.id.parentAddressTV);
        parentAddress = findViewById(R.id.parentAddress);
        userAddress = findViewById(R.id.userAddress);
        spinner = findViewById(R.id.nice_spinner);
        button = findViewById(R.id.button);
        button.setBackgroundColor(0xFF3366ff);
        List<String> dataSet = Data.userRoles.subList(userRole + 1, Data.userRoles.size() - 1);
        spinner.attachDataSource(dataSet);
        spinner.setSelectedIndex(0);
        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                if (position == 0) {
                    parentAddressTV.setVisibility(View.GONE);
                    parentAddress.setVisibility(View.GONE);
                    parentAddress.setText("");
                } else {
                    parentAddressTV.setText(dataSet.get(position - 1) + "\'s Public Address:");
                    parentAddressTV.setVisibility(View.VISIBLE);
                    parentAddress.setVisibility(View.VISIBLE);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedIndex() == 0) {
                    if (userAddress.getText().toString().trim().isEmpty()) {
                        userAddress.setError("Mandatory Field!");
                    } else {
                        executeAddChildUserGas(userAddress.getText().toString().trim());
                    }
                } else {
                    if (userAddress.getText().toString().trim().isEmpty()) {
                        userAddress.setError("Mandatory Field!");
                    } else if (parentAddress.getText().toString().trim().isEmpty()) {
                        parentAddress.setError("Mandatory Field!");
                    } else {
                        executeAddOtherUserGas(userAddress.getText().toString().trim(), parentAddress.getText().toString().trim(), userRole + 1 + spinner.getSelectedIndex());
                    }
                }
            }
        });
    }

    private void executeAddChildUserGas(String address) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new addChildUserGas(address), (result) -> {
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
                                executeAddChildUser(address);
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

    private void executeAddChildUser(String address) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new addChildUser(address), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus()) {
                showTransactionDialog(true, result.getData(), null);
            } else {
                showTransactionDialog(false, result.getData(), result.getErrorMsg());
            }
        });
    }

    private void executeAddOtherUserGas(String parentAddress, String address, int userRoleI) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new addOtherUserGas(parentAddress, address, userRoleI), (result) -> {
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
                                executeAddOtherUser(parentAddress, address, userRoleI);
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

    private void executeAddOtherUser(String parentAddress, String address, int userRoleI) {
        final KProgressHUD progressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new addOtherUser(parentAddress, address, userRoleI), (result) -> {
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

    class addChildUserGas implements Callable<WriteObject> {

        String userAddressS;

        public addChildUserGas(String userAddress) {
            this.userAddressS = userAddress;
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
                inputAsync.add(new Address(userAddressS));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("addChildUser", // Function name
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

    class addChildUser implements Callable<WriteObject> {

        String userAddressS;

        public addChildUser(String userAddress) {
            this.userAddressS = userAddress;
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
                inputAsync.add(new Address(userAddressS));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("addChildUser", // Function name
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

    class addOtherUserGas implements Callable<WriteObject> {

        String parentAddressS, userAddressS;
        int userRoleI;

        public addOtherUserGas(String parentAddressS, String userAddressS, int userRoleI) {
            this.parentAddressS = parentAddressS;
            this.userAddressS = userAddressS;
            this.userRoleI = userRoleI;
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
                inputAsync.add(new Address(parentAddressS));
                inputAsync.add(new Address(userAddressS));
                inputAsync.add(new Uint256(userRoleI));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("addOtherUser", // Function name
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

    class addOtherUser implements Callable<WriteObject> {

        String parentAddressS, userAddressS;
        int userRoleI;

        public addOtherUser(String parentAddressS, String userAddressS, int userRoleI) {
            this.parentAddressS = parentAddressS;
            this.userAddressS = userAddressS;
            this.userRoleI = userRoleI;
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
                inputAsync.add(new Address(parentAddressS));
                inputAsync.add(new Address(userAddressS));
                inputAsync.add(new Uint256(userRoleI));
                List<TypeReference<?>> outputAsync = new ArrayList<>();

                Function function = new Function("addOtherUser", // Function name
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
                    Toast.makeText(AddUser.this, "Transaction Hash copied to clipboard!", Toast.LENGTH_SHORT).show();
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