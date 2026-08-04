package com.example.doline.data

import retrofit2.http.GET
import retrofit2.http.Query

interface UPCApiService {
    @GET("https://api.upcitemdb.com/prod/trial/lookup")
    suspend fun lookupUPC(
        @Query("upc") upc: String
    ): UPCResponse
}