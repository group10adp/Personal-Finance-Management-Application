package com.example.financemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class PresavingplanFragment extends Fragment {

    private EditText itemNameInput, approxPriceInput, timeLimitInput;
    private Button submitButton;
    private Spinner categorySpinner;

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

        categorySpinner = view.findViewById(R.id.categorySpinner);

        String[] categoryArray = {
                "2024", "2025"
        };

// Step 2: Create an ArrayAdapter using the category array
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryArray
        );

// Step 3: Set the layout for the dropdown items
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Step 4: Attach the adapter to the Spinner
        categorySpinner.setAdapter(categoryAdapter);



        // Set OnClickListener for Submit Button
        submitButton.setOnClickListener(v -> {
            String approxPriceText = approxPriceInput.getText().toString().trim();
            String timeLimitText = timeLimitInput.getText().toString().trim();
            String item= itemNameInput.getText().toString().trim();
            int approxPrice = Integer.parseInt(approxPriceText);
            int timeLimit = Integer.parseInt(timeLimitText);

                        // Perform the division
            if (timeLimit != 0) { // Avoid division by zero
                AlertDialog progressDialog = new AlertDialog.Builder(getContext())
                        .setView(R.layout.custom_loading_dialog) // Reference your custom layout
                        .setCancelable(false)
                        .create();
                int result = approxPrice / timeLimit;
                Intent intent = new Intent(getActivity(), PresavingPlanDetails.class);
                intent.putExtra("result", String.valueOf(result));
                intent.putExtra("yearForData", categorySpinner.getSelectedItem().toString());
                intent.putExtra("goalAmount", approxPriceText);
                intent.putExtra("goalItem", item);
                intent.putExtra("timeSpan", timeLimitText);
                intent.putExtra("showLoading", true);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Time limit cannot be zero", Toast.LENGTH_SHORT).show();
            }



        });

        return view;
    }

    // Method to validate input fields

}
