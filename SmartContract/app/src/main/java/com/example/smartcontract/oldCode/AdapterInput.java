package com.example.smartcontract.oldCode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcontract.R;

import org.json.JSONObject;

import java.util.List;

public class AdapterInput extends RecyclerView.Adapter<AdapterInput.ViewHolder> {

    Context mContext;
    List<JSONObject> inputs;

    public AdapterInput(Context mContext, List<JSONObject> inputs) {
        this.mContext = mContext;
        this.inputs = inputs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.input_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(inputs.get(position).optString("name") + " ("+inputs.get(position).optString("type")+")");

    }

    @Override
    public int getItemCount() {
        return inputs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView name;
        EditText value;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            name = itemView.findViewById(R.id.name);
            value = itemView.findViewById(R.id.value);
        }
    }
}