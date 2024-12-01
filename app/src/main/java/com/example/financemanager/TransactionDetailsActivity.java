package com.example.financemanager;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TransactionDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter incomeAdapter;
    private List<TransactionModel> incomeList;

    private FirebaseFirestore firestore;
    FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Initialize the data list
        incomeList = new ArrayList<>();

        incomeAdapter = new TransactionAdapter(incomeList);
        recyclerView.setAdapter(incomeAdapter);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the year and month (You can replace this with actual data)
        String year = "2024"; // Example: Current year
        String month = "11"; // Example: Current month

        // Fetch income data from Firestore
        fetchTransactionData(year, month);

    }

    private void fetchTransactionData(String year, String month) {
        firestore.collection("users")
                .document(userId)
                .collection("transaction")
                .document(year)
                .collection(month)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        incomeList.clear(); // Clear any existing data

                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // SimpleDateFormat to parse and compare dates and times
                            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Check if the document ID is "totalIncome" and skip it
                                if ("totalIncome".equals(document.getId())) {
                                    Log.d("IncomeAdapter", "Skipping entry with ID: totalIncome");
                                    continue; // Skip this entry
                                }

                                // Safely retrieve 'amount' as Double
                                Double amount = document.getDouble("amount");
                                if (amount == null) {
                                    amount = 0.0; // Assign default value if 'amount' is null
                                }

                                // Convert amount (Double) to String
                                String amountString = String.valueOf(amount);

                                // Get 'category', 'date', and 'time' safely
                                String category = document.getString("category");
                                if (category == null) {
                                    category = "Unknown"; // Default category
                                }

                                String date = document.getString("date");
                                if (date == null) {
                                    date = "01 Jan 1970"; // Default date for invalid entries
                                }

                                String time = document.getString("time");
                                if (time == null) {
                                    time = "12:00 AM"; // Default time for invalid entries
                                }

                                String type = document.getString("type");

                                // Create a new IncomeModel object and add it to the list
                                incomeList.add(new TransactionModel(amountString, category, date, time,type));
                            }

                            // Sort the list based on date and time in descending order
                            Collections.sort(incomeList, (o1, o2) -> {
                                try {
                                    String dateTime1 = o1.getDate() + " " + o1.getTime();
                                    String dateTime2 = o2.getDate() + " " + o2.getTime();

                                    Date date1 = formatter.parse(dateTime1);
                                    Date date2 = formatter.parse(dateTime2);

                                    return date2.compareTo(date1); // Descending order
                                } catch (ParseException e) {
                                    Log.e("IncomeAdapter", "Error parsing date/time", e);
                                    return 0; // Keep order if parsing fails
                                }
                            });

                            //Log.d("IncomeAdapter", "Sorted Income List: " + incomeList);

                            // Notify the adapter that the data has changed
                            incomeAdapter.notifyDataSetChanged();
                        }

                    } else {
                        // Handle any errors
                        System.out.println("Error getting documents: " + task.getException());
                    }
                });
    }
}