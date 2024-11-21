package com.example.financemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class InvestmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private InvestmentAdapter adapter;
    private List<Investment> investmentList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_investments, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_investments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize data
        investmentList = new ArrayList<>();
        adapter = new InvestmentAdapter(investmentList);
        recyclerView.setAdapter(adapter);

        // Handle arguments passed from AddInvestment
        checkForNewInvestment();

        // FloatingActionButton to add new investment
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AddInvestment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    /**
     * Check for new investment details passed via arguments and add to the list.
     */
    private void checkForNewInvestment() {
        Bundle args = getArguments();
        if (args != null) {
            String mutualFund = args.getString("mutualFund");
            String returnRate = args.getString("returnRate");
            String amount = args.getString("amount");

            if (mutualFund != null && returnRate != null && amount != null) {
                try {
                    double returnRateValue = Double.parseDouble(returnRate);
                    double amountValue = Double.parseDouble(amount);

                    Investment newInvestment = new Investment(mutualFund, returnRateValue, amountValue);
                    investmentList.add(newInvestment);

                    // Notify adapter about new data
                    adapter.notifyItemInserted(investmentList.size() - 1);
                } catch (NumberFormatException e) {
                    // Handle invalid number format gracefully
                    e.printStackTrace();
                }
            }
        }
    }
}
