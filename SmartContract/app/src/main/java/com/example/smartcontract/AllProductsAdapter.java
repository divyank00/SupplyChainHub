package com.example.smartcontract;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.models.SingleProductModel;

import java.util.List;

public class AllProductsAdapter extends RecyclerView.Adapter<AllProductsAdapter.ViewHolder> {

    Context mContext;
    List<SingleProductModel> contracts;
    OnClick onClick;
    String lotId, productId;

    public AllProductsAdapter(Context mContext, List<SingleProductModel> contracts, OnClick onclick, String lotId, String productId) {
        this.mContext = mContext;
        this.contracts = contracts;
        this.onClick=onclick;
        this.lotId=lotId;
        this.productId=productId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_name, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(contracts.get(position).getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.click(contracts.get(holder.getAdapterPosition()).getContractAddress(),lotId,productId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView name;
        LinearLayout cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            name = itemView.findViewById(R.id.name);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
    interface OnClick{
        void click(String contractAddress, String lotId, String productId);
    }
}