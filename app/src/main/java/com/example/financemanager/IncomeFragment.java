package com.example.financemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IncomeFragment extends Fragment {

    private EditText amountEditText;
    private Button saveButton;
    private DatabaseReference incomeRef, totalIncomeRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        // Initialize views
        amountEditText = view.findViewById(R.id.amountEditText);
        saveButton = view.findViewById(R.id.saveButton);

        // Initialize Firebase Database references
        incomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("income");
        totalIncomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("totalIncome");

        // Set click listener on save button
        saveButton.setOnClickListener(v -> saveIncome());

        return view;
    }

    private void saveIncome() {
        String amountStr = amountEditText.getText().toString().trim();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Add new income entry
            String key = incomeRef.push().getKey();
            if (key != null) {
                incomeRef.child(key).setValue(amount)
                        .addOnSuccessListener(aVoid -> {
                            // Update total income after successfully saving new entry
                            updateTotalIncome(amount);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save amount.", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalIncome(double newIncome) {
        totalIncomeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentTotal = 0.0;

                // If totalIncome exists, retrieve its value
                if (dataSnapshot.exists()) {
                    Double totalIncome = dataSnapshot.getValue(Double.class);
                    if (totalIncome != null) {
                        currentTotal = totalIncome;
                    }
                }

                // Calculate new total income
                double updatedTotalIncome = currentTotal + newIncome;

                // Update total income in Firebase
                totalIncomeRef.setValue(updatedTotalIncome)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Amount saved successfully!", Toast.LENGTH_SHORT).show();
                            amountEditText.setText(""); // Clear input after saving
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update total income.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load current total income.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
