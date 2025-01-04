package com.example.financemanager;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MutualFundApi {
    @GET("/search") // Replace with your endpoint
    List<FundDetails> getDetais();
}
