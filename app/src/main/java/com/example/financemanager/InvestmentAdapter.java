package com.example.financemanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.InvestmentViewHolder> {

    private List<Investment> investments;

    public InvestmentAdapter(List<Investment> investments) {
        this.investments = investments;
    }

    @NonNull
    @Override
    public InvestmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_investment, parent, false);
        return new InvestmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvestmentViewHolder holder, int position) {
        Investment investment = investments.get(position);
        holder.mutualFundName.setText(investment.getMutualFund());
        holder.returnRate.setText("Return Rate: "+investment.getReturnRate());
        holder.amount.setText("Amount: ₹"+investment.getAmount());

        holder.itemView.setOnClickListener(v -> {

            Context context = holder.itemView.getContext();
            // Show a toast with the document ID
            String docId = investment.getDocId() != null ? investment.getDocId() : "";
            Intent intent = new Intent(context,InvestmentView.class);
            intent.putExtra("docId", docId);
            context.startActivity(intent);
//            Toast.makeText(holder.itemView.getContext(), "Doc ID: " + docId, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return investments.size();
    }

    public static class InvestmentViewHolder extends RecyclerView.ViewHolder {
        TextView mutualFundName, returnRate, amount;

        public InvestmentViewHolder(@NonNull View itemView) {
            super(itemView);
            mutualFundName = itemView.findViewById(R.id.text_mutual_fund_name);
            returnRate = itemView.findViewById(R.id.text_return_rate);
            amount = itemView.findViewById(R.id.text_amount);
        }
    }
}
