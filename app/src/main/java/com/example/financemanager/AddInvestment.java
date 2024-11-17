package com.example.financemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddInvestment extends Fragment {

    private Spinner spinnerMutualFund;

    public AddInvestment() {
        // Required empty public constructor
    }

    public static AddInvestment newInstance(String param1, String param2) {
        AddInvestment fragment = new AddInvestment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString("param1");
            String mParam2 = getArguments().getString("param2");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_investment, container, false);

        // Initialize Spinner
        spinnerMutualFund = view.findViewById(R.id.spinner_mutual_fund);

        // Top 5 Indian Mutual Fund Names
        String[] mutualFunds = {
                "SBI Mutual Fund",
                "HDFC Mutual Fund",
                "ICICI Prudential Mutual Fund",
                "Aditya Birla Sun Life Mutual Fund",
                "Axis Mutual Fund"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mutualFunds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMutualFund.setAdapter(adapter);

        return view;
    }
}
