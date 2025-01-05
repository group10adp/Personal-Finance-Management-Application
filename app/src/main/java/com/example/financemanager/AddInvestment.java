package com.example.financemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddInvestment extends Fragment {

    private Spinner spinnerMutualFund;
    private EditText editTextReturnRate, editTextAmount,editTextCurrPrice;
    private Button submitButton;

    private TextView dateText;
    private TextView timeText;

    private FirebaseFirestore firestore;

    private FirebaseAuth auth;

    private String userId;
    private Map<String, String> mutualFundMap;

    // Setup Retrofit

    public AddInvestment() {
        // Required empty public constructor
    }

    public static AddInvestment newInstance(String param1, String param2) {
        AddInvestment fragment = new AddInvestment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_investment, container, false);

        // Initialize views
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        spinnerMutualFund = view.findViewById(R.id.spinner_mutual_fund);
        editTextReturnRate = view.findViewById(R.id.edittext_return_rate);
        editTextAmount = view.findViewById(R.id.edittext_amount);
        editTextCurrPrice=view.findViewById(R.id.edittext_current_price);
        submitButton = view.findViewById(R.id.submit_button);
        dateText = view.findViewById(R.id.dateText);
        timeText = view.findViewById(R.id.timeText);
        ImageView dateIcon = view.findViewById(R.id.dateIcon);
        ImageView timeIcon = view.findViewById(R.id.timeIcon);

        firestore = FirebaseFirestore.getInstance();

        dateText.setText(getCurrentDate());
        timeText.setText(getCurrentTime());

        dateIcon.setOnClickListener(v -> showDatePicker());
        dateText.setOnClickListener(v -> showDatePicker());
        timeIcon.setOnClickListener(v -> showTimePicker());
        timeText.setOnClickListener(v -> showTimePicker());

        // Initialize mutual fund map
        initializeMutualFundMap();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(mutualFundMap.keySet())
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMutualFund.setAdapter(adapter);

        spinnerMutualFund.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected mutual fund name
                String selectedFund = parent.getItemAtPosition(position).toString();

                // Retrieve the corresponding fund code
                String fundCode = mutualFundMap.get(selectedFund);

                performApiRequest(fundCode);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle the case when nothing is selected (if needed)
            }
        });

        // TextWatcher to enable Save button only if all fields are filled
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        editTextReturnRate.addTextChangedListener(textWatcher);
        editTextAmount.addTextChangedListener(textWatcher);

        // Handle Save button click
        submitButton.setOnClickListener(v -> {
            String mutualFund = spinnerMutualFund.getSelectedItem().toString();
            String fundCode = mutualFundMap.get(mutualFund); // Retrieve fund code
            String returnRate = editTextReturnRate.getText().toString();
            String amountStr = editTextAmount.getText().toString();
            String date = dateText.getText().toString().trim();
            String time = timeText.getText().toString().trim();

            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);

                // Get current year and month
                String[] dateParts = date.split(" ");
                String year = dateParts[2];
                String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1); // Get the current month in MM format

                // Create an InvestmentEntry object
                InvestmentEntry investmentEntry = new InvestmentEntry(mutualFund, fundCode, returnRate, amount, date, time);

                // Save investment entry to Firestore
                firestore.collection("users").document(userId)
                        .collection("investment")
                        .document(year)
                        .collection(month)
                        .add(investmentEntry)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Investment saved successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save investment.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void performApiRequest(String fundCode) {
        // Trim the fundCode to remove leading and trailing spaces
        fundCode = fundCode.trim();

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://stockviewer-production-ae96.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of the ApiService
        ApiService apiService = retrofit.create(ApiService.class);

        // Make the API call
        Call<ResponseBody> call = apiService.searchFund(fundCode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse the response and display it
                        String responseBody = response.body().string();

                        // Parse the JSON response using JSONObject
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONObject stockInfo = jsonObject.getJSONObject("stock_info");
                        String currentPrice = stockInfo.getString("current_price");
                        JSONObject growthRates = stockInfo.getJSONObject("growth_rates");
                        String oneYearGrowthRate = growthRates.getString("1_year");
                        // Set the value to EditText
                        editTextReturnRate.setText(oneYearGrowthRate);
                        editTextCurrPrice.setText(currentPrice);
                        //Toast.makeText(getContext(), "Response: " + responseBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "API call unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initializeMutualFundMap() {
        mutualFundMap = new HashMap<>();
        mutualFundMap.put("ICICI Prudential Bluechip Fund", "ICICINIFTY.NS");
        mutualFundMap.put("HDFC Top 100 Fund", "HDFCNIFETF.NS");
        mutualFundMap.put("Axis Bluechip Fund", "AXISNIFTY.NS");
        mutualFundMap.put("Nippon India Mutual Fund", "08GPG.BO");
        mutualFundMap.put("Kotak Equity Savings Fund", "0P00014HE7.BO");
        mutualFundMap.put("Reliance Life Pure Equity Fund", "0P0000NPKG.BO");
        mutualFundMap.put("Aditya Birla Sun Life MNC Fund ", "0P00005V5R.BO");
        mutualFundMap.put("IDFC Nifty 50 ETF", "IDFNIFTYET.NS");
        mutualFundMap.put("HDFC Pharma and Healthcare Fund", "0P0001RK6V.BO");
        mutualFundMap.put("Kotak Low Duration Fund", "0P0000GBDS.BO");
        mutualFundMap.put("Tata AIA Life-Super Select Equity", "0P0000NQJX.BO");
        mutualFundMap.put("Tata Multicap Dir IDCW-P", "0P0001QCAT.BO");
        mutualFundMap.put("Sundaram Select Focus Fund", "0P000162E4");
        mutualFundMap.put("SBI Small Cap Fund", "0P0001BB9I.BO");
        mutualFundMap.put("Tata Small Cap Fund", "0P0001EUZV.BO");
        mutualFundMap.put("Nippon India Short Term Mn IDCW-R", "0P0001BB5M.BO");
        mutualFundMap.put("NIFTY100 ESG", "NIFTY100_ESG.NS");
        mutualFundMap.put("Motilal Oswal S&P 500 Index Reg Gr", "0P0001JMZC.BO");
        mutualFundMap.put("Invesco India Growth Fund", "INVESCOETF.NS");
        mutualFundMap.put("SBI PSU Dir Gr", "0P0000XVLF.BO");
        mutualFundMap.put("SBI Long Term Equity Fund Dir Gr", "0P0000XVL9.BO");
        mutualFundMap.put("Groww Nifty India Defence Etf", "GROWWDEFNC.NS");
        mutualFundMap.put("UTI Nifty Bank ETF", "UTIBANKETF.NS");
        mutualFundMap.put("Others", "Others");

        // Add the rest of the items as needed
    }


    private void validateInput() {
        String returnRate = editTextReturnRate.getText().toString().trim();
        String amount = editTextAmount.getText().toString().trim();
        submitButton.setEnabled(!returnRate.isEmpty() && !amount.isEmpty());
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(calendar.getTime());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    dateText.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String amPm = selectedHour >= 12 ? "PM" : "AM";
                    int hourIn12Format = selectedHour % 12 == 0 ? 12 : selectedHour % 12;
                    timeText.setText(String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm));
                },
                hour, minute, false);

        timePickerDialog.show();
    }

    public static class InvestmentEntry {
        private String mutualFund;
        private String fundCode;
        private String returnRate;
        private double amount;
        private String date;
        private String time;

        public InvestmentEntry() {
        }

        public InvestmentEntry(String mutualFund, String fundCode, String returnRate, double amount, String date, String time) {
            this.mutualFund = mutualFund;
            this.fundCode = fundCode;
            this.returnRate = returnRate;
            this.amount = amount;
            this.date = date;
            this.time = time;
        }

        public String getMutualFund() {
            return mutualFund;
        }

        public String getFundCode() {
            return fundCode;
        }

        public String getReturnRate() {
            return returnRate;
        }

        public double getAmount() {
            return amount;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }
    }
}