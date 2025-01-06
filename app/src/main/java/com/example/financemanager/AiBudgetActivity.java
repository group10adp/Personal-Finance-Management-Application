package com.example.financemanager;

import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

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

public class AiBudgetActivity extends AppCompatActivity {

    String userId, year, month;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    TextView text_content;

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



        text_content=findViewById(R.id.text_content);
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        auth = FirebaseAuth.getInstance();
        userId = String.valueOf(auth.getCurrentUser().getUid());

        fetchUserData(userId, "2024");


    }

    private void fetchUserData(String userId, String tempYear) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Increase connection timeout
                .readTimeout(60, TimeUnit.SECONDS)     // Increase read timeout
                .writeTimeout(60, TimeUnit.SECONDS)    // Increase write timeout
                .build();
        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://20ca-103-51-148-13.ngrok-free.app/")
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

                        // Show the complete response in a Toast
//                        Toast.makeText(AiBudgetActivity.this, "Response: " + responseBody, Toast.LENGTH_LONG).show();

                        // Parse the response to extract dynamic categories
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONObject categories = jsonObject; // The whole response is the categories object
                        StringBuilder categoryData = new StringBuilder();

                        // Loop through all categories (keys) and append the values
                        Iterator<String> keys = categories.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            double value = categories.getDouble(key); // Get value associated with the key

                            value = Math.abs(value);

                            value = value - value * 0.05;

                            value = Math.round(value * 100.0) / 100.0;

                            categoryData.append(key).append(": ").append(value).append("\n");
                        }

                        text_content.setText(categoryData.toString());
                        progressDialog.dismiss();

                        // Display the category data in EditText or Toast
                        Log.d("API Response",categoryData.toString());
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