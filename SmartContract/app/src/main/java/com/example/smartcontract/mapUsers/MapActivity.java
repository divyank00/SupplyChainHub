package com.example.smartcontract.mapUsers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartcontract.Dashboard;
import com.example.smartcontract.Data;
import com.example.smartcontract.R;
import com.google.api.Distribution;

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

import jnr.ffi.Struct;

public class MapActivity extends AppCompatActivity {

    ArrayList<String> arr = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    LinearLayout linear;
    TaskRunner taskRunner = new TaskRunner();
    String contractAddress, productId, cusAddress;
    List<Address> userAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        contractAddress = intent.getStringExtra("contractAddress");
        productId = intent.getStringExtra("productId");
        cusAddress = intent.getStringExtra("publicAddress");
        userAddress = new ArrayList<>();
        linear = findViewById(R.id.linear);
        executeTrackProductByProductId();
    }

    private void executeTrackProductByProductId() {
        taskRunner.executeAsync(new trackProductByProductId(), result -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    userAddress = (List<Address>) result.getData().get(3).getValue();
                    for (int i = 0; i < userAddress.size(); i++) {
                        executeGetUserDetails(userAddress.get(i).toString());
                    }
                }
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeGetUserDetails(String trackAddress) {
        taskRunner.executeAsync(new getUserDetails(trackAddress), result -> {
            if (result.isStatus()) {
                if (!result.getData().isEmpty()) {
                    String name = result.getData().get(1).toString();
                    String lon = result.getData().get(2).toString();
                    String lat = result.getData().get(3).toString();
                    String url = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
                    names.add(name);
                    arr.add(url);
                }
                createUI();
            } else {
                Toast.makeText(this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void createUI() {
        //linear.removeAllViews();
        for (int i = 0; i < arr.size(); i++) {

            LinearLayout linear1 = new LinearLayout(this);
            linear1.setOrientation(LinearLayout.HORIZONTAL);
            linear1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linear1.setGravity(Gravity.CENTER_HORIZONTAL);


            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(140, 140);
            l.setMargins(150, 0, 30, 10);
            image.setLayoutParams(l);

            int strokeWidth = 5;
            int strokeColor = Color.parseColor("#a200e0");
            int fillColor = Color.parseColor("#b39afd");
            GradientDrawable gD = new GradientDrawable();
            gD.setColor(fillColor);
            gD.setShape(GradientDrawable.OVAL);
            gD.setStroke(strokeWidth, strokeColor);
            image.setBackground(gD);

            TextView textname = new TextView(this);
            textname.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textname.setText(arr.get(i));
            textname.setGravity(Gravity.CENTER_VERTICAL);

            linear1.addView(textname);
            linear1.addView(image);

            TextView text = new TextView(this);
            text.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setText(arr.get(i));
            text.setGravity(Gravity.CENTER_VERTICAL);

            text.setClickable(true);
            text.setAutoLinkMask(Linkify.WEB_URLS);

            linear1.addView(text);

            LinearLayout linear2 = new LinearLayout(this);
            linear2.setOrientation(LinearLayout.VERTICAL);
            linear2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            linear2.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView text1 = new TextView(this);
            text1.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            text1.setGravity(Gravity.CENTER_HORIZONTAL);
            text1.setText(" ");
            text1.setBackgroundColor(Color.BLACK);
            text1.setTextSize(15);
            linear2.addView(text1);

            linear.addView(linear1);
            linear.addView(linear2);
        }

        TextView tex = new TextView(this);
        tex.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tex.setGravity(Gravity.CENTER_HORIZONTAL);
        tex.setText("Customer");
        tex.setTextColor(Color.BLACK);
        tex.setTextSize(20);
        tex.setGravity(Gravity.CENTER);

        linear.addView(tex);

    }

    public static class TaskRunner {
        //        private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Executor executor = new ThreadPoolExecutor(5, 128, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        private final Handler handler = new Handler(Looper.getMainLooper());

        public interface Callback<R> {
            void onComplete(R result);
        }

        public <R> void executeAsync(Callable<R> callable, Dashboard.TaskRunner.Callback<R> callback) {
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

                // Load an account
                // String pk = Data.privateKey;
                //Credentials credentials = Credentials.create(pk);

                // Contract and functions
                List<Type> inputAsync = new ArrayList<>();
                inputAsync.add(new Utf8String(productId));
                List<TypeReference<?>> outputAsync = new ArrayList<>();
                outputAsync.add(new TypeReference<Utf8String>() {
                });
                outputAsync.add(new TypeReference<Uint256>() {
                });
                outputAsync.add(new TypeReference<Address>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Address>>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Uint256>>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Uint256>>() {
                });
                outputAsync.add(new TypeReference<DynamicArray<Utf8String>>() {
                });
                Function function = new Function("trackProductByProductId", // Function name
                        inputAsync,  // Function input parameters
                        outputAsync); // Function returned parameters
                Log.d("Address Output: ", outputAsync.size() + "");
                String encodedFunction = FunctionEncoder.encode(function);
                EthCall ethCall = web3j.ethCall(
                        Transaction.createEthCallTransaction(cusAddress, contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                Toast.makeText(MapActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Address Error: ", e.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

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

                // Load an account
                // String pk = Data.privateKey;
                //Credentials credentials = Credentials.create(pk);

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
                        Transaction.createEthCallTransaction(cusAddress, contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .sendAsync().get();
                if (!ethCall.isReverted()) {
                    result = new Object(true, FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()), null);
                } else {
                    result = new Object(false, null, ethCall.getRevertReason() != null ? ethCall.getRevertReason() : "Something went wrong!");
                }
            } catch (Exception e) {
                Toast.makeText(MapActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
}