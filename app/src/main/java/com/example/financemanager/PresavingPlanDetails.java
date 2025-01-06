package com.example.financemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PresavingPlanDetails extends AppCompatActivity {

    String userId, year, goalAmount,goalItem,timeSpan;
    FirebaseAuth auth;
    TextView text_content,text_details;
    String result;

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presaving_plan_details);
        Intent intent = getIntent();
        result = intent.getStringExtra("result");
        year = intent.getStringExtra("yearForData");
        goalAmount = intent.getStringExtra("goalAmount");
        goalItem = intent.getStringExtra("goalItem");
        timeSpan = intent.getStringExtra("timeSpan");
        boolean showLoading = intent.getBooleanExtra("showLoading", false);

        progressDialog = null;
        if (showLoading) {
            // Show a loading dialog
            progressDialog = new ProgressDialog(PresavingPlanDetails.this);
            progressDialog.setMessage("Fetching data...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        text_content=findViewById(R.id.text_content);
        text_details=findViewById(R.id.text_details);


        fetchCustomUserData(userId, year,result);

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void fetchCustomUserData(String userId, String tempYear, String result) {
        // Setup Retrofit
        Log.d("dataFor",tempYear+" "+result);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Increase connection timeout
                .readTimeout(60, TimeUnit.SECONDS)     // Increase read timeout
                .writeTimeout(60, TimeUnit.SECONDS)    // Increase write timeout
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://4f1a-45-64-224-104.ngrok-free.app/")
                .client(client)  // Set custom OkHttpClient
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // Create an instance of the service with a new name
        CustomUserDataService customUserDataService = retrofit.create(CustomUserDataService.class);

        // Construct the API endpoint dynamically
        String fixedValue = "4000";
        Call<ResponseBody> call = customUserDataService.getCustomUserData(userId, tempYear, fixedValue, result);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();

                        // Parse the response to extract budget analysis and summary
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // Extract budget analysis
                        JSONArray budgetAnalysisArray = jsonObject.getJSONArray("budget_analysis");

                        // Separate StringBuilder for budget analysis
                        StringBuilder budgetAnalysisData = new StringBuilder();
                        for (int i = 0; i < budgetAnalysisArray.length(); i++) {
                            JSONObject category = budgetAnalysisArray.getJSONObject(i);
                            String categoryName = category.getString("Category");
                            String notes = category.getString("Notes");
                            String suggestion = category.getString("Suggestion");

                            budgetAnalysisData.append("Category: ").append(categoryName).append("\n")
                                    .append("Notes: ").append(notes).append("\n")
                                    .append("Suggestion: ").append(suggestion).append("\n\n");
                        }

                        // Extract summary data
                        JSONObject summary = jsonObject.getJSONObject("summary");
                        String monthlySavingsEmi = summary.getString("monthly_savings_emi");


                        // Separate StringBuilder for summary data
                        StringBuilder summaryData = new StringBuilder();
                        summaryData.append("Goal: ").append(goalItem).append("\n")
                                .append("Monthly Savings EMI: ").append(monthlySavingsEmi).append("\n")
                                .append("Time Span: ").append(timeSpan).append("\n")
                                .append("Total Savings Goal: ").append(goalAmount).append("\n");

                        // Now, you have the data separated:
                        // budgetAnalysisData for budget analysis info
                        // summaryData for summary info
                        text_content.setText(budgetAnalysisData.toString());
                        text_details.setText(summaryData.toString());
                        progressDialog.dismiss();


                        Log.d("Budget Analysis Data", budgetAnalysisData.toString());
                        Log.d("Summary Data", summaryData.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(PresavingPlanDetails.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }


                } else {
                    Toast.makeText(PresavingPlanDetails.this, "API call unsuccessful", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PresavingPlanDetails.this, "Failed to fetch data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }


}
