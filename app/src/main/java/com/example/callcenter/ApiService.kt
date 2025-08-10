    package com.example.callcenter

    import retrofit2.Response
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import retrofit2.http.Body
    import retrofit2.http.POST

    interface ApiService {

        // ✅ Missed Call POST
        @POST("api/miss-call")
        suspend fun sendMissedCall(
            @Body body: ParamsWrapper<MissedCallParams>
        ): Response<Unit>

        // ✅ Received Call POST
        @POST("api/call-record")
        suspend fun saveCallRecord(
            @Body body: ParamsWrapper<ReceivedCallParams>
        ): Response<Unit>
    }

    object ApiClient {
        val apiService: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl("https://intl.itscholarbd.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }