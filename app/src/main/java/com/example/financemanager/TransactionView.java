package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class TransactionView extends AppCompatActivity {

    String docId,from;

    private EditText amountEditText,note1;;
    private Button saveButton;
    private TextView dateText;
    private TextView timeText;
    private TextView categorySpinner, paymentModeSpinner;
    private FirebaseFirestore firestore;

    FirebaseAuth auth;

    private String userId;
    ExpenseFragment.CustomLoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_view);

        Intent intent = getIntent();
        docId = intent.getStringExtra("docId");
        from =  intent.getStringExtra("from");

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize views
        note1=findViewById(R.id.notes);
        amountEditText = findViewById(R.id.amountEditText);
        saveButton = findViewById(R.id.saveButton);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        categorySpinner = findViewById(R.id.categorySpinner);
        paymentModeSpinner = findViewById(R.id.paymentModeSpinner);
        ImageView dateIcon = findViewById(R.id.dateIcon);
        ImageView timeIcon = findViewById(R.id.timeIcon);

        amountEditText.setFocusable(false); // Disable focus
        amountEditText.setFocusableInTouchMode(false); // Disable focus in touch mode
        amountEditText.setClickable(false);

        note1.setFocusable(false); // Disable focus
        note1.setFocusableInTouchMode(false); // Disable focus in touch mode
        note1.setClickable(false);

        // Set current date and time
        dateText.setText(getCurrentDate());
        timeText.setText(getCurrentTime());

        dateIcon.setOnClickListener(v -> showDatePicker());
        dateText.setOnClickListener(v -> showDatePicker());
        timeIcon.setOnClickListener(v -> showTimePicker());
        timeText.setOnClickListener(v -> showTimePicker());

        loadingDialog = new ExpenseFragment.CustomLoadingDialog(this);

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);


        firestore = FirebaseFirestore.getInstance();


        firestore.collection("users")
                .document(userId) // Kq2rEqmQn8a9OYFluJc52HrxdRB2
                .collection(from)
                .document(year) // Year
                .collection(month) // Month
                .document(docId) // JOFRHly06EUDmTx7NN3d
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //Log.d("DocumentSnapshot", documentSnapshot.getData().toString());

                        TransactionModel expenseEntry = documentSnapshot.toObject(TransactionModel.class);
                        if (expenseEntry != null) {
                            // Use the retrieved data
                            double  amount = expenseEntry.getAmount();
                            amountEditText.setText(String.valueOf(amount));


                            String category = expenseEntry.getCategory();
                            if (category == null) {
                                category = "Unknown"; // Default category
                            }
                            categorySpinner.setText(category);

                            String date = expenseEntry.getDate();
                            if (date == null) {
                                date = "01 Jan 1970"; // Default date for invalid entries
                            }
                            dateText.setText(date);

                            String note = expenseEntry.getNote();
                            if (note == null) {
                                note = ""; // Default category
                            }

                            note1.setText(note);

                            String time = expenseEntry.getTime();
                            if (time == null) {
                                time = "12:00 AM"; // Default time for invalid entries
                            }
                            timeText.setText(time);

                            String paymentMode = expenseEntry.getPaymentMode();
                            paymentModeSpinner.setText(paymentMode);



                        }
                        Log.d("DataRetrieved", "Amount: ");

                    } else {
                        Log.e("FirestoreError", "No such document found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error retrieving document: ", e);
                });



        // Set click listener on save button
        saveButton.setOnClickListener(v -> {

//            saveExpense(); // Call the saveIncome method
//            loadingDialog.show();
////
////            // Simulate saving operation or dismiss dialog after completion
//            new Handler().postDelayed(() -> loadingDialog.dismiss(), 2000); // Dismiss after 2 seconds (example)

            finish();
        });



    }

    private void saveExpense() {
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String time = timeText.getText().toString().trim();
        String note=note1.getText().toString().trim();
        String category = categorySpinner.getText().toString();
        String paymentMode = paymentModeSpinner.getText().toString();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            // Get current year and month
            String[] dateParts = date.split(" ");
            String year = dateParts[2];
            String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1); // Get the current month in MM format

            // Create an ExpenseEntry object
            ExpenseFragment.ExpenseEntry expenseEntry = new ExpenseFragment.ExpenseEntry(amount, date, time, category, paymentMode,note);

            String type="Expense";
            IncomeFragment.TransactionEntry transactionEntry = new IncomeFragment.TransactionEntry(amount, date, time, category, paymentMode,note,type);

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
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save expense entry.", Toast.LENGTH_SHORT).show());

            firestore.collection("users").document(userId)
                    .collection("transaction")
                    .document(year)
                    .collection(month)
                    .add(transactionEntry)
                    .addOnSuccessListener(documentReference -> {
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save income entry.", Toast.LENGTH_SHORT).show());



        } else {
            Toast.makeText(this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                        amountEditText.setText("");
                        note1.setText("");
                        loadingDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update monthly total expense.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load current monthly expense.", Toast.LENGTH_SHORT).show());
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
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update yearly expense.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load current yearly expense.", Toast.LENGTH_SHORT).show());
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
                this,
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
                this,
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