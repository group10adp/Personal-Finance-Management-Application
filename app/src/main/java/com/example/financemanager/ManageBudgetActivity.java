package com.example.financemanager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
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
    String userId;
    private ShimmerFrameLayout shimmerLayout;
    private View mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_budget);

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());
        realtimeDatabase = FirebaseDatabase.getInstance().getReference(userId);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        budgets = new ArrayList<>();
        adapter = new BudgetAdapter(this, budgets);
        recyclerView.setAdapter(adapter);

        shimmerLayout =findViewById(R.id.shimmerLayout);
        mainContent = findViewById(R.id.main);

        // Start shimmer effect
        shimmerLayout.startShimmer();

        // Simulate data loading (replace with real logic)
        new Handler().postDelayed(() -> {
            // Stop shimmer effect and show main content
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }, 3000); // Simulated delay of 3 seconds

        // Fetch budget data
        fetchBudgetData();
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
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

    @Override
    protected void onPause() {
        super.onPause();
        shimmerLayout.stopShimmer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shimmerLayout.startShimmer();
    }

}
