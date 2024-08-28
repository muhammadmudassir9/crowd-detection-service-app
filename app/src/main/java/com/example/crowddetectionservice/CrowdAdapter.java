package com.example.crowddetectionservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;

import java.util.List;

public class CrowdAdapter extends RecyclerView.Adapter<CrowdAdapter.ViewHolder> {
    private List<CrowdItem> crowdItems;

    public CrowdAdapter(List<CrowdItem> crowdItems) {
        this.crowdItems = crowdItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crowd, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CrowdItem item = crowdItems.get(position);
        holder.alertTextView.setText(item.getAlert());
        holder.crowdCountTextView.setText(String.valueOf(item.getCrowdCount()));
        holder.timeTextView.setText(item.getTime());
    }

    @Override
    public int getItemCount() {
        return crowdItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView alertTextView;
        TextView crowdCountTextView;
        TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            alertTextView = itemView.findViewById(R.id.alertTextView);
            crowdCountTextView = itemView.findViewById(R.id.crowdCountTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}
