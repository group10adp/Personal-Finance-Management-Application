package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class ExpenseFragment extends Fragment {

    private EditText amountEditText;
    private Button saveButton;
    private TextView dateText;
    private TextView timeText;
    private Spinner categorySpinner, paymentModeSpinner;
    private FirebaseFirestore firestore;

    FirebaseAuth auth;

    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize views
        amountEditText = view.findViewById(R.id.amountEditText);
        saveButton = view.findViewById(R.id.saveButton);
        dateText = view.findViewById(R.id.dateText);
        timeText = view.findViewById(R.id.timeText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        paymentModeSpinner = view.findViewById(R.id.paymentModeSpinner);
        ImageView dateIcon = view.findViewById(R.id.dateIcon);
        ImageView timeIcon = view.findViewById(R.id.timeIcon);

        // Set current date and time
        dateText.setText(getCurrentDate());
        timeText.setText(getCurrentTime());

        dateIcon.setOnClickListener(v -> showDatePicker());
        dateText.setOnClickListener(v -> showDatePicker());
        timeIcon.setOnClickListener(v -> showTimePicker());
        timeText.setOnClickListener(v -> showTimePicker());

        // Set up ArrayAdapter for categorySpinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.category_array, // Use a different array for expense categories
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set up ArrayAdapter for paymentModeSpinner
        ArrayAdapter<CharSequence> paymentModeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.payment_mode_array,
                android.R.layout.simple_spinner_item
        );
        paymentModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentModeSpinner.setAdapter(paymentModeAdapter);

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Set click listener on save button
        saveButton.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void saveExpense() {
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String time = timeText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String paymentMode = paymentModeSpinner.getSelectedItem().toString();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Get current year and month
            String[] dateParts = date.split(" ");
            String year = dateParts[2];
            String month = String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1); // Get the current month in MM format

            // Create an ExpenseEntry object
            ExpenseEntry expenseEntry = new ExpenseEntry(amount, date, time, category, paymentMode);

            // Save expense entry to Firestore
            firestore.collection("users").document(userId)
                    .collection("expense")
                    .document(year)
                    .collection(month)
                    .add(expenseEntry)
                    .addOnSuccessListener(documentReference -> {
                        // Update total expense for the user
                        updateTotalExpense(year, month, amount);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save expense entry.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalExpense(String year, String month, double newExpense) {
        DocumentReference totalExpenseDoc = firestore.collection("users").document(userId)
                .collection("expense").document(year).collection(month).document("totalExpense");

        // Get the current total expense for the month
        totalExpenseDoc.get().addOnSuccessListener(documentSnapshot -> {
            double currentTotal = documentSnapshot.exists() ? documentSnapshot.getDouble("total") : 0.0;
            double updatedTotal = currentTotal + newExpense;

            // Update the total expense for that month
            totalExpenseDoc.set(Map.of("total", updatedTotal))
                    .addOnSuccessListener(aVoid -> {
                        // Update yearly total expense
                        updateYearlyExpense(year, newExpense);
                        Toast.makeText(getContext(), "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                        amountEditText.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update monthly total expense.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load current monthly expense.", Toast.LENGTH_SHORT).show());
    }

    private void updateYearlyExpense(String year, double newExpense) {
        DocumentReference yearlyExpenseDoc = firestore.collection("users").document(userId)
                .collection("expense").document("totalYearlyExpense"); // No need to use the year here

        // Check if the totalYearlyExpense document exists
        yearlyExpenseDoc.get().addOnSuccessListener(documentSnapshot -> {
            double currentYearlyTotal = documentSnapshot.exists() ? documentSnapshot.getDouble("total") : 0.0;
            double updatedYearlyTotal = currentYearlyTotal + newExpense;

            // Set or update the total yearly expense in the document
            yearlyExpenseDoc.set(Map.of("total", updatedYearlyTotal))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update yearly expense.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load current yearly expense.", Toast.LENGTH_SHORT).show());
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(calendar.getTime());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String amPm = selectedHour >= 12 ? "PM" : "AM";
                    int hourIn12Format = selectedHour % 12 == 0 ? 12 : selectedHour % 12;
                    timeText.setText(String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm));
                },
                hour, minute, false);

        timePickerDialog.show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    dateText.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day);

        datePickerDialog.show();
    }

    // Inner class for ExpenseEntry
    public static class ExpenseEntry {
        private double amount;
        private String date;
        private String time;
        private String category;
        private String paymentMode;

        public ExpenseEntry() {
        }

        public ExpenseEntry(double amount, String date, String time, String category, String paymentMode) {
            this.amount = amount;
            this.date = date;
            this.time = time;
            this.category = category;
            this.paymentMode = paymentMode;
        }

        public double getAmount() {
            return amount;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getCategory() {
            return category;
        }

        public String getPaymentMode() {
            return paymentMode;
        }
    }
}
