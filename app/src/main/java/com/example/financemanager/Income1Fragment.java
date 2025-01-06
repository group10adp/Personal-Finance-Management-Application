package com.example.financemanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Income1Fragment extends Fragment {

    private TextView incomeTextView, expenseTextView, balanceTextView, time_of_day;
    private FirebaseFirestore firestore;
    private DocumentReference totalIncomeRef, totalExpenseRef;

    private ImageView profile;

    private double totalIncome = 0.0, totalExpense = 0.0;
    private double balance;

    FirebaseAuth auth;

    private String userId;
    PieChart pieChart;
    BarChart barChart;
    private LinearLayout incomeLayout, spendingLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_income1, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        // Sample data without labels


        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());


        // Initialize the TextViews
        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView); // New TextView for balance

        incomeLayout = view.findViewById(R.id.incomeLayout); // Income layout reference
        spendingLayout = view.findViewById(R.id.spendingLayout);

        // Initialize Firebase Firestore instance
        firestore = FirebaseFirestore.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        // Firebase references for total income, total expense, and balance
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


        // Retrieve the total income and total expense from Firestore
        fetchTotalExpenseIncome();


        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry((float)50));
        pieEntries.add(new PieEntry((float)50));

        // Customize dataset
        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(
                Color.parseColor("#FFBF00")
        ); // Custom colors
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        // Create PieData and set it to the chart
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Remove labels from the chart
        pieChart.setDrawEntryLabels(false);

        // Customize the chart
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false); // Hide the description
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getLegend().setEnabled(false); // Hide the legend

        // Refresh the chart
        pieChart.invalidate();

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) 50));
        entries.add(new BarEntry(1, (float) 50));
        entries.add(new BarEntry(0, (float) 50));
        entries.add(new BarEntry(1, (float) 50));
        entries.add(new BarEntry(0, (float) 50));
        entries.add(new BarEntry(1, (float) 50));
        entries.add(new BarEntry(0, (float) 50));
        entries.add(new BarEntry(1, (float) 50));
        entries.add(new BarEntry(0, (float) 50));
        entries.add(new BarEntry(1, (float) 50));

        // Bar chart dataset
        BarDataSet barDataSet = new BarDataSet(entries, "Income vs Spending");
        barDataSet.setColors(
                Color.parseColor("#FFBF00")
        ); // Custom colors
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f); // Bar width

        // Configure the BarChart
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false); // Hide description
        barChart.getLegend().setEnabled(false); // Hide legend
        barChart.setTouchEnabled(false); // Disable interaction

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // Hide vertical grid lines
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0 ? "Income" : "Spending";
            }
        });

        // Customize Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start at 0
        leftAxis.setDrawGridLines(false); // Hide horizontal grid lines

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Hide right axis

        // Refresh the chart
        barChart.invalidate();

        incomeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), IncomeDetailsActivity.class);
            startActivity(intent);
        });

        // Set up spending layout click listener
        spendingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SpendingDetailsActivity.class);
            startActivity(intent);
        });

        // Dynamically create a legend on the right

        fetchIncomeByCategory();

        return view;
    }

    private void fetchTotalExpenseIncome() {
        totalIncomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalIncome = documentSnapshot.getDouble("total").doubleValue(); // Assuming the field is named "total"
                    incomeTextView.setText("₹" + totalIncome);
                } else {
                    incomeTextView.setText("₹0.00");
                }
            } else {
                incomeTextView.setText("Failed to load income data");
            }
            updateBalance();
            //updatePieChart(pieChart); // Update pie chart after income is fetched
            //updateBarChart((BarChart) getView().findViewById(R.id.barChart));
        });

        totalExpenseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalExpense = documentSnapshot.getDouble("total").doubleValue(); // Assuming the field is named "total"
                    expenseTextView.setText("₹" + totalExpense);
                } else {
                    expenseTextView.setText("₹0.00");
                }
            } else {
                expenseTextView.setText("Failed to load expense data");
            }
            updateBalance();
            //updatePieChart(pieChart); // Update pie chart after expense is fetched
            //updateBarChart((BarChart) getView().findViewById(R.id.barChart));
        });
    }



    private double updateBalance() {
        balance = totalIncome - totalExpense;

// Format the balance to 2 decimal places
        String formattedBalance = String.format("Balance: ₹%.2f", balance);

// Update the balance in the TextView
        balanceTextView.setText(formattedBalance);
        return balance;
    }

    private void updateCategoryPieChart(Map<String, Double> categoryTotals) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        List<LegendItem> legendItems = new ArrayList<>();

        int[] colors = {
                Color.parseColor("#DE3163"), Color.parseColor("#FFBF00"), Color.parseColor("#6495ED"),
                Color.parseColor("#DFFF00"), Color.parseColor("#CCCCFF"), Color.parseColor("#FF7F50"),
                Color.parseColor("#10f72f"), Color.parseColor("#1064f7"), Color.parseColor("#f710b1"),
                Color.parseColor("#f71010"), Color.parseColor("#3d867d"), Color.parseColor("#566573"),
                Color.parseColor("#800080"), Color.parseColor("#ccbf00")
        };

        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            // Check if category is null or empty and skip it
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                continue; // Skip this entry
            }

            String valueAsString = String.format("₹%.1f", entry.getValue());


            pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));

            // Add legend item
            legendItems.add(new LegendItem(entry.getKey(), colors[colorIndex % colors.length],valueAsString));
            colorIndex++;
        }


        // Update PieChart
        PieDataSet dataSet = new PieDataSet(pieEntries, "Categories");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();

        // Update Legend RecyclerView
        RecyclerView legendRecyclerView = getView().findViewById(R.id.legendRecyclerView);
        LegendAdapter legendAdapter = new LegendAdapter(legendItems);
        legendRecyclerView.setAdapter(legendAdapter);
        legendRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1)); // 3 legends per row
    }



    private void updateBarChart(BarChart barChart, Map<String, Double> categoryIncome) {
        if (categoryIncome == null || categoryIncome.isEmpty()) {
            // Avoid rendering an empty chart
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> categoryLabels = new ArrayList<>();

        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : categoryIncome.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                continue; // Skip invalid entries
            }

            // Add category name and income value to the chart
            entries.add(new BarEntry(colorIndex, entry.getValue().floatValue()));
            categoryLabels.add(entry.getKey());
            colorIndex++;
        }

        // Bar chart dataset
        BarDataSet barDataSet = new BarDataSet(entries, "Category-wise Income");
        barDataSet.setColors(
                Color.parseColor("#DE3163"), Color.parseColor("#FFBF00"), Color.parseColor("#6495ED"),
                Color.parseColor("#DFFF00"), Color.parseColor("#CCCCFF"), Color.parseColor("#FF7F50"),
                Color.parseColor("#10f72f"), Color.parseColor("#1064f7"), Color.parseColor("#f710b1"),
                Color.parseColor("#f71010"), Color.parseColor("#3d867d"), Color.parseColor("#566573"),
                Color.parseColor("#800080"), Color.parseColor("#ccbf00")
        ); // Custom colors
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f); // Bar width

        // Configure the BarChart
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false); // Hide description
        barChart.getLegend().setEnabled(false); // Hide legend

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // Hide vertical grid lines
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < categoryLabels.size()) {
                    return categoryLabels.get(index); // Show category name
                }
                return "";
            }
        });
        xAxis.setLabelRotationAngle(-90f);
        barChart.setExtraBottomOffset(55f);

        // Customize Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start at 0
        leftAxis.setDrawGridLines(false); // Hide horizontal grid lines

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Hide right axis

        // Refresh the chart
        barChart.invalidate();
    }


    private void fetchIncomeByCategory() {
        // Reference to the user's income collection for the current month and year
        CollectionReference incomeRef = firestore.collection("users").document(userId)
                .collection("income")
                .document(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))
                .collection(String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));

        // Query the income collection
        incomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // HashMap to store the total income by category
                    Map<String, Double> categoryTotals = new HashMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String category = document.getString("category");

                        // Use a default value if "amount" is missing or null
                        Double amount = document.getDouble("amount");
                        if (amount == null) {
                            Log.w("fetchIncomeByCategory", "Missing or null amount for document: " + document.getId());
                            amount = 0.0; // Default to 0 if the amount is null
                        }

                        // Add the amount to the corresponding category
                        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                    }

                    // Log or use the category totals as needed
                    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                        Log.d("CategoryTotals", "Category: " + entry.getKey() + ", Total: ₹" + entry.getValue());
                    }

                    // Update the UI (e.g., pie chart or other views)
                    updateCategoryPieChart(categoryTotals);
                    updateBarChart(barChart,categoryTotals);
                } else {
                    Log.d("CategoryTotals", "No income data found for categories.");
                }
            } else {
                Log.e("FirestoreError", "Error fetching income data by category", task.getException());
            }
        });
    }


}