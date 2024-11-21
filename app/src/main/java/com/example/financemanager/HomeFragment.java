package com.example.financemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView incomeTextView, expenseTextView, balanceTextView;
    private FirebaseFirestore firestore;
    private DocumentReference totalIncomeRef, totalExpenseRef, balanceRef;

    private double totalIncome = 0.0, totalExpense = 0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the TextViews
        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView); // New TextView for balance

        // Initialize Firebase Firestore instance
        firestore = FirebaseFirestore.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        // Firebase references for total income, total expense, and balance
        totalIncomeRef = firestore.collection("users").document("user1")
                .collection("income")
                .document(String.valueOf(currentYear))
                .collection(String.valueOf(currentMonth))
                .document("totalIncome");

        totalExpenseRef = firestore.collection("users").document("user1")
                .collection("expense")
                .document(String.valueOf(currentYear))
                .collection(String.valueOf(currentMonth))
                .document("totalExpense");


        // Retrieve the total income and total expense from Firestore
        fetchTotalIncome();
        fetchTotalExpense();

        return view;
    }

    private void fetchTotalIncome() {
        totalIncomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalIncome = documentSnapshot.getDouble("total").doubleValue(); // Assuming the field is named "total"
                    incomeTextView.setText("₹" + totalIncome);
                } else {
                    incomeTextView.setText("₹0.00");
                }

                // Update the balance after fetching income
                updateBalance();
            } else {
                incomeTextView.setText("Failed to load income data");
            }
        });
    }

    private void fetchTotalExpense() {
        totalExpenseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalExpense = documentSnapshot.getDouble("total").doubleValue(); // Assuming the field is named "total"
                    expenseTextView.setText("₹" + totalExpense);
                } else {
                    expenseTextView.setText("₹0.00");
                }

                // Calculate and update the balance
                updateBalance();
            } else {
                expenseTextView.setText("Failed to load expense data");
            }
        });
    }

    private void updateBalance() {
        double balance = totalIncome - totalExpense;

        // Update the balance in the TextView
        balanceTextView.setText("Balance: ₹" + balance);

        // Store the balance in Firebase
    }
}
