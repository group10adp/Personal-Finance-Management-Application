package com.example.financemanager;

import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AiBudgetActivity extends AppCompatActivity {

    String userId, year, month;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    TextView text_content;
    Button apply;

    FirebaseFirestore firestore;

    LinearLayout textContainer;
    DatabaseReference realtimeDatabase;

    private DocumentReference totalIncomeRef, totalExpenseRef;
    private double totalIncome = 0.0, totalExpense = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_budget);

        Intent intent = getIntent();
        boolean showLoading = intent.getBooleanExtra("showLoading", false);

        progressDialog = null;
        if (showLoading) {
            // Show a loading dialog
            progressDialog = new ProgressDialog(AiBudgetActivity.this);
            progressDialog.setMessage("Fetching data...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        apply=findViewById(R.id.apply_button);
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();

        //text_content=findViewById(R.id.text_content);
        textContainer = findViewById(R.id.text_container);
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());
        firestore = FirebaseFirestore.getInstance();

        year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);

        fetchUserData(userId, "2024");

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reference to the LinearLayout container
                LinearLayout textContainer = findViewById(R.id.text_container);

                // Variables to store data
                double totalBudget = 0.0;
                Map<String, Double> categoryValues = new LinkedHashMap<>(); // Store category values

                // Iterate through all child views
                for (int i = 0; i < textContainer.getChildCount(); i++) {
                    View child = textContainer.getChildAt(i);

                    // Check if the child is a TextView
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child;

                        // Get the text from the TextView
                        String text = textView.getText().toString();

                        // Split the text into key and value
                        if (text.contains(":")) {
                            String[] parts = text.split(":");
                            String key = parts[0].trim(); // Category name
                            String value = parts[1].trim(); // Value

                            try {
                                double numericValue = Double.parseDouble(value);

                                // Handle "Total Budget" separately
                                if (key.equals("Total Budget")) {
                                    totalBudget = numericValue; // Store total budget
                                } else {
                                    // Store other categories in the map
                                    categoryValues.put(key, numericValue);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(AiBudgetActivity.this, "Invalid value for " + key, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                // Use the values
                Log.d("Budget Data", "Total Budget: " + totalBudget);

                saveBudgetData(totalBudget);
                for (Map.Entry<String, Double> entry : categoryValues.entrySet()) {
                    String categoryName = entry.getKey();
                    double value = entry.getValue();

                    // Handle the special case for "Other"
                    if (categoryName.equals("Other")) {
                        categoryName = "Others";
                    }

                    saveCategoryData(categoryName, value);


                    // Use the category name and value as needed
                    Log.d("Processed Category", "Category: " + categoryName + ", Value: " + value);
                }

                navigateToBudgetDisplay();

            }
        });


    }

    private void saveBudgetData(double totalBudget) {
        double totalamount = totalBudget;
        totalExpenseRef = firestore.collection("users").document(userId)
                .collection("expense")
                .document(year)
                .collection(month)
                .document("totalExpense");

        totalExpenseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot documentSnapshot = task.getResult();
                totalExpense = documentSnapshot.contains("total")
                        ? documentSnapshot.getDouble("total")
                        : 0.0;

                double remainingBudget = totalBudget - totalExpense;

                saveTotalBudget(totalBudget);
                saveRemainingBudget(remainingBudget);

                realtimeDatabase.child(userId).child(month)
                        .setValue(month)
                        .addOnSuccessListener(aVoid -> Log.d("RealtimeDB", "Month value saved successfully."))
                        .addOnFailureListener(e -> Log.e("RealtimeDB", "Failed to save month value.", e));


            } else {
                Log.e("FirestoreError", "Failed to fetch totalExpense", task.getException());
                Toast.makeText(this, "Failed to retrieve expense data.", Toast.LENGTH_SHORT).show();
            }
        });

//        saveCategoryBudgets();
//        navigateToBudgetDisplay();
    }

    private void saveTotalBudget(double totalamount) {
        firestore.collection("users")
                .document(userId)
                .collection("budget")
                .document(year)
                .collection(month)
                .document("total-budget")
                .set(new TotalBudgetEntry(totalamount)) // Replace with your custom class
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Total budget saved successfully."))
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to save total budget", e);
                    Toast.makeText(this, "Failed to save total budget.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRemainingBudget(double remainingBudget) {
        firestore.collection("users")
                .document(userId)
                .collection("budget")
                .document(year)
                .collection(month)
                .document("total-remaining-budget")
                .set(new TotalBudgetEntry(remainingBudget)) // Replace with your custom class
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Remaining budget saved successfully."))
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to save remaining budget", e);
                    Toast.makeText(this, "Failed to save remaining budget.", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToBudgetDisplay() {
        Toast.makeText(this, "Budget details saved successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AiBudgetActivity.this, BudgetDisplayActivity.class);
        intent.putExtra("selectedDateValue", month);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }


private void saveCategoryData(String category,double amount) {
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
                    Toast.makeText(AiBudgetActivity.this, "Failed to save budget entry.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AiBudgetActivity.this, "Failed to save remaining budget entry.", Toast.LENGTH_SHORT).show();
                });



    }
}

    private void fetchUserData(String userId, String tempYear) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Increase connection timeout
                .readTimeout(60, TimeUnit.SECONDS)     // Increase read timeout
                .writeTimeout(60, TimeUnit.SECONDS)    // Increase write timeout
                .build();
        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://a939-103-51-148-13.ngrok-free.app/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of the ApiService
        UserDataService userDataService = retrofit.create(UserDataService.class);

        // Make the API call with the dynamic path
        Call<ResponseBody> call = userDataService.getUserData(userId, tempYear);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();

                        // Parse the response to extract dynamic categories
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONObject categories = jsonObject; // The whole response is the categories object

                        // Use a Map to store category data
                        Map<String, Double> categoryData = new LinkedHashMap<>(); // LinkedHashMap to maintain insertion order

                        // Add the "Total Budget" key at the top if it exists
                        if (categories.has("Total Budget")) {
                            double totalBudget = categories.getDouble("Total Budget");
                            totalBudget = Math.abs(totalBudget);
                            totalBudget = totalBudget - totalBudget * 0.05;
                            totalBudget = Math.round(totalBudget * 100.0) / 100.0;

                            categoryData.put("Total Budget", totalBudget); // Add to the map
                        }

                        // Loop through all other categories (keys) and add them to the map
                        Iterator<String> keys = categories.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (!key.equals("Total Budget")) { // Skip "Total Budget" since it's already added
                                double value = categories.getDouble(key);

                                value = Math.abs(value);
                                value = value - value * 0.05;
                                value = Math.round(value * 100.0) / 100.0;

                                categoryData.put(key, value); // Add to the map
                            }
                        }

                        // Reference to the LinearLayout container for dynamic TextViews
                        // Clear any existing views (to avoid duplicates on refresh)
                        textContainer.removeAllViews();

                        // Add TextViews dynamically for each entry in the map
                        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
                            String key = entry.getKey();
                            double value = entry.getValue();

                            // Create a new TextView
                            TextView textView = new TextView(AiBudgetActivity.this);
                            textView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            textView.setText(key + ": " + value+"\n");
                            textView.setTextSize(16f);
                            textView.setTextColor(getResources().getColor(android.R.color.black));

                            // Add the TextView to the container
                            textContainer.addView(textView);
                        }

                        progressDialog.dismiss();

                        // Log the map content for debugging
                        Log.d("API Response", categoryData.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AiBudgetActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }


                } else {
                    Toast.makeText(AiBudgetActivity.this, "API call unsuccessful", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AiBudgetActivity.this, "Failed to fetch data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

}