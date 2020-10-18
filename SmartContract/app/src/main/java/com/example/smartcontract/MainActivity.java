package com.example.smartcontract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;

public class MainActivity extends AppCompatActivity {

    EditText pk, seed;
    Button btn;

    public static final String KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        if(sharedPreferences.contains(KEY)){
            data.privateKey = sharedPreferences.getString(KEY,"");
            Intent i = new Intent(this, AllContracts.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        pk = findViewById(R.id.privateKey);
        btn = findViewById(R.id.button);
        seed = findViewById(R.id.seed);

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
                    String privateKey = pk.getText().toString().trim();
                    Credentials credentials = Credentials.create(privateKey);
                    editor.putString(PUBLIC_KEY,credentials.getAddress());
                    editor.putString(KEY, privateKey);
                }
                else if (!seed.getText().toString().trim().isEmpty()) {
                    String mnemonic = seed.getText().toString().trim();

                    //m/44'/60'/0'/0 derivation path
                    int[] derivationPath = {44 | Bip32ECKeyPair.HARDENED_BIT, 60 | Bip32ECKeyPair.HARDENED_BIT, 0 | Bip32ECKeyPair.HARDENED_BIT, 0, 0};

                    // Generate a BIP32 master keypair from the mnemonic phrase
                    Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(MnemonicUtils.generateSeed(mnemonic, null));

                    // Derive the keypair using the derivation path
                    Bip32ECKeyPair derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, derivationPath);

                    // Load the wallet for the derived keypair
                    Credentials credentials = Credentials.create(derivedKeyPair);
                    editor.putString(PUBLIC_KEY,credentials.getAddress());
                    editor.putString(KEY, credentials.getEcKeyPair().getPrivateKey().toString(16));
                }
                editor.apply();

                Intent i = new Intent(MainActivity.this, AllContracts.class);
                data.privateKey = sharedPreferences.getString(KEY,"");
                startActivity(i);
            }
        });
    }
}