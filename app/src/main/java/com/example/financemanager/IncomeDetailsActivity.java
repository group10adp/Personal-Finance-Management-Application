package com.example.financemanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class IncomeDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncomeAdapter incomeAdapter;
    private List<IncomeModel> incomeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_details);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Initialize the data list
        incomeList = new ArrayList<>();

        // Add dummy data to the list (you can replace this with actual Firebase data later)
        incomeList.add(new IncomeModel("5000", "Salary", "28 Nov 2024", "12:30 PM"));
        incomeList.add(new IncomeModel("1500", "Freelance", "27 Nov 2024", "10:00 AM"));
        incomeList.add(new IncomeModel("2000", "Interest", "25 Nov 2024", "03:15 PM"));

        // Set up the adapter
        incomeAdapter = new IncomeAdapter(incomeList);
        recyclerView.setAdapter(incomeAdapter);
    }
}
