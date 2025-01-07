package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;

public class ExpenseFragment extends Fragment {

    private EditText amountEditText,note1;;
    private Button saveButton;
    private TextView dateText;
    private TextView timeText;
    private Spinner categorySpinner, paymentModeSpinner;
    private FirebaseFirestore firestore;
    private double totalIncome = 0.0, totalExpense = 0.0;
    private DocumentReference totalIncomeRef, totalExpenseRef, totalIncomeRef1, totalExpenseRef1;

    FirebaseAuth auth;

    private String userId;

    CustomLoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

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
                "Others", "Food", "Transport", "Shopping", "Entertainment",
                "Health", "Education", "Bills", "Investments", "Rent",
                "Taxes", "Insurance", "Money Transfer"
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

            saveExpense(); // Call the saveIncome method
            loadingDialog.show();

            // Simulate saving operation or dismiss dialog after completion
            new Handler().postDelayed(() -> loadingDialog.dismiss(), 2500); // Dismiss after 2 seconds (example)
        });

        return view;
    }

    private void saveExpense() {
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String time = timeText.getText().toString().trim();
        String note=note1.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String paymentMode = paymentModeSpinner.getSelectedItem().toString();

        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr);

            isSpendingValid(amount, isValid -> {
                if (isValid) {

                    isSpendingBudgetValid(amount, isValidBudget -> {
                        if (isValidBudget) {
                            String[] dateParts = date.split(" ");
                            String year = dateParts[2];
                            String month = getMonthNumber(dateParts[1]); // Get the current month in MM format
                            Log.d("month",month);

                            // Create an ExpenseEntry object
                            ExpenseEntry expenseEntry = new ExpenseEntry(amount, date, time, category, paymentMode, note);

                            String type = "Expense";
                            IncomeFragment.TransactionEntry transactionEntry = new IncomeFragment.TransactionEntry(amount, date, time, category, paymentMode, note, type);

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

                            firestore.collection("users").document(userId)
                                    .collection("transaction")
                                    .document(year)
                                    .collection(month)
                                    .add(transactionEntry)
                                    .addOnSuccessListener(documentReference -> {
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save income entry.", Toast.LENGTH_SHORT).show());


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

                                            firestore.collection("users")
                                                    .document(userId)
                                                    .collection("budget")
                                                    .document(year)
                                                    .collection(month)
                                                    .document("total-remaining-budget")
                                                    .set(new TotalBudgetEntry(remainingBudget-amount)) // Replace with your custom class
                                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Remaining budget saved successfully."))
                                                    .addOnFailureListener(e -> {
                                                        Log.e("FirestoreError", "Failed to save remaining budget", e);
                                                        Toast.makeText(getContext(), "Failed to save remaining budget.", Toast.LENGTH_SHORT).show();
                                                    });
                                            saveCategoryData(amount,category);

                                        } else {
                                            Toast.makeText(getContext(), "Remaining budget not found!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch remaining budget.", Toast.LENGTH_SHORT).show());


                        }
                        else{
                                Toast.makeText(getContext(), "Spending exceeds your available budget.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            else{
                Toast.makeText(getContext(), "Spending exceeds your available balance.", Toast.LENGTH_SHORT).show();
            }
            });

            // Get current year and month
        } else {
            Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
        }
    }
    private void isSpendingValid(double amount, OnValidationCompleteListener listener) {
        firestore = FirebaseFirestore.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

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

        // Fetch totalIncome and totalExpense in parallel
        totalIncomeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                totalIncome = task.getResult().getDouble("total");
            } else {
                totalIncome = 0.0;
            }

            totalExpenseRef.get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful() && task2.getResult() != null && task2.getResult().exists()) {
                    totalExpense = task2.getResult().getDouble("total");
                } else {
                    totalExpense = 0.0;
                }

                // Calculate balance and validate
                double balance = totalIncome - totalExpense;
                boolean isValid = amount <= balance;

                // Pass the result to the listener
                listener.onValidationComplete(isValid);
            });
        });
    }

    // Interface for handling validation result
    interface OnValidationCompleteListener {
        void onValidationComplete(boolean isValid);
    }

    private void isSpendingBudgetValid(double amount, OnValidationCompleteListener2 listener) {
        firestore = FirebaseFirestore.getInstance();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        firestore.collection("users")
                .document(userId)
                .collection("budget")
                .document(String.valueOf(currentYear))
                .collection(String.valueOf(currentMonth))
                .document("total-remaining-budget")
                .get()
                .addOnSuccessListener(remainingBudgetSnapshot -> {
                    if (remainingBudgetSnapshot.exists()) {
                        double remainingBudget = remainingBudgetSnapshot.getDouble("amount");

                        boolean isValidBudget = amount <= remainingBudget;

                        // Pass the result to the listener
                        listener.onValidationComplete(isValidBudget);

                    } else {
                        Toast.makeText(getContext(), "Remaining budget not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch remaining budget.", Toast.LENGTH_SHORT).show());
                // Calculate balance and validate

    }

    interface OnValidationCompleteListener2 {
        void onValidationComplete(boolean isValidBudget);
    }

    // Interface for handling validation result
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
                        note1.setText("");
                        loadingDialog.dismiss();
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

    private void saveCategoryData(double amountField, String category1) {
        if (amountField > 0) {
            String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);


            DocumentReference budgetRef = firestore.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document(year)
                    .collection(month)
                    .document("category-based-remaining-budget")
                    .collection(category1)  // Use the category dynamically
                    .document("remaining-budget-entry");
            Log.d("categorieee",category1);

            budgetRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        //Log.d("checkCate0","gfhh");
                        // Retrieve the remaining budget for the category
                        Double amount1 = document.getDouble("amount");
                        //Log.d("checkCate0",""+amount1);
                        double res= amount1-amountField;
                        Log.d("checkCate0",""+res);
                        firestore.collection("users")
                                .document(userId)
                                .collection("budget")
                                .document(year)
                                .collection(month)
                                .document("category-based-remaining-budget")  // Document for all category-based remaining budgets
                                .collection(category1)  // Category name as subcollection
                                .document("remaining-budget-entry") // Use a static document ID or dynamically generate one
                                .set(new RemainingBudgetEntry(res))  // Save the remaining budget entry (ensure that RemainingBudgetEntry is properly defined)
                                .addOnSuccessListener(aVoid -> {
                                    //Toast.makeText(BudgetDetailsActivity.this, "Remaining budget saved successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to save remaining budget entry.", Toast.LENGTH_SHORT).show();
                                });
                        }
                } else {
                    Log.e("FirestoreError", "Error fetching data for category: " + category1, task.getException());
                }

            });

// Save category-based remaining budget



        }
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

    public static class CustomLoadingDialog {
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
