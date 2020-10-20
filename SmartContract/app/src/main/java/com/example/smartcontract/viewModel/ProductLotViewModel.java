package com.example.smartcontract.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartcontract.models.ObjectModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProductLotViewModel extends ViewModel {

    private MutableLiveData<ObjectModel> userLiveData;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public ProductLotViewModel() {

    }

    public LiveData<ObjectModel> getLotId(String productId) {
        userLiveData = new MutableLiveData<>();
        firebaseFirestore.collection("Products").document("AllProducts").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot ds = task.getResult();
                    Map<String,String> map = (Map<String, String>) ds.get("productIds");
                    if(map.containsKey(productId)) {
                        String lotId = map.get(productId);
                        userLiveData.postValue(new ObjectModel(true, lotId, null));
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
