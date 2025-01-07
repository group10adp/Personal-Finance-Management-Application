package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class IncomeFragment extends Fragment {

    private EditText amountEditText,note1;
    private Button saveButton;
    private TextView dateText;
    private TextView timeText;
    private Spinner categorySpinner, paymentModeSpinner;
    private FirebaseFirestore firestore;

    FirebaseAuth auth;

    private String userId; // Placeholder, use actual user ID from authentication

    CustomLoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize views
        note1=view.findViewById(R.id.notes);
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

        loadingDialog = new CustomLoadingDialog(getContext());




        // Set up ArrayAdapter for categorySpinner
        String[] categoryArray = {
                "Others", "Salary", "Sold Items", "Coupons"
        };

// Step 2: Create an ArrayAdapter using the category array
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryArray
        );

// Step 3: Set the layout for the dropdown items
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Step 4: Attach the adapter to the Spinner
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
        saveButton.setOnClickListener(v -> {

            saveIncome(); // Call the saveIncome method
            loadingDialog.show();

            // Simulate saving operation or dismiss dialog after completion
            new Handler().postDelayed(() -> loadingDialog.dismiss(), 2500); // Dismiss after 2 seconds (example)
        });

        return view;
    }

    private void saveIncome() {
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String time = timeText.getText().toString().trim();
        String note=note1.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String paymentMode = paymentModeSpinner.getSelectedItem().toString();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Get current year and month
            String[] dateParts = date.split(" ");
            String year = dateParts[2];
            String month = getMonthNumber(dateParts[1]); // Get the current month in MM format

            // Create an IncomeEntry object
            IncomeEntry incomeEntry = new IncomeEntry(amount, date, time, category, paymentMode,note);

            String type="Income";
            TransactionEntry transactionEntry = new TransactionEntry(amount, date, time, category, paymentMode,note,type);

            // Save income entry to Firestore
            firestore.collection("users").document(userId)
                    .collection("income")
                    .document(year)
                    .collection(month)
                    .add(incomeEntry)
                    .addOnSuccessListener(documentReference -> {
                        // Update total income for the user
                        updateTotalIncome(year, month, amount);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save income entry.", Toast.LENGTH_SHORT).show());


            firestore.collection("users").document(userId)
                    .collection("transaction")
                    .document(year)
                    .collection(month)
                    .add(transactionEntry)
                    .addOnSuccessListener(documentReference -> {
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save income entry.", Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalIncome(String year, String month, double newIncome) {
        DocumentReference totalIncomeDoc = firestore.collection("users").document(userId)
                .collection("income").document(year).collection(month).document("totalIncome");

        // Get the current total income for the month
        totalIncomeDoc.get().addOnSuccessListener(documentSnapshot -> {
            double currentTotal = documentSnapshot.exists() ? documentSnapshot.getDouble("total") : 0.0;
            double updatedTotal = currentTotal + newIncome;

            // Update the total income for that month
            totalIncomeDoc.set(Map.of("total", updatedTotal))
                    .addOnSuccessListener(aVoid -> {
                        // Update yearly total income
                        updateYearlyIncome(year, newIncome);
                        Toast.makeText(getContext(), "Income saved successfully!", Toast.LENGTH_SHORT).show();
                        amountEditText.setText("");
                        note1.setText("");
                        loadingDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update monthly total income.", Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load current monthly income.", Toast.LENGTH_SHORT).show());
    }

    private void updateYearlyIncome(String year, double newIncome) {
        DocumentReference yearlyIncomeDoc = firestore.collection("users").document(userId)
                .collection("income").document("totalYearlyIncome"); // No need to use the year here

        // Check if the totalYearlyIncome document exists
        yearlyIncomeDoc.get().addOnSuccessListener(documentSnapshot -> {
            double currentYearlyTotal = documentSnapshot.exists() ? documentSnapshot.getDouble("total") : 0.0;
            double updatedYearlyTotal = currentYearlyTotal + newIncome;

            // Set or update the total yearly income in the document
            yearlyIncomeDoc.set(Map.of("total", updatedYearlyTotal))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update yearly income.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load current yearly income.", Toast.LENGTH_SHORT).show());
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

    private String getMonthNumber(String monthName) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.ENGLISH); // "MMM" is for short month names
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(monthName)); // Parse month name
            return String.valueOf(cal.get(Calendar.MONTH) + 1); // Calendar months are 0-indexed, so add 1
        } catch (Exception e) {
            e.printStackTrace();
            return "1"; // Return -1 in case of an error
        }
    }

    // Inner class for IncomeEntry
    public static class IncomeEntry {
        private double amount;
        private String date;
        private String time;
        private String category;
        private String paymentMode;
        private String note;

        public IncomeEntry() {
        }

        public IncomeEntry(double amount, String date, String time, String category, String paymentMode,String note) {
            this.amount = amount;
            this.date = date;
            this.time = time;
            this.category = category;
            this.paymentMode = paymentMode;
            this.note=note;
        }

        public double getAmount() {
            return amount;
        }

        public String getDate() {
            return date;
        }

        public String getNote() {
            return note;
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

    public static class TransactionEntry {
        private double amount;
        private String date;
        private String time;
        private String category;
        private String paymentMode;
        private String note;
        private String type;

        public TransactionEntry() {
        }

        public TransactionEntry(double amount, String date, String time, String category, String paymentMode,String note,String type) {
            this.amount = amount;
            this.date = date;
            this.time = time;
            this.category = category;
            this.paymentMode = paymentMode;
            this.note=note;
            this.type=type;
        }

        public double getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }

        public String getDate() {
            return date;
        }

        public String getNote() {
            return note;
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

    public class CustomLoadingDialog {
        private final AlertDialog dialog;

        public CustomLoadingDialog(Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

            View view = inflater.inflate(R.layout.custom_loading_dialog, null);
            builder.setView(view);

            // Disable canceling the dialog by clicking outside
            builder.setCancelable(false);

            dialog = builder.create();
        }

        public void show() {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }

        public void dismiss() {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

}
