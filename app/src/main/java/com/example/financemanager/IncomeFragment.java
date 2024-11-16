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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class IncomeFragment extends Fragment {

    private EditText amountEditText;
    private Button saveButton;
    private TextView dateText;
    private TextView timeText;
    private Spinner categorySpinner, paymentModeSpinner;
    private DatabaseReference incomeRef, totalIncomeRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

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
                R.array.category_array,
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

        // Initialize Firebase Database references
        incomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("income");
        totalIncomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("totalIncome");

        // Set click listener on save button
        saveButton.setOnClickListener(v -> saveIncome());

        return view;
    }

    private void saveIncome() {
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String time = timeText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String paymentMode = paymentModeSpinner.getSelectedItem().toString();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Create an IncomeEntry object
            IncomeEntry incomeEntry = new IncomeEntry(amount, date, time, category, paymentMode);

            // Add new income entry
            String key = incomeRef.push().getKey();
            if (key != null) {
                incomeRef.child(key).setValue(incomeEntry)
                        .addOnSuccessListener(aVoid -> {
                            // Update total income after successfully saving new entry
                            updateTotalIncome(amount);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save income entry.", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalIncome(double newIncome) {
        totalIncomeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentTotal = 0.0;

                if (dataSnapshot.exists()) {
                    Double totalIncome = dataSnapshot.getValue(Double.class);
                    if (totalIncome != null) {
                        currentTotal = totalIncome;
                    }
                }

                double updatedTotalIncome = currentTotal + newIncome;

                totalIncomeRef.setValue(updatedTotalIncome)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Income saved successfully!", Toast.LENGTH_SHORT).show();
                            amountEditText.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update total income.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load current total income.", Toast.LENGTH_SHORT).show();
            }
        });
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

    // Inner class for IncomeEntry
    public static class IncomeEntry {
        private double amount;
        private String date;
        private String time;
        private String category;
        private String paymentMode;

        public IncomeEntry() {
        }

        public IncomeEntry(double amount, String date, String time, String category, String paymentMode) {
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
