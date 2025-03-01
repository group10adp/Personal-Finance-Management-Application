package com.example.financemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class BudgetSetupActivity extends AppCompatActivity {

    private TextView selectedMonthYear, monthlyButton, yearlyButton;
    private EditText budgetLimitInput;
    private Button nextButton;
    private Calendar calendar;
    private boolean isYearlySelected = false; // Default to Monthly
    private String selectedDateValue; // Stores the selected month/year value
    Button joinBudgetTextView,aiBudgetTextView;
    String month,year;

    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setup);

        year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);

        // Initialize views
        selectedMonthYear = findViewById(R.id.selected_month_year);
        monthlyButton = findViewById(R.id.monthly_button);
        yearlyButton = findViewById(R.id.yearly_button);
        budgetLimitInput = findViewById(R.id.budget_limit_input);
        nextButton = findViewById(R.id.next_button);

        // Disable the Next button by default
        nextButton.setEnabled(true);

        // Set up the calendar instance
        calendar = Calendar.getInstance();
        updateSelectedMonthYear();

        firestore = FirebaseFirestore.getInstance();

        // Set up toggle behavior for Monthly and Yearly
        setupToggleButtons();

        joinBudgetTextView = findViewById(R.id.join_budget);
        aiBudgetTextView = findViewById(R.id.ai_budget);

        aiBudgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog progressDialog = new AlertDialog.Builder(BudgetSetupActivity.this)
                        .setView(R.layout.custom_loading_dialog) // Reference your custom layout
                        .setCancelable(false)
                        .create();

                // Start the next activity
                Intent intent = new Intent(BudgetSetupActivity.this, AiBudgetActivity.class);
                // Pass a flag to indicate a loading operation is ongoing
                intent.putExtra("showLoading", true);
                startActivity(intent);

                // Finish the current activity
                finish();
            }
        });

        joinBudgetTextView.setOnClickListener(v -> showJoinBudgetDialog());

        // Set a click listener on the "Budget for" section to show a picker dialog
        selectedMonthYear.setOnClickListener(view -> {
            if (isYearlySelected) {
                showYearPickerDialog(); // Year-only picker
            } else {
                showCustomMonthPickerDialog(); // Month-Year picker
            }
        });

        // Add a TextWatcher to the budget limit input to enable/disable the "Next" button
        budgetLimitInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Set an OnClickListener for the "Next" button
        nextButton.setOnClickListener(v -> {
            // Get the total budget amount entered by the user
            String budgetLimit = budgetLimitInput.getText().toString().trim();

            // Check if the input is valid
            if (!budgetLimit.isEmpty()) {
                // Convert the input to a number
                double totalBudget = Double.parseDouble(budgetLimit);

                // Create an intent to go to the next activity
                Intent intent = new Intent(BudgetSetupActivity.this, BudgetDetailsActivity.class);

                // Pass the total budget amount and selected date value to the next activity
                intent.putExtra("totalBudget", totalBudget);
                intent.putExtra("selectedDateValue", selectedDateValue);
                //Log.d("Selected23",""+selectedDateValue);

                // Start the next activity
                startActivity(intent);
            }
        });
    }

    private void showJoinBudgetDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_join_budget);

        EditText budgetIdInput = dialog.findViewById(R.id.budget_id_input);
        Button submitButton = dialog.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> {
            String budgetId = budgetIdInput.getText().toString().trim();
            if (!budgetId.isEmpty()) {

                //fetchBudgetDetails(budgetId, year);

                Intent intent = new Intent(BudgetSetupActivity.this, BudgetDisplayActivityTemp.class);
                intent.putExtra("selectedDateValue", month);
                intent.putExtra("userId", budgetId);
                startActivity(intent);
                finish();
                //FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//
//
//                // Reference to the "budget" collection under the specified budgetId, year "2025", and month "1"
//                CollectionReference sourceBudgetCollectionRef = firestore.collection("users")
//                        .document(budgetId)
//                        .collection("budget")
//                        .document("2025") // Replace with the actual year if needed
//                        .collection("1"); // Replace with the actual month if needed
//
//                // Get all documents in the "budget" collection
//                sourceBudgetCollectionRef.get()
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful() && task.getResult() != null) {
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    // Log document data (fields)
//                                    Log.d("BudgetData", "Document ID: " + document.getId());
//                                    Log.d("BudgetData", "Fields: " + document.getData());
//
//                                    // Check for nested collections dynamically
//                                    //fetchAllNestedCollections(document.getReference());
//                                }
//                            } else {
//                                Log.e("FirestoreError", "Error getting documents: ", task.getException());
//                            }
//                        });

            } else {
                Toast.makeText(BudgetSetupActivity.this, "Please enter a valid Budget ID", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void fetchBudgetDetails(String budgetId, String year) {

        // Fetch total budget
        firestore.collection("users")
                .document(budgetId)
                .collection("budget")
                .document(year)
                .collection(month)
                .document("total-budget")
                .get()
                .addOnSuccessListener(totalBudgetSnapshot -> {
                    double totalBudget = totalBudgetSnapshot.getDouble("amount");


                    // Fetch remaining budget
                    firestore.collection("users")
                            .document(budgetId)
                            .collection("budget")
                            .document(year)
                            .collection(month)
                            .document("total-remaining-budget")
                            .get()
                            .addOnSuccessListener(remainingBudgetSnapshot -> {
                                if (remainingBudgetSnapshot.exists()) {
                                    double remainingBudget = remainingBudgetSnapshot.getDouble("amount");

                                    // Calculate the percentage

                                } else {
                                    Toast.makeText(this, "Remaining budget not found!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch remaining budget.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch total budget.", Toast.LENGTH_SHORT).show());
    }



    // Sets up the behavior for the toggle buttons
    private void setupToggleButtons() {
        monthlyButton.setOnClickListener(view -> {
            isYearlySelected = false;
            updateToggleUI();
            updateSelectedMonthYear();
        });

        yearlyButton.setOnClickListener(view -> {
            isYearlySelected = true;
            updateToggleUI();
            updateSelectedMonthYear();
        });
    }

    // Updates the UI of the toggle buttons based on the selected mode
    private void updateToggleUI() {
        if (isYearlySelected) {
            yearlyButton.setBackgroundResource(R.drawable.toggle_selected);
            monthlyButton.setBackgroundResource(R.drawable.toggle_unselected);
        } else {
            monthlyButton.setBackgroundResource(R.drawable.toggle_selected);
            yearlyButton.setBackgroundResource(R.drawable.toggle_unselected);
        }
    }

    // Updates the "selectedMonthYear" TextView with the appropriate text
    private void updateSelectedMonthYear() {
        SimpleDateFormat dateFormat;
        if (isYearlySelected) {
            dateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
            selectedDateValue = String.valueOf(calendar.get(Calendar.YEAR));
        } else {
            selectedDateValue = String.valueOf(calendar.get(Calendar.MONTH) + 1);
            dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            selectedMonthYear.setText(dateFormat.format(calendar.getTime()));
        }
        selectedMonthYear.setText(dateFormat.format(calendar.getTime()));
    }

    // Shows a custom month and year picker dialog
    private void showCustomMonthPickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_month_year_picker);

        // Initialize the NumberPickers for month and year
        NumberPicker monthPicker = dialog.findViewById(R.id.month_picker);
        NumberPicker yearPicker = dialog.findViewById(R.id.year_picker);
        Button confirmButton = dialog.findViewById(R.id.confirm_button);

        // Set up month picker
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"});
        monthPicker.setValue(calendar.get(Calendar.MONTH));

        // Set up year picker
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 50);
        yearPicker.setMaxValue(currentYear + 50);
        yearPicker.setValue(calendar.get(Calendar.YEAR));

        // Confirm button click listener
        confirmButton.setOnClickListener(view -> {
            // Update the calendar with selected month and year
            calendar.set(Calendar.MONTH, monthPicker.getValue());
            calendar.set(Calendar.YEAR, yearPicker.getValue());
            updateSelectedMonthYear();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Shows a year picker dialog for the Yearly mode
    private void showYearPickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_picker);

        // Initialize the NumberPicker for year
        NumberPicker yearPicker = dialog.findViewById(R.id.year_picker);
        Button confirmButton = dialog.findViewById(R.id.confirm_button);

        // Set up year picker
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 50);
        yearPicker.setMaxValue(currentYear + 50);
        yearPicker.setValue(calendar.get(Calendar.YEAR));

        // Confirm button click listener
        confirmButton.setOnClickListener(view -> {
            // Update the calendar with the selected year
            calendar.set(Calendar.YEAR, yearPicker.getValue());
            updateSelectedMonthYear();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Validates input and enables/disables the "Next" button
    private void validateInput() {
        String budgetLimit = budgetLimitInput.getText().toString().trim();
        nextButton.setEnabled(!budgetLimit.isEmpty());
    }


}