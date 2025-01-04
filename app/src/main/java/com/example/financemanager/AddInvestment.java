package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class AddInvestment extends Fragment {

    private Spinner spinnerMutualFund;
    private EditText editTextReturnRate, editTextAmount;
    private Button submitButton;

    private TextView dateText;
    private TextView timeText;

    private FirebaseFirestore firestore;

    FirebaseAuth auth;

    private String userId;

    public AddInvestment() {
        // Required empty public constructor
    }

    public static AddInvestment newInstance(String param1, String param2) {
        AddInvestment fragment = new AddInvestment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_investment, container, false);

        // Initialize views

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());
        spinnerMutualFund = view.findViewById(R.id.spinner_mutual_fund);
        editTextReturnRate = view.findViewById(R.id.edittext_return_rate);
        editTextAmount = view.findViewById(R.id.edittext_amount);
        submitButton = view.findViewById(R.id.submit_button);
        dateText = view.findViewById(R.id.dateText);
        timeText = view.findViewById(R.id.timeText);
        ImageView dateIcon = view.findViewById(R.id.dateIcon);
        ImageView timeIcon = view.findViewById(R.id.timeIcon);

        dateText.setText(getCurrentDate());
        timeText.setText(getCurrentTime());

        dateIcon.setOnClickListener(v -> showDatePicker());
        dateText.setOnClickListener(v -> showDatePicker());
        timeIcon.setOnClickListener(v -> showTimePicker());
        timeText.setOnClickListener(v -> showTimePicker());

        firestore = FirebaseFirestore.getInstance();

        // Populate Spinner with mutual funds
        String[] mutualFunds = {
                "SBI Mutual Fund",
                "HDFC Mutual Fund",
                "ICICI Prudential Mutual Fund",
                "Aditya Birla Sun Life Mutual Fund",
                "Axis Mutual Fund"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mutualFunds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMutualFund.setAdapter(adapter);

        // TextWatcher to enable Save button only if all fields are filled
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextReturnRate.addTextChangedListener(textWatcher);
        editTextAmount.addTextChangedListener(textWatcher);

        // Handle Save button click
        submitButton.setOnClickListener(v -> {
            String mutualFund = spinnerMutualFund.getSelectedItem().toString();
            String returnRate = editTextReturnRate.getText().toString();
            String amountStr = editTextAmount.getText().toString();
            String date = dateText.getText().toString().trim();
            String time = timeText.getText().toString().trim();

            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);

                // Get current year and month
                //String[] dateParts = date.split(" ");
                //String year = dateParts[2];
                String[] dateParts = date.split(" ");
                String year = dateParts[2];
                String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1); // Get the current month in MM format
                //String month = String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1); // Get the current month in MM format

                // Create an ExpenseEntry object
                AddInvestment.InvestmentEntry expenseEntry = new AddInvestment.InvestmentEntry(mutualFund,returnRate,amount, date, time);


                // Save expense entry to Firestore
                firestore.collection("users").document(userId)
                        .collection("investment")
                        .document(year)
                        .collection(month)
                        .add(expenseEntry)
                        .addOnSuccessListener(documentReference -> {
                            // Update total expense for the user
                            //updateTotalExpense(year, month, amount);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save expense entry.", Toast.LENGTH_SHORT).show());

                ExpenseFragment.ExpenseEntry expenseEntry1 = new ExpenseFragment.ExpenseEntry(amount, date, time, "Investment", "Online","Invested on mutual funds");

                // Save expense entry to Firestore
                firestore.collection("users").document(userId)
                        .collection("expense")
                        .document(year)
                        .collection(month)
                        .add(expenseEntry1)
                        .addOnSuccessListener(documentReference -> {
                            // Update total expense for the user
                            updateTotalExpense(year, month, amount);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save expense entry.", Toast.LENGTH_SHORT).show());

            } else {
                Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
            }

            // Pass data back to InvestmentsFragment
            Bundle bundle = new Bundle();
            bundle.putString("mutualFund", mutualFund);
            bundle.putString("returnRate", returnRate);
            bundle.putString("amount", amountStr);

            InvestmentsFragment investmentsFragment = new InvestmentsFragment();
            investmentsFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, investmentsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    // Enable Save button only when all fields have valid input
    private void validateInput() {
        String returnRate = editTextReturnRate.getText().toString().trim();
        String amount = editTextAmount.getText().toString().trim();
        submitButton.setEnabled(!returnRate.isEmpty() && !amount.isEmpty());
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

    public static class InvestmentEntry {
        private String mutualFund;
        private String date;
        private String time;
        private String returnRate;
        private double amount;

        public InvestmentEntry() {
        }

        public InvestmentEntry(String mutualFund,String returnRate,double amount, String date, String time) {
            this.mutualFund=mutualFund;
            this.returnRate=returnRate;
            this.amount = amount;
            this.date = date;
            this.time = time;

        }

        public String getMutualFund() {
            return mutualFund;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getReturnRate() {
            return returnRate;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static class ExpenseEntry {
        private double amount;
        private String date;
        private String time;
        private String category;
        private String paymentMode;
        private String note;

        public ExpenseEntry() {
        }

        public ExpenseEntry(double amount, String date, String time, String category, String paymentMode,String note) {
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
}
