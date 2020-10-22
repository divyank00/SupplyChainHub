package com.example.smartcontract.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartcontract.Data;
import com.example.smartcontract.models.ContractModel;
import com.example.smartcontract.models.ObjectModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllContractsViewModel extends ViewModel {
    private MutableLiveData<ObjectModel> userLiveData;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public AllContractsViewModel() {

    }

    public LiveData<ObjectModel> getContracts() {
        userLiveData = new MutableLiveData<>();
        firebaseFirestore.collection("User").document(Data.publicKey).collection("Contracts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<ContractModel> mList = new ArrayList<>();
                    for (DocumentSnapshot ds : task.getResult().getDocuments()) {
                        ContractModel model = ds.toObject(ContractModel.class);
                        mList.add(model);
                    }
                    userLiveData.postValue(new ObjectModel(true, mList, null));
                } else {
                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()));
                }
            }
        });
        return userLiveData;
    }

    public LiveData<ObjectModel> addContract(String contractAddress , String name) {
        userLiveData = new MutableLiveData<>();
        ContractModel model = new ContractModel(contractAddress, name);
        firebaseFirestore.collection("Contracts").document(contractAddress).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult()!=null) {
                    if (task.getResult().exists()) {
                        firebaseFirestore.collection("User").document(Data.publicKey).collection("Contracts").add(model).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    userLiveData.postValue(new ObjectModel(true, model, null));
                                } else {
                                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()));
                                }
                            }
                        });
                    } else {
                        userLiveData.postValue(new ObjectModel(false, null, "No such Supply-Chain with given address exists!"));
                    }
                }else{
                    userLiveData.postValue(new ObjectModel(false, null, task.getException().getMessage()!=null?task.getException().getMessage():"Something went wrong!"));
                }
            }
        });
        return userLiveData;
    }
}

