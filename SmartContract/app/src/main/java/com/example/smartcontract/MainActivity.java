package com.example.smartcontract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.viewModel.ProductLotViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    EditText pk, seed;
    Button btn;
    ImageView scan;
    EditText barcode;
    Button track;
    private IntentIntegrator qrScan;
    ProductLotViewModel productLotViewModel;
    ProgressBar loader;

    public static final String KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        if (sharedPreferences.contains(KEY)) {
            data.publicKey = sharedPreferences.getString(PUBLIC_KEY, "");
            String encryptedKey = sharedPreferences.getString(KEY, "");
            if (encryptedKey != null) {
                try {
                    String[] split = encryptedKey.substring(1, encryptedKey.length() - 1).split(", ");
                    byte[] encryptedBytes = new byte[split.length];
                    for (int i = 0; i < split.length; i++) {
                        encryptedBytes[i] = Byte.parseByte(split[i]);
                    }
                    data.privateKey = decryptKey(encryptedBytes, data.publicKey);
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
        productLotViewModel = new ProductLotViewModel();
        qrScan = new IntentIntegrator(this);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.initiateScan();
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
                        data.privateKey = privateKey;
                        data.publicKey = credentials.getAddress();
                        byte[] encryptedBytes = encryptKey(privateKey, credentials.getAddress());
                        String encryptedKey = Arrays.toString(encryptedBytes);
                        editor.putString(PUBLIC_KEY, credentials.getAddress());
                        editor.putString(KEY, encryptedKey);
                        editor.apply();
                        Intent i = new Intent(MainActivity.this, AllContracts.class);
                        startActivity(i);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "You have typed the seed phrase instead of private key!", Toast.LENGTH_SHORT).show();
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
                    data.privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
                    data.publicKey = credentials.getAddress();
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
                String productId = barcode.getText().toString().trim();
                if (productId.isEmpty()) {
                    barcode.setError("Enter a ProductId");
                    return;
                }
                track.setVisibility(View.GONE);
                loader.setVisibility(View.VISIBLE);
                productLotViewModel.getLotId(productId).observe(MainActivity.this, new Observer<ObjectModel>() {
                    @Override
                    public void onChanged(ObjectModel objectModel) {
                        if (objectModel.isStatus()) {
                            String lotId = (String) objectModel.getObj();
                            Toast.makeText(MainActivity.this, lotId, Toast.LENGTH_SHORT).show();
                            loader.setVisibility(View.GONE);
                            track.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(MainActivity.this, objectModel.getMessage(), Toast.LENGTH_SHORT).show();
                            loader.setVisibility(View.GONE);
                            track.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                Log.d("Address", result.getContents());
                barcode.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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