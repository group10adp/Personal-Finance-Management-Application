package com.example.financemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private TextView incomeTextView, expenseTextView, balanceTextView;
    private DatabaseReference incomeRef, expenseRef, balanceRef;

    private double totalIncome = 0.0, totalExpense = 0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the TextViews
        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView); // New TextView for balance

        // Initialize Firebase Database references
        incomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("income");
        expenseRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("expense");
        balanceRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("balance");

        // Retrieve the total income and total expense from Firebase
        fetchTotalIncome();
        fetchTotalExpense();

        return view;
    }

    private void fetchTotalIncome() {
        incomeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalIncome = 0; // Reset total income before summing

                if (dataSnapshot.exists()) {
                    // Iterate through all income entries
                    for (DataSnapshot incomeSnapshot : dataSnapshot.getChildren()) {
                        Double income = incomeSnapshot.child("amount").getValue(Double.class);
                        if (income != null) {
                            totalIncome += income; // Sum the amounts
                        }
                    }
                    // Update the income TextView
                    incomeTextView.setText("₹" + totalIncome);
                } else {
                    // Handle the case where no income data exists
                    incomeTextView.setText("₹0.00");
                }

                // Update the balance after fetching income
                updateBalance();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors during data retrieval
                incomeTextView.setText("Failed to load income data");
            }
        });
    }


    private void fetchTotalExpense() {
        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    totalExpense = 0;

                    // Iterate through all expense entries and sum them
                    for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                        Double expense = expenseSnapshot.child("amount").getValue(Double.class);
                        if (expense != null) {
                            totalExpense += expense;
                        }
                    }

                    // Display the total expense in the TextView
                    expenseTextView.setText("₹" + totalExpense);

                    // Calculate and update the balance
                    updateBalance();
                } else {
                    expenseTextView.setText("₹0.00");
                    updateBalance();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
                expenseTextView.setText("Failed to load expense data");
            }
        });
    }

    private void updateBalance() {
        double balance = totalIncome - totalExpense;

        // Update the balance in the TextView
        balanceTextView.setText("Balance: ₹" + balance);

        // Store the balance in Firebase
        balanceRef.setValue(balance).addOnSuccessListener(aVoid -> {
            // Optionally show a success message
        }).addOnFailureListener(e -> {
            // Optionally show an error message
        });
    }
}
