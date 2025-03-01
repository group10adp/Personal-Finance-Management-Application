package com.example.financemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private TextView incomeTextView, expenseTextView, balanceTextView, time_of_day,username;
    private LinearLayout incomeLayout, spendingLayout; // Added spendingLayout for Spending section click
    private FirebaseFirestore firestore;
    private DocumentReference totalIncomeRef, totalExpenseRef, totalIncomeRef1, totalExpenseRef1;

    private ImageView profile;

    private double totalIncome = 0.0, totalExpense = 0.0;
    private double balance;

    FirebaseAuth auth;

    private String userId;

    private RecyclerView recyclerView;
    private TransactionAdapter incomeAdapter;
    private List<TransactionModel> incomeList;
    TextView popUpText;
    private ShimmerFrameLayout shimmerLayout;
    private View mainContent;



    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        ImageView dropDown = view.findViewById(R.id.dropDown);
        popUpText=view.findViewById(R.id.popUpText);

        // Initialize the TextViews
        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        time_of_day = view.findViewById(R.id.time_of_day);
        time_of_day.setText("Good " + getTimeOfDay());
        TextView seeall =view.findViewById(R.id.tv_see_all);
        Button manageBudgetButton = view.findViewById(R.id.manageBudgetButton);

        username=view.findViewById(R.id.username);
        profile = view.findViewById(R.id.profile);
        incomeLayout = view.findViewById(R.id.incomeLayout); // Income layout reference
        spendingLayout = view.findViewById(R.id.spendingLayout); // Spending layout reference

        // Initialize Firebase Firestore instance
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the first (and only) key under this userId
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey(); // Get the key
                        //Log.d("RealtimeDB1", "Key retrieved: " + key);
                        username.setText(key);
                        break; // Since there's only one key, exit the loop
                    }
                } else {
                    Log.e("RealtimeDB", "No data found for the given user ID.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RealtimeDB", "Database error: " + databaseError.getMessage());
            }
        });
        firestore = FirebaseFirestore.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 01;

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

        totalIncomeRef1 = firestore.collection("users").document(userId)
                .collection("income")
                .document("totalYearlyIncome");

// Reference to the total yearly expense
        totalExpenseRef1 = firestore.collection("users").document(userId)
                .collection("expense")
                .document("totalYearlyExpense");

        // Retrieve the total income and expense

        dropDown.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_home, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.this_month) {
                    popUpText.setText("This month");
                    fetchTotalExpenseIncome();


                } else if (item.getItemId() == R.id.this_year) {
                    popUpText.setText("This year");
                    fetchTotalYearlyExpenseIncome();

                }

                return true;
            });

            popupMenu.show();
        });

        fetchTotalExpenseIncome();

        // Set up profile click listener
        profile.setOnClickListener(v -> {
            // Open a new activity instead of logging out
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
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

        Button setUpBudgetButton = view.findViewById(R.id.setUpBudgetButton);

        // Set a click listener to open BudgetSetupActivity
        setUpBudgetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BudgetSetupActivity.class);
            startActivity(intent);
        });

        manageBudgetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageBudgetActivity.class);
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
                    Log.d("expense",""+totalExpense+" "+totalIncome);
                } else {
                    incomeTextView.setText("₹0.00");
                }
            } else {
                incomeTextView.setText("₹0.00");
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
                expenseTextView.setText("₹0.00");
            }
            updateBalance();
        });
    }

    private void fetchTotalYearlyExpenseIncome() {
        totalIncomeRef1.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalIncome = documentSnapshot.getDouble("total").doubleValue();
                    incomeTextView.setText("₹" + totalIncome);
                    Log.d("expense",""+totalExpense+" "+totalIncome);
                } else {
                    incomeTextView.setText("₹0.00");
                }
            } else {
                incomeTextView.setText("₹0.00");
            }
            updateBalance();
        });

        totalExpenseRef1.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalExpense = documentSnapshot.getDouble("total").doubleValue();
                    expenseTextView.setText("₹" + totalExpense);
                } else {
                    expenseTextView.setText("₹0.00");
                }
            } else {
                expenseTextView.setText("₹0.00");
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
                                double amountString = amount != null ? amount : 0.0;
                                String category = document.getString("category") != null ? document.getString("category") : "Unknown";
                                String date = document.getString("date") != null ? document.getString("date") : "01 Jan 1970";
                                String time = document.getString("time") != null ? document.getString("time") : "12:00 AM";
                                String type = document.getString("type");
                                String note = document.getString("note");
                                String docId =document.getId();
                                String paymentMode = document.getString("paymentMode");

                                // Add the transaction to the list
                                incomeList.add(new TransactionModel(amountString, category, date, time, type,note,docId,paymentMode));
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
                            if (incomeList.size() > 5) {
                                incomeList.subList(5, incomeList.size()).clear(); // Remove elements beyond the 4th
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

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize shimmer and main content views
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        mainContent = view.findViewById(R.id.mainContent);

        // Start shimmer effect
        shimmerLayout.startShimmer();

        // Simulate data loading (replace with real logic)
        new Handler().postDelayed(() -> {
            // Stop shimmer effect and show main content
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }, 2700); // Simulated delay of 3 seconds
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop shimmer when the fragment is paused
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restart shimmer when the fragment is resumed
        if (shimmerLayout != null) {
            shimmerLayout.startShimmer();
        }
    }


}
