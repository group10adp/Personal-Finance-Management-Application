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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {

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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        // Sample data without labels


        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize the TextViews
        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView); // New TextView for balance
        time_of_day=view.findViewById(R.id.time_of_day);

        time_of_day.setText("Good "+getTimeOfDay());

        profile=view.findViewById(R.id.profile);

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
                Color.parseColor("#43A047"),  // Green shade
                Color.parseColor("#E53935") // Red shade
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

        // Bar chart dataset
        BarDataSet barDataSet = new BarDataSet(entries, "Income vs Spending");
        barDataSet.setColors(
                Color.parseColor("#43A047"), // Green shade
                Color.parseColor("#E53935")  // Red shade
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

        // Dynamically create a legend on the right
        LinearLayout legendContainer = view.findViewById(R.id.legend_container); // Add this in your layout XML
        String[] labels = {"Income","Spending"};
        int[] colors = {Color.parseColor("#43A047"),Color.parseColor("#E53935")};

        for (int i = 0; i < labels.length; i++) {
            View legendItem = inflater.inflate(R.layout.legend_item, legendContainer, false); // Create a custom layout for legend
            TextView label = legendItem.findViewById(R.id.legend_label);
            View colorIndicator = legendItem.findViewById(R.id.legend_color);

            label.setText(labels[i]);
            colorIndicator.setBackgroundColor(colors[i]);

            legendContainer.addView(legendItem);
        }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

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
            updatePieChart(pieChart); // Update pie chart after income is fetched
            updateBarChart((BarChart) getView().findViewById(R.id.barChart));
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
            updatePieChart(pieChart); // Update pie chart after expense is fetched
            updateBarChart((BarChart) getView().findViewById(R.id.barChart));
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


    private void pieChart(){

    }

    public static String getTimeOfDay() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // Determine the time of day
        if (hourOfDay >= 5 && hourOfDay < 12) {
            return "Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }

    private void updatePieChart(PieChart pieChart) {
        if (balance == 0) {
            // Avoid division by zero

            return;
        }

        float incomePercentage = (float) (totalIncome / balance * 100);
        float expensePercentage = (float) (totalExpense / balance * 100);


        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(incomePercentage, "Income"));
        pieEntries.add(new PieEntry(expensePercentage, "Spending"));

        // Customize dataset
        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(
                Color.parseColor("#43A047"),  // Green shade
                Color.parseColor("#E53935") // Red shade
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
    }

    private void updateBarChart(BarChart barChart) {
        if (totalIncome == 0 && totalExpense == 0) {
            // Avoid rendering an empty chart

            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totalIncome, "Income"));
        entries.add(new BarEntry(1, (float) totalExpense, "Spending"));

        // Bar chart dataset
        BarDataSet barDataSet = new BarDataSet(entries, "Income vs Spending");
        barDataSet.setColors(
                Color.parseColor("#43A047"), // Green shade
                Color.parseColor("#E53935")  // Red shade
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
    }

}
