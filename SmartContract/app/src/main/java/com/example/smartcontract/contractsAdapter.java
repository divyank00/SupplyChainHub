package com.example.smartcontract;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.models.ContractModel;

import java.util.List;

public class contractsAdapter extends RecyclerView.Adapter<contractsAdapter.ViewHolder> {

    Context mContext;
    List<ContractModel> contracts;

    public contractsAdapter(Context mContext, List<ContractModel> contracts) {
        this.mContext = mContext;
        this.contracts = contracts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contract_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(contracts.get(position).getName());
        holder.address.setText(contracts.get(position).getAddress());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,AllFunctions.class);
                i.putExtra("contractAddress",contracts.get(holder.getAdapterPosition()).getAddress());
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView name, address;
        LinearLayout cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.contractAddress);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}