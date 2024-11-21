package com.example.financemanager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class AddInvestment extends Fragment {

    private Spinner spinnerMutualFund;
    private EditText editTextReturnRate, editTextAmount;
    private Button submitButton;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_investment, container, false);

        // Initialize views
        spinnerMutualFund = view.findViewById(R.id.spinner_mutual_fund);
        editTextReturnRate = view.findViewById(R.id.edittext_return_rate);
        editTextAmount = view.findViewById(R.id.edittext_amount);
        submitButton = view.findViewById(R.id.submit_button);

        // Populate Spinner with mutual funds
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

        // TextWatcher to enable Save button only if all fields are filled
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextReturnRate.addTextChangedListener(textWatcher);
        editTextAmount.addTextChangedListener(textWatcher);

        // Handle Save button click
        submitButton.setOnClickListener(v -> {
            String mutualFund = spinnerMutualFund.getSelectedItem().toString();
            String returnRate = editTextReturnRate.getText().toString();
            String amount = editTextAmount.getText().toString();

            // Pass data back to InvestmentsFragment
            Bundle bundle = new Bundle();
            bundle.putString("mutualFund", mutualFund);
            bundle.putString("returnRate", returnRate);
            bundle.putString("amount", amount);

            InvestmentsFragment investmentsFragment = new InvestmentsFragment();
            investmentsFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, investmentsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    // Enable Save button only when all fields have valid input
    private void validateInput() {
        String returnRate = editTextReturnRate.getText().toString().trim();
        String amount = editTextAmount.getText().toString().trim();
        submitButton.setEnabled(!returnRate.isEmpty() && !amount.isEmpty());
    }
}
