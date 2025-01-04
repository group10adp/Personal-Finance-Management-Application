package com.example.financemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class PresavingplanFragment extends Fragment {

    private EditText itemNameInput, approxPriceInput, timeLimitInput;
    private Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_presavingplan, container, false);

        // Initialize input fields and button
        itemNameInput = view.findViewById(R.id.input_item_name);
        approxPriceInput = view.findViewById(R.id.input_approx_price);
        timeLimitInput = view.findViewById(R.id.input_time_limit);
        submitButton = view.findViewById(R.id.btn_submit);

        // Set OnClickListener for Submit Button
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                Intent intent = new Intent(getActivity(), PresavingPlanDetails.class);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Method to validate input fields
    private boolean validateInputs() {
        String itemName = itemNameInput.getText().toString().trim();
        String approxPrice = approxPriceInput.getText().toString().trim();
        String timeLimit = timeLimitInput.getText().toString().trim();

        return !itemName.isEmpty() && !approxPrice.isEmpty() && !timeLimit.isEmpty();
    }
}
