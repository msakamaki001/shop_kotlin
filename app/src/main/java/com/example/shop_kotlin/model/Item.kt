package com.example.shop_kotlin.model

import java.io.Serializable

data class Item(val id:Int = -1, val name:String = "", val price:Int = -1, val num:Int = -1, val category_id:Int = -1, val image_path:String = "")
