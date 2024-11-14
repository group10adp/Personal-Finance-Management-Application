package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private DatabaseReference incomeRef, totalIncomeRef;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        // Initialize views
        amountEditText = view.findViewById(R.id.amountEditText);
        saveButton = view.findViewById(R.id.saveButton);
        dateText = view.findViewById(R.id.dateText);  // Initialize date TextView
        timeText = view.findViewById(R.id.timeText);  // Initialize time TextView

        // Set current date and time
        dateText.setText(getCurrentDate());
        timeText.setText(getCurrentTime());
        ImageView dateIcon = view.findViewById(R.id.dateIcon);
        ImageView timeIcon = view.findViewById(R.id.timeIcon);

        dateIcon.setOnClickListener(v -> showDatePicker());
        dateText.setOnClickListener(v -> showDatePicker());
        timeIcon.setOnClickListener(v -> showTimePicker());
        timeText.setOnClickListener(v -> showTimePicker());

        // Initialize Firebase Database references
        incomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("income");
        totalIncomeRef = FirebaseDatabase.getInstance().getReference("user").child("user1").child("totalIncome");

        // Set click listener on save button
        saveButton.setOnClickListener(v -> saveIncome());

        return view;
    }

    private void saveIncome() {
        String amountStr = amountEditText.getText().toString().trim();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Add new income entry
            String key = incomeRef.push().getKey();
            if (key != null) {
                incomeRef.child(key).setValue(amount)
                        .addOnSuccessListener(aVoid -> {
                            // Update total income after successfully saving new entry
                            updateTotalIncome(amount);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save amount.", Toast.LENGTH_SHORT).show());
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

                // If totalIncome exists, retrieve its value
                if (dataSnapshot.exists()) {
                    Double totalIncome = dataSnapshot.getValue(Double.class);
                    if (totalIncome != null) {
                        currentTotal = totalIncome;
                    }
                }

                // Calculate new total income
                double updatedTotalIncome = currentTotal + newIncome;

                // Update total income in Firebase
                totalIncomeRef.setValue(updatedTotalIncome)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Amount saved successfully!", Toast.LENGTH_SHORT).show();
                            amountEditText.setText(""); // Clear input after saving
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
        // Get current date
        Calendar calendar = Calendar.getInstance();

        // Define date format (e.g., "13 Nov 2024")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Return the formatted date
        return dateFormat.format(calendar.getTime());
    }

    private String getCurrentTime() {
        // Get current time
        Calendar calendar = Calendar.getInstance();

        // Define time format (e.g., "12:45 PM")
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Return the formatted time
        return timeFormat.format(calendar.getTime());
    }
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // Gets current hour (24-hour format)
        int minute = calendar.get(Calendar.MINUTE);    // Gets current minute

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    // Update the TextView with the selected time
                    String amPm = selectedHour >= 12 ? "PM" : "AM";
                    int hourIn12Format = selectedHour % 12 == 0 ? 12 : selectedHour % 12;
                    timeText.setText(String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm));
                },
                hour, minute, false);  // 'false' for 24-hour format, 'true' for 12-hour format

        timePickerDialog.show();
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);      // Gets the current year
        int month = calendar.get(Calendar.MONTH);    // Gets the current month (0-based, so January is 0)
        int day = calendar.get(Calendar.DAY_OF_MONTH); // Gets the current day

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date (e.g., "13 Nov 2024")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    dateText.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day);

        datePickerDialog.show();
    }

}
