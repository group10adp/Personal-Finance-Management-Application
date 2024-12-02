package com.example.financemanager;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetSetupActivity extends AppCompatActivity {

    private TextView selectedMonthYear, monthlyButton, yearlyButton;
    private EditText budgetLimitInput;
    private Button nextButton;
    private Calendar calendar;
    private boolean isYearlySelected = false; // Default to Monthly

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setup);

        // Initialize views
        selectedMonthYear = findViewById(R.id.selected_month_year);
        monthlyButton = findViewById(R.id.monthly_button);
        yearlyButton = findViewById(R.id.yearly_button);
        budgetLimitInput = findViewById(R.id.budget_limit_input);
        nextButton = findViewById(R.id.next_button);

        // Set up the calendar instance
        calendar = Calendar.getInstance();
        updateSelectedMonthYear();

        // Set up toggle behavior for Monthly and Yearly
        setupToggleButtons();

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
            // TODO: Implement logic for next action (e.g., save data, navigate to another activity)
        });
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
        } else {
            dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
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
