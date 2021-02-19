package com.example.smartcontract;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.mapUsers.MapActivity;
import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.models.SingleProductModel;
import com.example.smartcontract.viewModel.ProductLotViewModel;
import com.google.zxing.integration.android.IntentIntegrator;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements AllProductsAdapter.OnClick{

    EditText pk, seed;
    Button btn;
    ImageView scan, scanLot;
    EditText barcode, barcodeLot;
    Button track;
    private IntentIntegrator qrScan, qrScanLot;
    ProductLotViewModel productLotViewModel;
    ProgressBar loader;
    ConstraintLayout productNames;
    RecyclerView productNamesRV;

    public static final String KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        if (sharedPreferences.contains(KEY)) {
            Data.publicKey = sharedPreferences.getString(PUBLIC_KEY, "");
            String encryptedKey = sharedPreferences.getString(KEY, "");
            if (encryptedKey != null) {
                try {
                    String[] split = encryptedKey.substring(1, encryptedKey.length() - 1).split(", ");
                    byte[] encryptedBytes = new byte[split.length];
                    for (int i = 0; i < split.length; i++) {
                        encryptedBytes[i] = Byte.parseByte(split[i]);
                    }
                    Data.privateKey = decryptKey(encryptedBytes, Data.publicKey);
                } catch (Exception e) {
                    Log.d("EncryptionError", "D: " + e.toString());
                    e.printStackTrace();
                }
                Intent i = new Intent(this, AllContracts.class);
                startActivity(i);
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_main);
        pk = findViewById(R.id.privateKey);
        btn = findViewById(R.id.button);
        seed = findViewById(R.id.seed);
        scan = findViewById(R.id.scan);
        track = findViewById(R.id.track);
        barcode = findViewById(R.id.barcode);
        loader = findViewById(R.id.loader);
        productNames = findViewById(R.id.productNames);
        productNamesRV  = findViewById(R.id.productNamesRV);
        productNamesRV.setLayoutManager(new LinearLayoutManager(this));
        productLotViewModel = new ProductLotViewModel();
        qrScan = new IntentIntegrator(this).setRequestCode(1);
        scanLot = findViewById(R.id.scanLot);
        barcodeLot = findViewById(R.id.barcodeLot);
        qrScanLot = new IntentIntegrator(this).setRequestCode(2);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.initiateScan();
            }
        });
        scanLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanLot.initiateScan();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pk.getText().toString().trim().isEmpty() && seed.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter your private key or seed phrase", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (!pk.getText().toString().trim().isEmpty()) {
                    try {
                        String privateKey = pk.getText().toString().trim();
                        Credentials credentials = Credentials.create(privateKey);
                        Data.privateKey = privateKey;
                        Data.publicKey = credentials.getAddress();
                        byte[] encryptedBytes = encryptKey(privateKey, credentials.getAddress());
                        String encryptedKey = Arrays.toString(encryptedBytes);
                        editor.putString(PUBLIC_KEY, credentials.getAddress());
                        editor.putString(KEY, encryptedKey);
                        editor.apply();
                        Intent i = new Intent(MainActivity.this, AllContracts.class);
                        startActivity(i);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "It seems you have typed the seed phrase instead of private key!", Toast.LENGTH_SHORT).show();
                        Log.d("EncryptionError", e.toString());
                        e.printStackTrace();
                    }
                } else if (!seed.getText().toString().trim().isEmpty()) {
                    String mnemonic = seed.getText().toString().trim();

                    //m/44'/60'/0'/0 derivation path
                    int[] derivationPath = {44 | Bip32ECKeyPair.HARDENED_BIT, 60 | Bip32ECKeyPair.HARDENED_BIT, 0 | Bip32ECKeyPair.HARDENED_BIT, 0, 0};

                    // Generate a BIP32 master keypair from the mnemonic phrase
                    Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(MnemonicUtils.generateSeed(mnemonic, null));

                    // Derive the keypair using the derivation path
                    Bip32ECKeyPair derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, derivationPath);

                    // Load the wallet for the derived keypair
                    Credentials credentials = Credentials.create(derivedKeyPair);
                    Data.privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
                    Data.publicKey = credentials.getAddress();
                    try {
                        byte[] encryptedBytes = encryptKey(credentials.getEcKeyPair().getPrivateKey().toString(16), credentials.getAddress());
                        String encryptedKey = Arrays.toString(encryptedBytes);
                        editor.putString(PUBLIC_KEY, credentials.getAddress());
                        editor.putString(KEY, encryptedKey);
                        editor.apply();
                        Intent i = new Intent(MainActivity.this, AllContracts.class);
                        startActivity(i);
                        finish();
                    } catch (Exception e) {
                        Log.d("EncryptionError", e.toString());
                        e.printStackTrace();
                    }
                }
            }
        });
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcode.setError(null);
                if (barcode.getText().toString().trim().isEmpty() && barcodeLot.getText().toString().trim().isEmpty()) {
                    barcode.setError("Mandatory Field");
                } else if (!barcode.getText().toString().trim().isEmpty()) {
                    track.setVisibility(View.GONE);
                    loader.setVisibility(View.VISIBLE);
                    String _productId = barcode.getText().toString().trim();
                    productLotViewModel.getAddress(_productId).observe(MainActivity.this, new Observer<ObjectModel>() {
                        @Override
                        public void onChanged(ObjectModel objectModel) {
                            if (objectModel.isStatus()) {
                                Map<String, String> contractAddress = (Map<String, String>) objectModel.getObj();
                                List<SingleProductModel> products = new ArrayList<>();
                                for (Map.Entry<String, String> entry : contractAddress.entrySet()) {
                                    products.add(new SingleProductModel(entry.getKey(), entry.getValue()));
                                }
                                AllProductsAdapter2 productsAdapter = new AllProductsAdapter2(MainActivity.this, products, MainActivity.this, null, _productId);
                                productNamesRV.setAdapter(productsAdapter);
                                productNames.setVisibility(View.VISIBLE);
                            } else {
                                productNames.setVisibility(View.GONE);
                                List<SingleProductModel> products = new ArrayList<>();
                                AllProductsAdapter2 productsAdapter = new AllProductsAdapter2(MainActivity.this, products, MainActivity.this, null, _productId);
                                productNamesRV.setAdapter(productsAdapter);
                                Toast.makeText(MainActivity.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            loader.setVisibility(View.GONE);
                            track.setVisibility(View.VISIBLE);
                        }
                    });
                } else if (!barcodeLot.getText().toString().trim().isEmpty()) {
                    track.setVisibility(View.GONE);
                    loader.setVisibility(View.VISIBLE);
                    String _lotId = barcodeLot.getText().toString().trim();
                    productLotViewModel.getAddress(_lotId).observe(MainActivity.this, new Observer<ObjectModel>() {
                        @Override
                        public void onChanged(ObjectModel objectModel) {
                            if (objectModel.isStatus()) {
                                Map<String, String> contractAddress = (Map<String, String>) objectModel.getObj();
                                List<SingleProductModel> products = new ArrayList<>();
                                for (Map.Entry<String, String> entry : contractAddress.entrySet()) {
                                    products.add(new SingleProductModel(entry.getKey(), entry.getValue()));
                                }
                                AllProductsAdapter2 productsAdapter = new AllProductsAdapter2(MainActivity.this, products, MainActivity.this, _lotId, null);
                                productNamesRV.setAdapter(productsAdapter);
                                productNames.setVisibility(View.VISIBLE);
                            } else {
                                productNames.setVisibility(View.GONE);
                                List<SingleProductModel> products = new ArrayList<>();
                                AllProductsAdapter2 productsAdapter = new AllProductsAdapter2(MainActivity.this, products, MainActivity.this, _lotId, null);
                                productNamesRV.setAdapter(productsAdapter);
                                Toast.makeText(MainActivity.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            loader.setVisibility(View.GONE);
                            track.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void click(String contractAddress, String lotId, String productId) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra("contractAddress", contractAddress);
        if (productId != null)
            intent.putExtra("productId", productId);
        else
            intent.putExtra("lotId", lotId);
        intent.putExtra("isPermitted", false);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                barcode.setText(contents);
                barcode.setSelection(barcode.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                barcodeLot.setText(contents);
                barcodeLot.setSelection(barcodeLot.getText().length());
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static byte[] encryptKey(String privateKey, String publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        SecretKey secret = new SecretKeySpec(publicKey.substring(0, 32).getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(privateKey.getBytes("UTF-8"));
        return cipherText;
    }

    public static String decryptKey(byte[] cipherText, String publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        SecretKey secret = new SecretKeySpec(publicKey.substring(0, 32).getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
    }
}