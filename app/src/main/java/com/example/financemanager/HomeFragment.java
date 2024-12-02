package com.example.financemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    private RecyclerView recyclerView;
    private TransactionAdapter incomeAdapter;
    private List<TransactionModel> incomeList;


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
        TextView seeall =view.findViewById(R.id.tv_see_all);

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
        seeall.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TransactionDetailsActivity.class);
            startActivity(intent);
        });

        incomeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), IncomeDetailsActivity.class);
            startActivity(intent);
        });

        // Set up spending layout click listener
        spendingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SpendingDetailsActivity.class);
            startActivity(intent);
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Initialize the data list
        incomeList = new ArrayList<>();

        incomeAdapter = new TransactionAdapter(incomeList);
        recyclerView.setAdapter(incomeAdapter);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the year and month (You can replace this with actual data)
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);

        // Fetch income data from Firestore
        fetchTransactionData(year, month);


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

    private void fetchTransactionData(String year, String month) {
        firestore.collection("users")
                .document(userId)
                .collection("transaction")
                .document(year)
                .collection(month)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        incomeList.clear(); // Clear any existing data

                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // SimpleDateFormat to parse and compare dates and times
                            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Skip the "totalIncome" document
                                if ("totalIncome".equals(document.getId())) {
                                    Log.d("IncomeAdapter", "Skipping entry with ID: totalIncome");
                                    continue;
                                }

                                // Safely retrieve 'amount', 'category', 'date', 'time', and 'type'
                                Double amount = document.getDouble("amount");
                                String amountString = String.valueOf(amount != null ? amount : 0.0);
                                String category = document.getString("category") != null ? document.getString("category") : "Unknown";
                                String date = document.getString("date") != null ? document.getString("date") : "01 Jan 1970";
                                String time = document.getString("time") != null ? document.getString("time") : "12:00 AM";
                                String type = document.getString("type");

                                // Add the transaction to the list
                                incomeList.add(new TransactionModel(amountString, category, date, time, type));
                            }

                            // Sort the list by date and time in descending order
                            Collections.sort(incomeList, (o1, o2) -> {
                                try {
                                    String dateTime1 = o1.getDate() + " " + o1.getTime();
                                    String dateTime2 = o2.getDate() + " " + o2.getTime();

                                    Date date1 = formatter.parse(dateTime1);
                                    Date date2 = formatter.parse(dateTime2);

                                    return date2.compareTo(date1); // Descending order
                                } catch (ParseException e) {
                                    Log.e("IncomeAdapter", "Error parsing date/time", e);
                                    return 0; // Keep order if parsing fails
                                }
                            });

                            // Limit to the top 4 elements after sorting
                            if (incomeList.size() > 4) {
                                incomeList.subList(4, incomeList.size()).clear(); // Remove elements beyond the 4th
                            }

                            // Notify the adapter that the data has changed
                            incomeAdapter.notifyDataSetChanged();
                        }

                    } else {
                        // Handle any errors
                        Log.e("IncomeAdapter", "Error getting documents: " + task.getException());
                    }
                });
    }

}
