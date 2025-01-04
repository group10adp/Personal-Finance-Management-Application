package com.example.financemanager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("search/{fundCode}")
    Call<ResponseBody> searchFund(@Path("fundCode") String fundCode);
}