package com.example.smartcontract.mapUsers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.R;
import com.example.smartcontract.models.TrackModel;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.List;

public class TrackUserAdapter extends RecyclerView.Adapter<TrackUserAdapter.ViewHolder> {

    Context mContext;
    List<TrackModel> nodes;
    List<String> userRoles;

    public TrackUserAdapter(Context mContext, List<TrackModel> nodes, List<String> userRoles) {
        this.mContext = mContext;
        this.nodes = nodes;
        this.userRoles = userRoles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.flow_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.publicAddress.setText(nodes.get(position).getUserAddress());
        holder.role.setText("(" + userRoles.get(position) + ")");
        if (nodes.get(position).getBuyingPrice().isEmpty() || nodes.get(position).getBuyingPrice().equals("0")) {
            holder.sellPrice.setVisibility(View.GONE);
        } else {
            holder.sellPrice.setText("Selling Price:" + nodes.get(position).getBuyingPrice());
            holder.sellPrice.setVisibility(View.VISIBLE);
        }
        if (nodes.get(position).getBuyingPrice().isEmpty() || nodes.get(position).getBuyingPrice().equals("0")) {
            holder.soldPrice.setVisibility(View.GONE);
        } else {
            holder.soldPrice.setText("Sold At:" + nodes.get(position).getSellingPrice());
            holder.soldPrice.setVisibility(View.VISIBLE);
        }
        if (nodes.get(position).getTransactionHash().isEmpty()) {
            holder.txnHash.setVisibility(View.GONE);
        } else {
            holder.txnHash.setText("https://rinkeby.etherscan.io/tx/" + nodes.get(position).getTransactionHash());
            holder.txnHash.setVisibility(View.VISIBLE);
        }
        if (position == nodes.size() - 1) {
            holder.downArrow.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        EasyFlipView cardView;
        LinearLayout clickCard;
        TextView publicAddress, role, sellPrice, soldPrice, txnHash;
        ImageView downArrow;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            cardView = itemView.findViewById(R.id.cardView);
            clickCard = itemView.findViewById(R.id.clickCard);
            publicAddress = itemView.findViewById(R.id.publicAddress);
            role = itemView.findViewById(R.id.role);
            sellPrice = itemView.findViewById(R.id.sellPrice);
            soldPrice = itemView.findViewById(R.id.soldPrice);
            txnHash = itemView.findViewById(R.id.txnHash);
            downArrow = itemView.findViewById(R.id.downwArrow);
        }
    }
}