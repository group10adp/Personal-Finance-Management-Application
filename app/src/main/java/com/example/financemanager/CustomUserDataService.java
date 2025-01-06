package com.example.financemanager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CustomUserDataService {
    @GET("{userId}/{tempYear}/{fixedValue}/{result}")
    Call<ResponseBody> getCustomUserData(
            @Path("userId") String userId,
            @Path("tempYear") String tempYear,
            @Path("fixedValue") String fixedValue,
            @Path("result") String result
    );
}
