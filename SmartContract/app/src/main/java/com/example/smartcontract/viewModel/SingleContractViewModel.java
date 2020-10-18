package com.example.smartcontract.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartcontract.models.ObjectModel;
import com.example.smartcontract.models.SingleContractModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SingleContractViewModel extends ViewModel {
    private MutableLiveData<ObjectModel> userLiveData;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public SingleContractViewModel() {

    }

    public LiveData<ObjectModel> getContract(String address) {
        userLiveData = new MutableLiveData<>();
        firebaseFirestore.collection("Contracts").document(address).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    SingleContractModel model = task.getResult().toObject(SingleContractModel.class);
                    userLiveData.postValue(new ObjectModel(true, model, null));
                }else{
                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()));
                }
            }
        });
        return userLiveData;
    }
}

