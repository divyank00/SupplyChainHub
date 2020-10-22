package com.example.smartcontract.functions;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.smartcontract.Data;
import com.example.smartcontract.R;
import com.kaopiz.kprogresshud.KProgressHUD;

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
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GetUserDetails extends AppCompatActivity {

    String contractAddress;
    EditText userAddress;
    TaskRunner taskRunner;
    Button button;
    CardView resultCard;
    TextView userRole, name, location, parentAddress, childAddresses, currentQuantity, error;
    String ownerAddress;
    List<String> userRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_details);
        getSupportActionBar().setTitle("User Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        userRoles = new ArrayList<>();
        ownerAddress = intent.getStringExtra("ownerAddress");
        userRoles = intent.getStringArrayListExtra("userRoles");
        contractAddress = intent.getStringExtra("contractAddress");
        taskRunner = new TaskRunner();
        userAddress = findViewById(R.id.userAddress);
        button = findViewById(R.id.button);
//        button.setBackgroundColor(getResources().getColor(R.color.green));
        resultCard = findViewById(R.id.result);
        userRole = findViewById(R.id.userRole);
        name = findViewById(R.id.name);
        location = findViewById(R.id.location);
        parentAddress = findViewById(R.id.parentAddress);
        childAddresses = findViewById(R.id.childAddresses);
        currentQuantity = findViewById(R.id.currentQuantity);
        error = findViewById(R.id.error);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userAddress.getText().toString().trim().isEmpty()) {
                    userAddress.setError("Mandatory Field!");
                } else {
                    executeGetUserDetails(userAddress.getText().toString().trim());
                }
            }
        });
    }

    private void executeGetUserDetails(String address) {
        final KProgressHUD progressHUD = KProgressHUD.create(GetUserDetails.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        taskRunner.executeAsync(new getUserDetails(address), (result) -> {
            progressHUD.dismiss();
            if (result.isStatus() && !result.getData().isEmpty()) {
                int userRoleInt = Integer.parseInt(result.getData().get(0).getValue().toString());
                if (userRoleInt == userRoles.size() - 1) {
                    error.setText("User not a member of this Supply Chain!");
                    error.setVisibility(View.VISIBLE);
                    userRole.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    location.setVisibility(View.GONE);
                    parentAddress.setVisibility(View.GONE);
                    childAddresses.setVisibility(View.GONE);
                    currentQuantity.setVisibility(View.GONE);
                } else {
                    error.setVisibility(View.GONE);
                    userRole.setText("Role: " + userRoles.get(userRoleInt));
                    userRole.setVisibility(View.VISIBLE);
                    if (result.getData().get(1).getValue().toString().isEmpty()) {
                        name.setVisibility(View.GONE);
                    } else {
                        name.setText("Name: " + result.getData().get(1).getValue().toString());
                        name.setVisibility(View.VISIBLE);
                    }
                    if (result.getData().get(2).getValue().toString().isEmpty() || result.getData().get(3).getValue().toString().isEmpty()) {
                        location.setVisibility(View.GONE);
                    } else {
                        location.setText("Location: https://www.google.com/maps/search/?api=1&query=" + result.getData().get(3).getValue().toString() + "," + result.getData().get(2).getValue().toString());
                        location.setVisibility(View.VISIBLE);
                    }
                    if (userRoleInt < 2) {
                        parentAddress.setVisibility(View.GONE);
                    } else {
                        parentAddress.setVisibility(View.VISIBLE);
                        parentAddress.setText(userRoles.get(userRoleInt - 1) + ":\n" + result.getData().get(4).getValue().toString());
                    }
                    if (userRoleInt == userRoles.size() - 2) {
                        childAddresses.setVisibility(View.GONE);
                    } else {
                        String childAdd = result.getData().get(5).getValue().toString();
                        childAdd = childAdd.substring(1, childAdd.length() - 1);
                        if (childAdd.isEmpty()) {
                            childAddresses.setVisibility(View.GONE);
                        } else {
                            String text = userRoles.get(userRoleInt + 1) + "s:\n";
                            String[] arr = childAdd.split(",");
                            for (int i = 0; i < arr.length - 1; i++) {
                                text += arr[i].trim() + ",\n";
                            }
                            text += arr[arr.length - 1];
                            childAddresses.setText(text);
                            childAddresses.setVisibility(View.VISIBLE);
                        }
                    }
                    if (userRoleInt == 0) {
                        currentQuantity.setVisibility(View.GONE);
                    } else {
                        currentQuantity.setText("Quantity: " + result.getData().get(6).getValue().toString());
                        currentQuantity.setVisibility(View.VISIBLE);
                    }
                }
                resultCard.setVisibility(View.VISIBLE);
            } else {
                resultCard.setVisibility(View.GONE);
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
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

    class getUserDetails implements Callable<Object> {

        String address;

        public getUserDetails(String address) {
            this.address = address;
        }

        @Override
        public Object call() {
            Object result = new Object();
            try {
                // Connect to the node
                System.out.println("Connecting to Ethereum ...");
                Web3j web3j = Web3j.build(new HttpService(getString(R.string.endpoint)));

                // Load an account
                String pk = Data.privateKey;
                Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Address(address));
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
                result = new Object(false,null,e.toString());
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

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}