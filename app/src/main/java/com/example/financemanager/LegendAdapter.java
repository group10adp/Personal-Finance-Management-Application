package com.example.financemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.LegendViewHolder> {

    private final List<LegendItem> legendItems;

    public LegendAdapter(List<LegendItem> legendItems) {
        this.legendItems = legendItems;
    }

    @NonNull
    @Override
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.legend_item1, parent, false);
        return new LegendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        LegendItem legendItem = legendItems.get(position);
        holder.legendLabel.setText(legendItem.getLabel());
        holder.legendColorBox.setBackgroundColor(legendItem.getColor());
        holder.legendamount.setText(legendItem.getAmount());
    }

    @Override
    public int getItemCount() {
        return legendItems.size();
    }

    static class LegendViewHolder extends RecyclerView.ViewHolder {
        TextView legendLabel,legendamount;
        View legendColorBox;

        public LegendViewHolder(@NonNull View itemView) {
            super(itemView);
            legendLabel = itemView.findViewById(R.id.legend_label);
            legendamount=itemView.findViewById(R.id.legend_amount);
            legendColorBox = itemView.findViewById(R.id.legend_color);
        }
    }
}
