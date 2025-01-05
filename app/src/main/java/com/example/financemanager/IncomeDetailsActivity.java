package com.example.financemanager;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.time.format.DateTimeFormatter;


public class IncomeDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncomeAdapter incomeAdapter;
    private List<IncomeModel> incomeList;

    private FirebaseFirestore firestore;
    FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_details);

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Initialize the data list
        incomeList = new ArrayList<>();

        incomeAdapter = new IncomeAdapter(incomeList);
        recyclerView.setAdapter(incomeAdapter);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the year and month (You can replace this with actual data)
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);

        // Fetch income data from Firestore
        fetchIncomeData(year, month);

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void fetchIncomeData(String year, String month) {
        firestore.collection("users")
                .document(userId)
                .collection("income")
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
                                double amountString = amount;

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

                                String note = document.getString("note");
                                if (note == null) {
                                    note = ""; // Default category
                                }


                                String docId =document.getId();
                                String paymentMode = document.getString("paymentMode");

                                // Create a new IncomeModel object and add it to the list
                                incomeList.add(new IncomeModel(amountString, category, date, time,note,docId,paymentMode));
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
