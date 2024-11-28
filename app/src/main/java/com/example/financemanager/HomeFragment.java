package com.example.financemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView incomeTextView, expenseTextView, balanceTextView, time_of_day;
    private LinearLayout incomeLayout, spendingLayout; // Added spendingLayout for Spending section click
    private FirebaseFirestore firestore;
    private DocumentReference totalIncomeRef, totalExpenseRef;

    private ImageView profile;

    private double totalIncome = 0.0, totalExpense = 0.0;
    private double balance;

    FirebaseAuth auth;

    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize the TextViews
        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        time_of_day = view.findViewById(R.id.time_of_day);
        time_of_day.setText("Good " + getTimeOfDay());

        profile = view.findViewById(R.id.profile);
        incomeLayout = view.findViewById(R.id.incomeLayout); // Income layout reference
        spendingLayout = view.findViewById(R.id.spendingLayout); // Spending layout reference

        // Initialize Firebase Firestore instance
        firestore = FirebaseFirestore.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        totalIncomeRef = firestore.collection("users").document(userId)
                .collection("income")
                .document(String.valueOf(currentYear))
                .collection(String.valueOf(currentMonth))
                .document("totalIncome");

        totalExpenseRef = firestore.collection("users").document(userId)
                .collection("expense")
                .document(String.valueOf(currentYear))
                .collection(String.valueOf(currentMonth))
                .document("totalExpense");

        // Retrieve the total income and expense
        fetchTotalExpenseIncome();

        // Set up profile click listener
        profile.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        });

        // Set up income layout click listener
        incomeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), IncomeDetailsActivity.class);
            startActivity(intent);
        });

        // Set up spending layout click listener
        spendingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SpendingDetailsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchTotalExpenseIncome() {
        totalIncomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalIncome = documentSnapshot.getDouble("total").doubleValue();
                    incomeTextView.setText("₹" + totalIncome);
                } else {
                    incomeTextView.setText("₹0.00");
                }
            } else {
                incomeTextView.setText("Failed to load income data");
            }
            updateBalance();
        });

        totalExpenseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalExpense = documentSnapshot.getDouble("total").doubleValue();
                    expenseTextView.setText("₹" + totalExpense);
                } else {
                    expenseTextView.setText("₹0.00");
                }
            } else {
                expenseTextView.setText("Failed to load expense data");
            }
            updateBalance();
        });
    }

    private void updateBalance() {
        balance = totalIncome - totalExpense;
        String formattedBalance = String.format("Balance: ₹%.2f", balance);
        balanceTextView.setText(formattedBalance);
    }

    public static String getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 5 && hourOfDay < 12) {
            return "Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }
}
