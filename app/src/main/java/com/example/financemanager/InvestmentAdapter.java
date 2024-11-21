package com.example.financemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        holder.mutualFundName.setText(investment.getMutualFundName());
        holder.returnRate.setText(String.format("Return Rate: %.2f%%", investment.getReturnRate()));
        holder.amount.setText(String.format("Amount: ₹%.2f", investment.getAmount()));
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
