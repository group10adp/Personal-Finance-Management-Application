package com.example.financemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class InvestmentView extends AppCompatActivity {
    String docId,from;
    FirebaseAuth auth;
    private String userId;

    private TextView spinnerMutualFund;
    private EditText editTextReturnRate, editTextAmount,editTextCurrPrice;
    Button submitbtn;

    private TextView dateText;
    private TextView timeText;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_view);

        Intent intent = getIntent();
        docId = intent.getStringExtra("docId");

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        spinnerMutualFund = findViewById(R.id.spinner_mutual_fund);
        editTextReturnRate = findViewById(R.id.edittext_return_rate);
        editTextAmount = findViewById(R.id.edittext_amount);
        submitbtn=findViewById(R.id.save);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        ImageView dateIcon = findViewById(R.id.dateIcon);
        ImageView timeIcon = findViewById(R.id.timeIcon);

        firestore = FirebaseFirestore.getInstance();

        editTextReturnRate.setFocusable(false); // Disable focus
        editTextReturnRate.setFocusableInTouchMode(false); // Disable focus in touch mode
        editTextReturnRate.setClickable(false);

        editTextAmount.setFocusable(false); // Disable focus
        editTextAmount.setFocusableInTouchMode(false); // Disable focus in touch mode
        editTextAmount.setClickable(false);

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);


        firestore.collection("users")
                .document(userId)
                .collection("investment")
                .document(year) // Year
                .collection(month) // Month
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //Log.d("DocumentSnapshot", documentSnapshot.getData().toString());

                        Investment expenseEntry = documentSnapshot.toObject(Investment.class);
                        if (expenseEntry != null) {
                            // Use the retrieved data
                            double  amount = expenseEntry.getAmount();
                            editTextAmount.setText(String.valueOf(amount));


                            String mutualFund = expenseEntry.getMutualFund();
                            //Log.d("mutualName",mutualFund);
                            if (mutualFund == null) {
                                mutualFund = "Unknown"; // Default category
                            }
                            spinnerMutualFund.setText(mutualFund);

                            String date = expenseEntry.getDate();
                            if (date == null) {
                                date = "01 Jan 1970"; // Default date for invalid entries
                            }
                            dateText.setText(date);

                            String note = expenseEntry.getReturnRate();
                            if (note == null) {
                                note = ""; // Default category
                            }

                            editTextReturnRate.setText(note);

                            String time = expenseEntry.getTime();
                            if (time == null) {
                                time = "12:00 AM"; // Default time for invalid entries
                            }
                            timeText.setText(time);

                        }
                        Log.d("DataRetrieved", "Amount: ");

                    } else {
                        Log.e("FirestoreError", "No such document found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error retrieving document: ", e);
                });



        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());


    }
}