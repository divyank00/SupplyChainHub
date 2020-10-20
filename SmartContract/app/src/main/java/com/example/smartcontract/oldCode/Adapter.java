package com.example.smartcontract.oldCode;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.R;

import org.json.JSONObject;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    Context mContext;
    List<JSONObject> functions;
    String contractAddress;

    public Adapter(Context mContext, List<JSONObject> functions, String contractAddress) {
        this.mContext = mContext;
        this.functions = functions;
        this.contractAddress= contractAddress;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(functions.get(position).optString("name"));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, callFunction.class);
                i.putExtra("object",functions.get(holder.getAdapterPosition()).toString());
                i.putExtra("contractAddress",contractAddress);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return functions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView name;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            name = itemView.findViewById(R.id.name);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}