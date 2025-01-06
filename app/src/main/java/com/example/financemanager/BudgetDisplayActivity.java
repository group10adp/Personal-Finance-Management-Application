package com.example.financemanager;

import static java.security.AccessController.getContext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.DonutProgress;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BudgetDisplayActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private TextView budgetTextView, remainingTextView, spentTextView, monthYear,tv_remark;
    String userId, year, month;
    FirebaseAuth auth;
    private DonutProgress donutProgress;
    private DocumentReference totalIncomeRef, totalExpenseRef;
    PieChart pieChart;
    BarChart barChart;
    Button copy_code;
    private double totalIncome = 0.0, totalExpense = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_display);

        Intent intent = getIntent();
        month = intent.getStringExtra("selectedDateValue");

        auth = FirebaseAuth.getInstance();
        userId = intent.getStringExtra("userId"); // Get userId from the intent
        //Log.d("userId66","jjjj");
        auth = FirebaseAuth.getInstance();

// Check if userId is provided via intent
        if (userId == null || userId.isEmpty()) {
            userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        }
        year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
//        month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        firestore = FirebaseFirestore.getInstance();
        budgetTextView = findViewById(R.id.tv_budget);
        remainingTextView = findViewById(R.id.tv_available_budget);
        spentTextView = findViewById(R.id.tv_total_spent);
        donutProgress = findViewById(R.id.donutProgress);
        monthYear = findViewById(R.id.tv_month_year);
        tv_remark=findViewById(R.id.tv_remark);

        copy_code=findViewById(R.id.copy_code);

        copy_code.setOnClickListener(v -> {
            if (userId != null && !userId.isEmpty()) {
                // Copy the userId to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("User ID", userId);
                clipboard.setPrimaryClip(clip);

                // Show a Toast to inform the user
                Toast.makeText(this, "Budget code copied to clipboard!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            }
        });

        if(year.equals(month)){
            totalIncomeRef = firestore.collection("users").document(userId)
                    .collection("income")
                    .document(String.valueOf(year))
                    .collection(String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1))
                    .document("totalIncome");

            totalExpenseRef = firestore.collection("users").document(userId)
                    .collection("expense")
                    .document(String.valueOf(year))
                    .collection(String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1))
                    .document("totalExpense");
        }
        else{
            totalIncomeRef = firestore.collection("users").document(userId)
                    .collection("income")
                    .document(String.valueOf(year))
                    .collection(String.valueOf(month))
                    .document("totalIncome");

            totalExpenseRef = firestore.collection("users").document(userId)
                    .collection("expense")
                    .document(String.valueOf(year))
                    .collection(String.valueOf(month))
                    .document("totalExpense");
        }


        fetchBudgetDetails(userId, year);
        fetchTotalExpenseIncome();

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry((float) 50));
        pieEntries.add(new PieEntry((float) 50));

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

        fetchIncomeByCategory();

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

    }

    private void fetchBudgetDetails(String userId, String year) {

        // Fetch total budget
        firestore.collection("users")
                .document(userId)
                .collection("budget")
                .document(year)
                .collection(month)
                .document("total-budget")
                .get()
                .addOnSuccessListener(totalBudgetSnapshot -> {
                        double totalBudget = totalBudgetSnapshot.getDouble("amount");


                        // Fetch remaining budget
                        firestore.collection("users")
                                .document(userId)
                                .collection("budget")
                                .document(year)
                                .collection(month)
                                .document("total-remaining-budget")
                                .get()
                                .addOnSuccessListener(remainingBudgetSnapshot -> {
                                    if (remainingBudgetSnapshot.exists()) {
                                        double remainingBudget = remainingBudgetSnapshot.getDouble("amount");

                                        // Calculate the percentage
                                        double percentageRemaining = (remainingBudget / totalBudget) * 100;
                                        percentageRemaining = 100 - percentageRemaining;
                                        String formattedPercentage = String.format("%.1f%%", percentageRemaining);
                                        donutProgress.setStartingDegree(270);
                                        donutProgress.setProgress((float) percentageRemaining);
                                        donutProgress.setText(formattedPercentage);

                                        String message;

                                        if (percentageRemaining > 100) {
                                            message = "Alert! You've reached your budget limit.";
                                            tv_remark.setText(message);
                                            tv_remark.setTextColor(Color.parseColor("#FF0000")); // Red color for alert (hex code)
                                        } else if (percentageRemaining > 50) {
                                            message = "At this rate, you may exceed your budget soon.";
                                            tv_remark.setText(message);
                                            tv_remark.setTextColor(Color.parseColor("#FFA500")); // Orange color for warning (hex code)
                                        } else {
                                            message = "Great job! You are under your budget.";
                                            tv_remark.setText(message);
                                            tv_remark.setTextColor(Color.parseColor("#4CAF50")); // Green color for good progress (hex code)
                                        }



                                        // Update UI

                                        budgetTextView.setText(String.format("Budget: ₹%s", totalBudget));
                                        remainingTextView.setText(String.valueOf(remainingBudget));

                                        // Retrieve and display month and year
                                        String monthYearText = formatMonthYear(Integer.parseInt(month), Integer.parseInt(year));
                                        monthYear.setText(monthYearText);
                                    } else {
                                        Toast.makeText(this, "Remaining budget not found!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch remaining budget.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch total budget.", Toast.LENGTH_SHORT).show());
    }

    // Utility method to format the month and year
    private String formatMonthYear(int month, int year) {
        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        try{
            return monthNames[month - 1] + " " + year;
        }
        catch (Exception e){
            return year+"";
        }

    }



    private void fetchTotalExpenseIncome() {
        totalIncomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalIncome = documentSnapshot.getDouble("total").doubleValue(); // Assuming the field is named "total"
                } else {

                }
            } else {

            }
        });

        totalExpenseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    totalExpense = documentSnapshot.getDouble("total").doubleValue(); // Assuming the field is named "total"
                    spentTextView.setText(String.format(String.valueOf(totalExpense)));

                } else {
                }
            } else {
            }

        });
    }


    private void fetchIncomeByCategory() {
        // Reference to the user's income collection for the current month and year
        CollectionReference incomeRef = firestore.collection("users").document(userId)
                .collection("expense")
                .document(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))
                .collection(String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));

        // Query the income collection
        incomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // HashMap to store the total income by category
                    Map<String, Double> categoryTotals = new HashMap<>();

                    // Categories to process
                    String[] categories = {
                            "Others", "Transport", "Shopping", "Entertainment", "Health", "Education",
                            "Bill", "Investment", "Rent", "Tax", "Insurance", "Money Transfer"
                    };

                    // Atomic counter to track the number of processed categories
                    AtomicInteger counter = new AtomicInteger(0);

                    for (String category : categories) {
                        fetchCategoryData(userId, year, month, category, categoryTotals, () -> {
                            // Increment the counter and check if all categories are processed
                            if (counter.incrementAndGet() == categories.length) {
                                // All categories processed
                                Log.d("FinalCategoryTotals", "Category Totals: " + categoryTotals);
                                // Update the UI
                                updateCategoryPieChart(categoryTotals);
                                updateBarChart(barChart, categoryTotals);
                            }
                        });
                    }
                } else {
                    Log.d("CategoryTotals", "No income data found for categories.");
                }
            } else {
                Log.e("FirestoreError", "Error fetching income data by category", task.getException());
            }
        });
    }

    // Modified fetchCategoryData with a callback
    private void fetchCategoryData(String userId, String year, String month, String category,
                                   Map<String, Double> categoryTotals, Runnable onComplete) {
        // Use the category name to dynamically reference the document
        DocumentReference budgetRef = firestore.collection("users")
                .document(userId)
                .collection("budget")
                .document(year)
                .collection(month)
                .document("category-based-remaining-budget")
                .collection(category)  // Use the category dynamically
                .document("remaining-budget-entry");

        budgetRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve the remaining budget for the category
                    Double amount = document.getDouble("amount");
                    if (amount != null) {
                        synchronized (categoryTotals) {
                            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                        }
                        Log.d("CategoryData", "Category: " + category + ", Remaining Budget: ₹" + amount);
                    } else {
                        Log.w("CategoryData", "No remaining budget found for category: " + category);
                    }
                } else {
                    Log.d("CategoryData", "No document found for category: " + category);
                }
            } else {
                Log.e("FirestoreError", "Error fetching data for category: " + category, task.getException());
            }
            // Trigger the completion callback
            onComplete.run();
        });
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
            legendItems.add(new LegendItem(entry.getKey(), colors[colorIndex % colors.length], valueAsString));
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
        RecyclerView legendRecyclerView = findViewById(R.id.legendRecyclerView);
        LegendAdapter legendAdapter = new LegendAdapter(legendItems);
        legendRecyclerView.setAdapter(legendAdapter);
        legendRecyclerView.setLayoutManager(new GridLayoutManager(BudgetDisplayActivity.this, 1)); // 3 legends per row
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

}


