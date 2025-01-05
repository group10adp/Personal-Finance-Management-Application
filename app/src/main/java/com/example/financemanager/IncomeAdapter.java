package com.example.financemanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private List<IncomeModel> incomeList;

    public IncomeAdapter(List<IncomeModel> incomeList) {
        this.incomeList = incomeList;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_income_card, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        IncomeModel income = incomeList.get(position);
        holder.tvPrice.setText("₹" + income.getAmount());
        holder.tvCategory.setText(income.getCategory());
        holder.tvDate.setText(income.getDate());
        holder.tvTime.setText(income.getTime());

        holder.itemView.setOnClickListener(v -> {

            Context context = holder.itemView.getContext();
            // Show a toast with the document ID
            String docId = income.getDocId() != null ? income.getDocId() : "";
            Intent intent = new Intent(context,TransactionView.class);
            intent.putExtra("docId", docId);
            intent.putExtra("from", "income");
            context.startActivity(intent);
            //Toast.makeText(holder.itemView.getContext(), "Doc ID: " + docId, Toast.LENGTH_SHORT).show();
        });
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
