package com.example.smartcontract;

import android.content.Context;
import android.content.Intent;
import android.net.sip.SipSession;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.models.ListenerModel;

import org.json.JSONObject;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    Context mContext;
    List<ListenerModel> listeners;

    public DashboardAdapter(Context mContext, List<ListenerModel> listeners) {
        this.mContext = mContext;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dashboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(listeners.get(position).getName());
        holder.desc.setText(listeners.get(position).getDescription());
        holder.clickCard.setOnClickListener(listeners.get(holder.getAdapterPosition()).getClickListener());
    }

    @Override
    public int getItemCount() {
        return listeners.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        LinearLayout clickCard;
        TextView name, desc;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            clickCard = itemView.findViewById(R.id.clickCard);
            name = itemView.findViewById(R.id.name);
            desc = itemView.findViewById(R.id.description);
        }
    }
}