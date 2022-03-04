package com.example.shop_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shop_kotlin.adapter.ItemListAdapter
import com.example.shop_kotlin.api.APIService
import com.example.shop_kotlin.fragment.ButtomSheetFragment
import com.example.shop_kotlin.model.Category
import com.example.shop_kotlin.model.Item
import com.example.shop_kotlin.model.ItemsData
import com.google.android.material.tabs.TabLayout
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.shop_kotlin.setting.Setting


class ShopActivity : AppCompatActivity() {
    val _this = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        val intent: Intent = getIntent()
        val loginId: Int = intent.getIntExtra(LoginActivity().EXTRA_MESSAGE,-1)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar);
        val cartButton = findViewById<Button>(R.id.cartButton)
        cartButton.setOnClickListener{
            cartButtonClick()
        }
        val cart = getSharedPreferences("cartItems", Context.MODE_PRIVATE)
        cart.edit().putString("id","").apply()

        fetchCategories { categories ->
            val tabLayout = TabLayout(this)
            for (category in categories) {
                val tab = tabLayout.newTab()
                tab.id = category.id
                tab.setText(category.name)
                tabLayout.addTab(tab)
            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab) {
                    fetchCategories { categories ->
                        fetchItems(categoryId = tab.id) { items ->
                            setRecyclerView(_this, items)
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {

                }

                override fun onTabReselected(tab: TabLayout.Tab) {

                }
            })
            val linearLayout = findViewById<LinearLayout>(R.id.root)
            linearLayout.addView(tabLayout)
        }
        val handler = Handler(Looper.getMainLooper())
        val runnableCategories = object : Runnable {
            override fun run() {
                fetchCategories { categories ->
                    val tabLayout = TabLayout(_this)
                    for (category in categories) {
                        val tab = tabLayout.newTab()
                        tab.id = category.id
                        tab.setText(category.name)
                        tabLayout.addTab(tab)
                    }
                    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                        override fun onTabSelected(tab: TabLayout.Tab) {
                            fetchCategories { categories ->
                                fetchItems(categoryId = tab.id) { items ->
                                    setRecyclerView(_this, items)
                                }
                            }
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab) {

                        }

                        override fun onTabReselected(tab: TabLayout.Tab) {
                            fetchCategories { categories ->
                                fetchItems(categoryId = tab.id) { items ->
                                    setRecyclerView(_this, items)
                                }
                            }
                        }
                    })
                    val linearLayout = findViewById<LinearLayout>(R.id.root)
                    linearLayout.addView(tabLayout)
                    handler.postDelayed(this, 5000)
                }
            }
        }
        fetchCategories { categories ->
            fetchItems(categoryId = categories[0].id) { items ->
                setRecyclerView(_this, items)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRecyclerView(context: Context, items:List<Item>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager =
            GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        val adapter = ItemListAdapter(items)
        adapter.itemClickListener = object : ItemListAdapter.OnItemClickListener {
            @SuppressLint("WrongConstant")
            override fun onItemClick(holder: ItemListAdapter.ViewHolder) {
                AlertDialog.Builder(this@ShopActivity)
                    .setTitle("カートへ入れる")
                    .setPositiveButton("はい") { dialog, which ->
                        val cart = getSharedPreferences("cartItems", Context.MODE_APPEND)
                        val str = cart.getString("id", null)
                        cart.edit().putString("id", str + "," + holder.id.text.toString())
                            .apply()
                        dialog.dismiss()
                    }
                    .setNegativeButton("いいえ") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        recyclerView.adapter = adapter
    }

    fun fetchCategories(callback:(List<Category>)->Unit) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Setting.BASE_URL.url+"/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        thread {
            try {
                val service: APIService = retrofit.create(APIService::class.java)
                service.fetch_categories()
                    .enqueue(object : Callback<List<Category>> {
                        override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                            throw t
                        }

                        override fun onResponse(
                            call: Call<List<Category>>,
                            response: Response<List<Category>>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    callback(it)
                                }
                            } else {
                                Log.d("response-error", "response-error")
                            }
                        }
                    })
            } catch (e: Exception) {
                Log.d("fetch_categries():error", "$e")
            }
        }
    }

    fun fetchItems(categoryId:Int = -1, callback:(List<Item>)->Unit) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Setting.BASE_URL.url+"/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        thread {
            try {
                val service: APIService = retrofit.create(APIService::class.java)
                service.fetch_items(ItemsData(categoryId))
                    .enqueue(object : Callback<List<Item>> {
                        override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                            throw t
                        }

                        override fun onResponse(
                            call: Call<List<Item>>,
                            response: Response<List<Item>>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    callback(it)
                                }
                            } else {
                                Log.d("response-error", "response-error")
                            }
                        }
                    })
            } catch (e: Exception) {
                Log.d("fetch_items():error", "$e")
            }
        }
    }

    fun fetchCartItems(itemIds:List<Int>, callback:(List<Item>)->Unit) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Setting.BASE_URL.url+"/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        thread {
            try {
                val service: APIService = retrofit.create(APIService::class.java)
                service.fetch_cart_items(ItemsData(itemIds = itemIds))
                    .enqueue(object : Callback<List<Item>> {
                        override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                            throw t
                        }

                        override fun onResponse(
                            call: Call<List<Item>>,
                            response: Response<List<Item>>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    callback(it)
                                }
                            } else {
                                Log.d("response-error", "response-error")
                            }
                        }
                    })
            } catch (e: Exception) {
                Log.d("fetch_items():error", "$e")
            }
        }
    }

    @SuppressLint("WrongConstant", "WrongViewCast")
    fun cartButtonClick() {
        val cart = getSharedPreferences("cartItems", Context.MODE_PRIVATE)
        val str = cart.getString("id",null)
        val cartItems = str?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() }

        fetchCartItems(cartItems!!) { cartItems ->
            val bundle = Bundle()
            val list: ArrayList<Item> = ArrayList(cartItems)
            bundle.putParcelableArrayList("cartItems",list as ArrayList<Parcelable>)
            val bottomSheet = ButtomSheetFragment()
            bottomSheet.arguments = bundle
            bottomSheet.show(supportFragmentManager,"navigation_bottom_sheet")
        }
    }

    internal class ScrollController : OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            return true
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }
}
