package com.example.financemanager;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<Budget> budgets;
    private final Context context;

    public BudgetAdapter(Context context, List<Budget> budgets) {
        this.context = context;
        this.budgets = budgets;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.budget_card, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.tvMonthYear.setText(formatMonthOrYear(budget.getMonthYear()));

        holder.itemView.setOnClickListener(v -> {


            Intent intent = new Intent(context, BudgetDisplayActivity.class);
            intent.putExtra("selectedDateValue", budget.getMonthYear());
            context.startActivity(intent);

        });

        holder.itemView.setOnLongClickListener(v -> {
            // Show a confirmation dialog or a toast for delete option
            // Show a toast with the document ID
            showDeleteDialog(holder.itemView.getContext(), budget, position);
            return true;  // Return true to indicate the long click was handled
        });
    }


    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthYear, tvBudget, tvTotalSpent, tvAvailableBudget;
        DonutProgress donutProgress;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonthYear = itemView.findViewById(R.id.tv_month_year);
            tvBudget = itemView.findViewById(R.id.tv_budget);
            tvTotalSpent = itemView.findViewById(R.id.tv_total_spent);
            tvAvailableBudget = itemView.findViewById(R.id.tv_available_budget);
            donutProgress = itemView.findViewById(R.id.donutProgress);
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

    private void showDeleteDialog(Context context, Budget budget, int position) {
        // Create an AlertDialog to confirm deletion
        new AlertDialog.Builder(context)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Perform delete action
                    deleteIncome(budget, position, context);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing, just dismiss the dialog
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void deleteIncome(Budget budget, int position, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get the current user ID

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String month = budget.getMonthYear();

        // Reference to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        if (userId != null && month != null) {
            // Navigate to the user's data and the specific key
            databaseReference.child(userId).child(month).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Successfully deleted
                        Toast.makeText(context, "Budget deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to delete
                        Toast.makeText(context, "Failed to delete the budget", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "UserId or Key is null", Toast.LENGTH_SHORT).show();
        }


    }
}
