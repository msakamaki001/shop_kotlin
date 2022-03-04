package com.example.shop_kotlin.fragment

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shop_kotlin.R
import com.example.shop_kotlin.adapter.DialogItemListAdapter
import com.example.shop_kotlin.api.APIService
import com.example.shop_kotlin.model.Category
import com.example.shop_kotlin.model.Item
import com.example.shop_kotlin.model.ItemsData
import com.example.shop_kotlin.setting.Setting
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread


class ButtomSheetFragment(): BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_bottom_sheet,container,false)
        val cartItemsView = view.findViewById<RecyclerView>(R.id.cartItemList)
        val bundle = arguments
        val cartItems = bundle?.getParcelableArrayList<Parcelable>("cartItems") as ArrayList<Item>
        val adapter = DialogItemListAdapter(cartItems)
        cartItemsView.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL,false)
        cartItemsView.adapter = adapter
        val swipeToDismissTouchHelper = getSwipeToDismissTouchHelper(adapter)
        swipeToDismissTouchHelper.attachToRecyclerView(cartItemsView)

        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dismiss()
        }
        val cart = activity?.getSharedPreferences("cartItems", Context.MODE_PRIVATE)
        val str = cart?.getString("id",null)
        val buyButton = view.findViewById<Button>(R.id.buyButton)
        buyButton.isEnabled = !str.isNullOrEmpty()
        buyButton.setOnClickListener {
            buyCartItems()
        }

        return view
    }

    fun buyCartItems() {
        val cart = activity?.getSharedPreferences("cartItems", Context.MODE_PRIVATE)
        val str = cart?.getString("id",null)
        val cartItems = str?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() }
        buyCartItems(cartItems!!) { result ->
            if (result == 1) {
                cart.edit().putString("id","").apply()
                dismiss()
                Snackbar.make(activity!!.findViewById(android.R.id.content) , "購入しました", Snackbar.LENGTH_LONG).show()
            } else {
                dismiss()
                Snackbar.make(activity!!.findViewById(android.R.id.content) , "購入失敗しました", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    fun buyCartItems(cartItems:List<Int>, callback:(Int)->Unit) {
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
                service.buy_cart_items(ItemsData(cartItems = cartItems))
                    .enqueue(object : Callback<Int> {
                        override fun onFailure(call: Call<Int>, t: Throwable) {
                            throw t
                        }

                        override fun onResponse(
                            call: Call<Int>,
                            response: Response<Int>
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
                Log.d("buyCartItems():error", "$e")
            }
        }
    }

    fun getSwipeToDismissTouchHelper(adapter: DialogItemListAdapter) =
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val cart = activity?.getSharedPreferences("cartItems", Context.MODE_PRIVATE)
                var list = adapter.getItemList().toMutableList()
                list.removeAt(viewHolder.bindingAdapterPosition)
                adapter.setItemList(list)
                val str = list.map{it.id}.joinToString(",")
                cart!!.edit()!!.putString("id",str).apply()
                adapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
                val buyButton = view?.findViewById<Button>(R.id.buyButton)
                buyButton?.isEnabled = !str.isNullOrEmpty()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val itemView = viewHolder.itemView
                val background = ColorDrawable()
                background.color = Color.parseColor("#f44336")
                if (dX < 0)
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                else
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )

                background.draw(c)
            }
        })
}