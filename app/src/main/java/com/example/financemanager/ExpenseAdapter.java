package com.example.financemanager;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.IncomeViewHolder> {

    private List<IncomeModel> incomeList;

    public ExpenseAdapter(List<IncomeModel> incomeList) {
        this.incomeList = incomeList;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_card, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        IncomeModel income = incomeList.get(position);
        holder.tvPrice.setText("₹" + income.getAmount());
        holder.tvCategory.setText(income.getCategory());
        holder.tvDate.setText(income.getDate());
        holder.tvTime.setText(income.getTime());

        holder.itemView.setOnClickListener(v -> {

            Context context = holder.itemView.getContext();
            // Show a toast with the document ID
            String docId = income.getDocId() != null ? income.getDocId() : "";
            Intent intent = new Intent(context,TransactionView.class);
            intent.putExtra("docId", docId);
            intent.putExtra("from", "expense");
            context.startActivity(intent);
            //Toast.makeText(holder.itemView.getContext(), "Doc ID: " + docId, Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnLongClickListener(v -> {
            // Show a confirmation dialog or a toast for delete option
            // Show a toast with the document ID
            showDeleteDialog(holder.itemView.getContext(), income, position);
            return true;  // Return true to indicate the long click was handled
        });
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public static class IncomeViewHolder extends RecyclerView.ViewHolder {

        TextView tvPrice, tvCategory, tvDate, tvTime;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private void showDeleteDialog(Context context, IncomeModel income, int position) {
        // Create an AlertDialog to confirm deletion
        new AlertDialog.Builder(context)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Perform delete action
                    deleteIncome(income, position, context);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing, just dismiss the dialog
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void deleteIncome(IncomeModel income, int position, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get the current user ID
        String date = income.getDate();  // Assuming the `IncomeModel` contains a `getYear()` method
        String[] dateParts = date.split(" ");
        String year = dateParts[2];
        String month = getMonthNumber(dateParts[1]);


        String docId = income.getDocId();  // The document ID of the income entry
        double amount = income.getAmount();

        if (userId != null && year != null && month != null && docId != null) {
            // Construct the path to the document to be deleted
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("users")
                    .document(userId)
                    .collection("expense")
                    .document(year)
                    .collection(month)
                    .document(docId);

            DocumentReference documentReference1 = db.collection("users")
                    .document(userId)
                    .collection("transaction")
                    .document(year)
                    .collection(month)
                    .document(docId);

            // Delete the document
            documentReference.delete()
                    .addOnSuccessListener(aVoid -> {
                        incomeList.remove(position);  // Remove the item from the local list
                        notifyItemRemoved(position);  // Notify the adapter
                        Toast.makeText(context, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                        updateTotalIncome(year,month,amount,context);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting expense", Toast.LENGTH_SHORT).show();
                    });

            documentReference1.delete()
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting expense", Toast.LENGTH_SHORT).show();
                    });
        }
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

    private void updateTotalIncome(String year, String month, double newIncome,Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference totalIncomeDoc = firestore.collection("users").document(userId)
                .collection("expense").document(year).collection(month).document("totalExpense");

        // Get the current total income for the month
        totalIncomeDoc.get().addOnSuccessListener(documentSnapshot -> {
            double currentTotal = documentSnapshot.exists() ? documentSnapshot.getDouble("total") : 0.0;
            double updatedTotal = currentTotal - newIncome;

            // Update the total income for that month
            totalIncomeDoc.set(Map.of("total", updatedTotal))
                    .addOnSuccessListener(aVoid -> {
                        // Update yearly total income
                        updateYearlyIncome(year, newIncome,context);

                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update monthly total income.", Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(context, "Failed to load current monthly income.", Toast.LENGTH_SHORT).show());
    }

    private void updateYearlyIncome(String year, double newIncome, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference yearlyIncomeDoc = firestore.collection("users").document(userId)
                .collection("expense").document("totalYearlyExpense"); // No need to use the year here

        // Check if the totalYearlyIncome document exists
        yearlyIncomeDoc.get().addOnSuccessListener(documentSnapshot -> {
            double currentYearlyTotal = documentSnapshot.exists() ? documentSnapshot.getDouble("total") : 0.0;
            double updatedYearlyTotal = currentYearlyTotal - newIncome;

            // Set or update the total yearly income in the document
            yearlyIncomeDoc.set(Map.of("total", updatedYearlyTotal))
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update yearly income.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(context, "Failed to load current yearly income.", Toast.LENGTH_SHORT).show());
    }
}
