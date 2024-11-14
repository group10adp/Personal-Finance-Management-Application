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

public class ExpenseFragment extends Fragment {

    private EditText amountEditText;
    private Button saveButton;
    private DatabaseReference expenseRef, totalExpenseRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        // Initialize views
        amountEditText = view.findViewById(R.id.amountEditText);
        saveButton = view.findViewById(R.id.saveButton);

        // Initialize Firebase Database references
        expenseRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("expense");
        totalExpenseRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("totalExpense");

        // Set click listener on save button
        saveButton.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void saveExpense() {
        String amountStr = amountEditText.getText().toString().trim();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Add new expense entry
            String key = expenseRef.push().getKey();
            if (key != null) {
                expenseRef.child(key).setValue(amount)
                        .addOnSuccessListener(aVoid -> {
                            // Update total expense after successfully saving new entry
                            updateTotalExpense(amount);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save expense.", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalExpense(double newExpense) {
        totalExpenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentTotal = 0.0;

                // If totalExpense exists, retrieve its value
                if (dataSnapshot.exists()) {
                    Double totalExpense = dataSnapshot.getValue(Double.class);
                    if (totalExpense != null) {
                        currentTotal = totalExpense;
                    }
                }

                // Calculate new total expense
                double updatedTotalExpense = currentTotal + newExpense;

                // Update total expense in Firebase
                totalExpenseRef.setValue(updatedTotalExpense)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                            amountEditText.setText(""); // Clear input after saving
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update total expense.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load current total expense.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
