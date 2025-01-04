package com.example.financemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class BudgetDetailsActivity extends AppCompatActivity {
    TextView totalBudgetTextView, remainingBudgetTextView;
    EditText categoryInputOthers, categoryInputTransport, categoryInputShopping,
            categoryInputEntertainment, categoryInputHealth, categoryInputEducation,
            categoryInputBill, categoryInputInvestment, categoryInputRent, categoryInputTax,
            categoryInputInsurance, categoryInputMoneyTransfer;
    Button saveButton;
    double totalBudget, remainingBudget;
    FirebaseFirestore firestore;
    String userId, year, month;
    FirebaseAuth auth;
    private DocumentReference totalIncomeRef, totalExpenseRef;
    private double totalIncome = 0.0, totalExpense = 0.0;

    DatabaseReference realtimeDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_details);

        realtimeDatabase = FirebaseDatabase.getInstance().getReference();

        // Get the total budget passed from the previous activity
        Intent intent = getIntent();
        totalBudget = intent.getDoubleExtra("totalBudget", 0);
        month = intent.getStringExtra("selectedDateValue");
        remainingBudget = totalBudget;

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize the TextViews and Firestore
        totalBudgetTextView = findViewById(R.id.total_budget_value);
        remainingBudgetTextView = findViewById(R.id.remaining_budget_value);
        saveButton = findViewById(R.id.save_button);
        firestore = FirebaseFirestore.getInstance();

        year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        //month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);


        totalBudgetTextView.setText("₹" + totalBudget);
        remainingBudgetTextView.setText("₹" + remainingBudget);

        // Initialize EditTexts
        categoryInputOthers = findViewById(R.id.category_input_others);
        categoryInputTransport = findViewById(R.id.category_input_transport);
        categoryInputShopping = findViewById(R.id.category_input_shopping);
        categoryInputEntertainment = findViewById(R.id.category_input_entertainment);
        categoryInputHealth = findViewById(R.id.category_input_health);
        categoryInputEducation = findViewById(R.id.category_input_education);
        categoryInputBill = findViewById(R.id.category_input_bill);
        categoryInputInvestment = findViewById(R.id.category_input_investment);
        categoryInputRent = findViewById(R.id.category_input_rent);
        categoryInputTax = findViewById(R.id.category_input_tax);
        categoryInputInsurance = findViewById(R.id.category_input_insurance);
        categoryInputMoneyTransfer = findViewById(R.id.category_input_money_transfer);

        // Add TextWatchers to update remaining budget
        addTextWatcher(categoryInputOthers);
        addTextWatcher(categoryInputTransport);
        addTextWatcher(categoryInputShopping);
        addTextWatcher(categoryInputEntertainment);
        addTextWatcher(categoryInputHealth);
        addTextWatcher(categoryInputEducation);
        addTextWatcher(categoryInputBill);
        addTextWatcher(categoryInputInvestment);
        addTextWatcher(categoryInputRent);
        addTextWatcher(categoryInputTax);
        addTextWatcher(categoryInputInsurance);
        addTextWatcher(categoryInputMoneyTransfer);

        // Set up the save button
        saveButton.setOnClickListener(view -> saveBudgetData());
    }

    // Method to add a TextWatcher to each EditText
    private void addTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {
                // Nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                updateRemainingBudget();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Nothing to do here
            }
        });
    }

    // Method to update the remaining budget based on entered values
    private void updateRemainingBudget() {
        double spentAmount = 0;

        // Sum all the values from the EditTexts
        spentAmount += getCategoryAmount(categoryInputOthers);
        spentAmount += getCategoryAmount(categoryInputTransport);
        spentAmount += getCategoryAmount(categoryInputShopping);
        spentAmount += getCategoryAmount(categoryInputEntertainment);
        spentAmount += getCategoryAmount(categoryInputHealth);
        spentAmount += getCategoryAmount(categoryInputEducation);
        spentAmount += getCategoryAmount(categoryInputBill);
        spentAmount += getCategoryAmount(categoryInputInvestment);
        spentAmount += getCategoryAmount(categoryInputRent);
        spentAmount += getCategoryAmount(categoryInputTax);
        spentAmount += getCategoryAmount(categoryInputInsurance);
        spentAmount += getCategoryAmount(categoryInputMoneyTransfer);

        // Update the remaining budget
        remainingBudget = totalBudget - spentAmount;
        remainingBudgetTextView.setText("₹" + remainingBudget);
    }

    // Helper method to get the value from each EditText, considering empty fields as 0
    private double getCategoryAmount(EditText editText) {
        String text = editText.getText().toString();
        return text.isEmpty() ? 0 : Double.parseDouble(text);
    }

    // Method to save budget data to Firestore
    private void saveBudgetData() {
        double totalamount = totalBudget;

        totalExpenseRef = firestore.collection("users").document(userId)
                .collection("expense")
                .document(String.valueOf(year))
                .collection(month)
                .document("totalExpense");

        totalExpenseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();

                totalExpense = documentSnapshot != null && documentSnapshot.contains("total")
                        ? documentSnapshot.getDouble("total")
                        : 0.0;

                    firestore.collection("users")
                            .document(userId)
                            .collection("budget")
                            .document(year)
                            .collection(month)
                            .document("total-budget") // Document ID for total budget
                            .set(new TotalBudgetEntry(totalamount))  // TotalBudgetEntry is a custom class to store the total amount
                            .addOnSuccessListener(aVoid -> {
                                //Toast.makeText(BudgetDetailsActivity.this, "Total budget saved successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(BudgetDetailsActivity.this, "Failed to save total budget.", Toast.LENGTH_SHORT).show();
                            });


                    firestore.collection("users")
                            .document(userId)
                            .collection("budget")
                            .document(year)
                            .collection(month)
                            .document("total-remaining-budget") // Document ID for total budget
                            .set(new TotalBudgetEntry(totalamount-totalExpense))  // TotalBudgetEntry is a custom class to store the total amount
                            .addOnSuccessListener(aVoid -> {
                                //Toast.makeText(BudgetDetailsActivity.this, "Total budget saved successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(BudgetDetailsActivity.this, "Failed to save total budget.", Toast.LENGTH_SHORT).show();
                            });

                    realtimeDatabase.child("budget").child(month)
                            .setValue(month)
                            .addOnSuccessListener(aVoid -> Log.d("RealtimeDB", "Month value saved successfully."))
                            .addOnFailureListener(e -> Log.e("RealtimeDB", "Failed to save month value.", e));

            } else {

            }
        });




        // Create a BudgetEntry object for each category
        saveCategoryData(categoryInputOthers, "Others");
        saveCategoryData(categoryInputTransport, "Transport");
        saveCategoryData(categoryInputShopping, "Shopping");
        saveCategoryData(categoryInputEntertainment, "Entertainment");
        saveCategoryData(categoryInputHealth, "Health");
        saveCategoryData(categoryInputEducation, "Education");
        saveCategoryData(categoryInputBill, "Bill");
        saveCategoryData(categoryInputInvestment, "Investment");
        saveCategoryData(categoryInputRent, "Rent");
        saveCategoryData(categoryInputTax, "Tax");
        saveCategoryData(categoryInputInsurance, "Insurance");
        saveCategoryData(categoryInputMoneyTransfer, "Money Transfer");

        Toast.makeText(BudgetDetailsActivity.this, "Budget details saved successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(BudgetDetailsActivity.this, BudgetDisplayActivity.class);
        intent.putExtra("selectedDateValue", month);
        startActivity(intent);
        finish();
    }

    // Helper method to save data for each category
    private void saveCategoryData(EditText editText, String category) {
        double amount = getCategoryAmount(editText);
        if (amount > 0) {
            // Save category-based budget
            firestore.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document(year)
                    .collection(month)
                    .document("category-based-budget")  // Document for all category-based budgets
                    .collection(category)  // Category name as subcollection
                    .document("budget-entry") // Use a static document ID or dynamically generate one
                    .set(new BudgetEntry(amount))  // Save the budget entry (ensure that BudgetEntry is properly defined)
                    .addOnSuccessListener(aVoid -> {
                        //Toast.makeText(BudgetDetailsActivity.this, "Budget saved successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(BudgetDetailsActivity.this, "Failed to save budget entry.", Toast.LENGTH_SHORT).show();
                    });

// Save category-based remaining budget
            firestore.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document(year)
                    .collection(month)
                    .document("category-based-remaining-budget")  // Document for all category-based remaining budgets
                    .collection(category)  // Category name as subcollection
                    .document("remaining-budget-entry") // Use a static document ID or dynamically generate one
                    .set(new RemainingBudgetEntry(amount))  // Save the remaining budget entry (ensure that RemainingBudgetEntry is properly defined)
                    .addOnSuccessListener(aVoid -> {
                        //Toast.makeText(BudgetDetailsActivity.this, "Remaining budget saved successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(BudgetDetailsActivity.this, "Failed to save remaining budget entry.", Toast.LENGTH_SHORT).show();
                    });



        }
    }

    private String formatMonthOrYear(String monthYear) {
        try {
            int month = Integer.parseInt(monthYear);
            if (month >= 1 && month <= 12) {
                return getMonthNameFromNumber(month); // Convert to month name
            } else {
                return "Year: " + monthYear; // Format as year
            }
        } catch (NumberFormatException e) {
            return "Invalid: " + monthYear; // Handle unexpected non-numeric keys
        }
    }

    private String getMonthNameFromNumber(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return "Month: "+months[month - 1];
    }
}

