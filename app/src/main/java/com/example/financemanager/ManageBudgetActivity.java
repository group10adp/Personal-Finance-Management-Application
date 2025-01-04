package com.example.financemanager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageBudgetActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference realtimeDatabase;
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private List<Budget> budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_budget);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        realtimeDatabase = FirebaseDatabase.getInstance().getReference("budget");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        budgets = new ArrayList<>();
        adapter = new BudgetAdapter(this, budgets);
        recyclerView.setAdapter(adapter);

        // Fetch budget data
        fetchBudgetData();
    }

    private void fetchBudgetData() {
        realtimeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                budgets.clear(); // Clear list to avoid duplicates
                for (DataSnapshot monthSnapshot : snapshot.getChildren()) {
                    String monthYear = monthSnapshot.getKey(); // Retrieve month name
                    if (monthYear != null) {
                        budgets.add(new Budget(monthYear)); // Format and add data
                    }
                }
                adapter.notifyDataSetChanged(); // Update RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RealtimeDB", "Error fetching budget data", error.toException());
                Toast.makeText(ManageBudgetActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
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
