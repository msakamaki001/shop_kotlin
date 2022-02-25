package com.example.shop_kotlin.api

import com.example.shop_kotlin.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
    @POST("login")
    fun login(@Body data: LoginData): Call<Customer>

    @POST("fetch_categories")
    fun fetch_categories(): Call<List<Category>>

    @POST("fetch_items")
    fun fetch_items(@Body data: ItemsData): Call<List<Item>>

    @POST("fetch_cart_items")
    fun fetch_cart_items(@Body data: ItemsData): Call<List<Item>>

    @POST("buy_cart_items")
    fun buy_cart_items(@Body data: ItemsData): Call<Int>
}