package com.example.financemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class InvestmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private InvestmentAdapter adapter;
    private List<Investment> investmentList;

    private FirebaseFirestore firestore;
    FirebaseAuth auth;
    private String userId;
    private ShimmerFrameLayout shimmerLayout;
    private View mainContent;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_investments, container, false);

        //getActivity().getWindow().setStatusBarColor(Color.parseColor("#121212"));

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());
        firestore = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_investments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize data
        investmentList = new ArrayList<>();
        adapter = new InvestmentAdapter(investmentList);
        recyclerView.setAdapter(adapter);

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);

        fetchIncomeData(year, month);

        // FloatingActionButton to add new investment
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AddInvestment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void fetchIncomeData(String year, String month) {
        firestore.collection("users")
                .document(userId)
                .collection("investment")
                .document(year)
                .collection(month)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        investmentList.clear(); // Clear any existing data

                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // SimpleDateFormat to parse and compare dates and times
                            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Check if the document ID is "totalIncome" and skip it

                                // Safely retrieve 'amount' as Double
                                Double amount = document.getDouble("amount");
                                if (amount == null) {
                                    amount = 0.0; // Assign default value if 'amount' is null
                                }

                                // Convert amount (Double) to String
                                String amountString = String.valueOf(amount);

                                // Get 'category', 'date', and 'time' safely

                                String mutualFund = document.getString("mutualFund");
                                if (mutualFund == null) {
                                    mutualFund = "Others"; // Default date for invalid entries
                                }

                                String returnRate = document.getString("returnRate");
                                if (returnRate == null) {
                                    returnRate = "0"; // Default time for invalid entries
                                }

                                String date = document.getString("date");
                                if (date == null) {
                                    date = "01 Jan 1970"; // Default date for invalid entries
                                }

                                String time = document.getString("time");
                                if (time == null) {
                                    time = "12:00 AM"; // Default time for invalid entries
                                }

                                String docId =document.getId();

                                // Create a new IncomeModel object and add it to the list
                                investmentList.add(new Investment(mutualFund,returnRate,amount,date,time,docId));
                            }

                            // Sort the list based on date and time in descending order
                            Collections.sort(investmentList, (o1, o2) -> {
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

                            //Log.d("IncomeAdapter", "Sorted Income List: " + incomeList);

                            // Notify the adapter that the data has changed
                            adapter.notifyDataSetChanged();
                        }

                    } else {
                        // Handle any errors
                        System.out.println("Error getting documents: " + task.getException());
                    }
                });
    }

    @Override
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
        }, 3000); // Simulated delay of 3 seconds
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
