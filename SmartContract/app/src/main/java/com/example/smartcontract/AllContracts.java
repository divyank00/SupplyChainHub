package com.example.smartcontract;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.models.ContractModel;
import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.viewModel.AllContractsViewModel;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AllContracts extends AppCompatActivity {

    RecyclerView rV;
    AllContractsAdapter adapter;
    Button button;
    List<ContractModel> mList;
    ProgressBar loader;
    AllContractsViewModel allContractsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contracts);
        rV = findViewById(R.id.rV);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        loader = findViewById(R.id.loader);
        mList = new ArrayList<>();
        adapter = new AllContractsAdapter(this, mList);
        rV.setLayoutManager(new LinearLayoutManager(this));
        rV.setAdapter(adapter);
        allContractsViewModel = new AllContractsViewModel();
        fetchData();
    }

    void showDialog() {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.activity_add_contract);
            EditText address = dialog.findViewById(R.id.contractAddress);
            EditText name = dialog.findViewById(R.id.name);
            Button b = dialog.findViewById(R.id.button);
            ProgressBar loader = dialog.findViewById(R.id.loader);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (address.getText().toString().trim().isEmpty()) {
                        address.setError("Enter the contract address!");
                        return;
                    }
                    if (name.getText().toString().trim().isEmpty()) {
                        name.setError("Enter a suitable name!");
                        return;
                    }
                    for (ContractModel contractModel : mList) {
                        if (contractModel.getAddress().equals(address.getText().toString().trim())) {
                            Toast.makeText(AllContracts.this, "Following Supply-Chain already exists!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    b.setVisibility(View.GONE);
                    loader.setVisibility(View.VISIBLE);
                    allContractsViewModel.addContract(address.getText().toString().trim(), name.getText().toString().trim())
                            .observe(AllContracts.this, new Observer<ObjectModel>() {
                                @Override
                                public void onChanged(ObjectModel objectModel) {
                                    if (objectModel.isStatus()) {
                                        mList.add(0, (ContractModel) objectModel.getObj());
                                        adapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(AllContracts.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        loader.setVisibility(View.GONE);
                                        b.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
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

    void fetchData() {
        allContractsViewModel.getContracts().observe(this, new Observer<ObjectModel>() {
            @Override
            public void onChanged(ObjectModel objectModel) {
                if (objectModel.isStatus()) {
                    loader.setVisibility(View.GONE);
                    mList.addAll((Collection<? extends ContractModel>) objectModel.getObj());
                } else {
                    Toast.makeText(AllContracts.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLogoutDialog() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        clearSharedPref();
        finish();
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
                    Toast.makeText(AllContracts.this, "Public Address copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
            copyAddress.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PublicAddress", address.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(AllContracts.this, "Public Address copied to clipboard!", Toast.LENGTH_SHORT).show();
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
}