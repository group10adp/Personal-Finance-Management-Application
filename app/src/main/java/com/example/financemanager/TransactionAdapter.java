package com.example.financemanager;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.IncomeViewHolder> {

    private List<TransactionModel> incomeList;

    public TransactionAdapter(List<TransactionModel> incomeList) {
        this.incomeList = incomeList;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_card, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        TransactionModel transaction = incomeList.get(position);

        // Set text for transaction details
        holder.tvPrice.setText("₹" + transaction.getPrice());
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDate.setText(transaction.getDate());
        holder.tvTime.setText(transaction.getTime());

        // Change text color based on type (Income or Expense)
        Log.d("Harami:",transaction.getType());
        if ("Income".equalsIgnoreCase(transaction.getType())) {

            // Income: Green shades
            holder.tvPrice.setTextColor(Color.parseColor("#388E3C")); // Dark Green
            holder.tvCategory.setTextColor(Color.parseColor("#4CAF50")); // Light Green
        } else if ("Expense".equalsIgnoreCase(transaction.getType())) {
            // Expense: Red shades
            holder.tvPrice.setTextColor(Color.parseColor("#D01B0D")); // Dark Red
            holder.tvCategory.setTextColor(Color.parseColor("#E4635D")); // Light Red
        } else {
            // Default color for undefined types
            holder.tvPrice.setTextColor(Color.BLACK);
            holder.tvCategory.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public static class IncomeViewHolder extends RecyclerView.ViewHolder {

        TextView tvPrice, tvCategory, tvDate, tvTime;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
