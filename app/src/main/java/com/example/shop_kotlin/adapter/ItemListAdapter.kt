package com.example.shop_kotlin.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.shop_kotlin.R
import com.example.shop_kotlin.model.Item
import java.io.InputStream
import java.net.URL
import kotlin.concurrent.thread

class ItemListAdapter (private val itemList: List<Item>): RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image_view)
        val name: TextView = view.findViewById(R.id.name_view)
        val price: TextView = view.findViewById(R.id.price_view)
        val id: TextView = view.findViewById(R.id.id_view)
    }

    var itemClickListener: OnItemClickListener? = null
    interface OnItemClickListener {
        fun onItemClick(holder: ViewHolder)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, viewGroup, false)
        val holder = ViewHolder(view)
        view.setOnClickListener{
            itemClickListener?.onItemClick(holder)
        }
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        thread {
            val item = itemList[position]
            val image_path = item.image_path.replace("localhost","10.0.2.2")
            val url = URL(image_path)
            val inputStream: InputStream = url.openStream()
            val bitMap = BitmapFactory.decodeStream(inputStream)
            Handler(Looper.getMainLooper()).post {
                viewHolder.image.setImageBitmap(bitMap)
                viewHolder.name.text = item.name
                viewHolder.price.text = item.price.toString()+"円"
                viewHolder.id.text = item.id.toString()
                viewHolder.id.isVisible = false
            }
        }
    }

    override fun getItemCount() = itemList.size
}
