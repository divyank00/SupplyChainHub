package com.example.smartcontract.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.models.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductLotViewModel extends ViewModel {

    private MutableLiveData<ObjectModel> userLiveData;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public ProductLotViewModel() {

    }

    public LiveData<ObjectModel> getMap() {
        userLiveData = new MutableLiveData<>();
        firebaseFirestore.collection("Products").document("AllProducts").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot ds = task.getResult();
                    Map<String,String> map = (Map<String, String>) ds.get("productIds");
                    if (map != null) userLiveData.postValue(new ObjectModel(true,map,null));
                    else userLiveData.postValue(new ObjectModel(true, new HashMap<String,String>(),null));
                } else {
                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()));
                }
            }
        });
        return userLiveData;
    }

    public LiveData<ObjectModel> addMap(Map<String,Map<String,String>> newMap) {
        userLiveData = new MutableLiveData<>();
        firebaseFirestore.collection("Products").document("AllProducts").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot ds = task.getResult();
                    Map<String,Map<String,String>> map = (Map<String, Map<String,String>>) ds.get("productIds");
                    if (map != null) {
                        map.putAll(newMap);
                        ProductModel model = new ProductModel(map);
                        firebaseFirestore.collection("Products").document("AllProducts").set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }else{
                        ProductModel model = new ProductModel(newMap);
                        firebaseFirestore.collection("Products").document("AllProducts").set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                } else {
                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()));
                }
            }
        });
        return userLiveData;
    }

    public LiveData<ObjectModel> getAddress(String productId) {
        userLiveData = new MutableLiveData<>();
        firebaseFirestore.collection("Products").document("AllProducts").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot ds = task.getResult();
                    Map<String,Map<String,String>> map = (Map<String, Map<String,String>>) ds.get("productIds");
                    if(map!=null && map.containsKey(productId)) {
                        Map<String,String> contracts = map.get(productId);
                        userLiveData.postValue(new ObjectModel(true, contracts, null));
                    }else{
                        userLiveData.postValue(new ObjectModel(false, null, "No entry found in database!"));
                    }
                } else {
                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()));
                }
            }
        });
        return userLiveData;
    }
}
