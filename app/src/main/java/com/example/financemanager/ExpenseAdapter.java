package com.example.financemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.IncomeViewHolder> {

    private List<IncomeModel> incomeList;

    public ExpenseAdapter(List<IncomeModel> incomeList) {
        this.incomeList = incomeList;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_card, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        IncomeModel income = incomeList.get(position);
        holder.tvPrice.setText("₹" + income.getPrice());
        holder.tvCategory.setText(income.getCategory());
        holder.tvDate.setText(income.getDate());
        holder.tvTime.setText(income.getTime());
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
