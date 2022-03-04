package com.example.shop_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.shop_kotlin.api.APIService
import com.example.shop_kotlin.model.Customer
import com.example.shop_kotlin.model.LoginData
import com.example.shop_kotlin.setting.Setting
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class LoginActivity : AppCompatActivity() {
    val EXTRA_MESSAGE: String = "com.example.shop_kotlin.MESSAGE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun doLogin(mail:String, password:String, callback:(Customer)->Unit) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Setting.BASE_URL.url+"/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        try {
            val service: APIService = retrofit.create(APIService::class.java)
            service.login(LoginData(mail = mail, password = password))
                .enqueue(object:Callback<Customer>{
                    override fun onFailure(call: Call<Customer>, t: Throwable) {
                        throw t
                    }
                    override fun onResponse(call: Call<Customer>,
                                            response:Response<Customer>) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                callback(it)
                            }
                        } else {
                            val errorText = findViewById(R.id.errorText) as TextView
                            errorText.text = getString(R.string.fail_login)
                        }
                    }
                })
        } catch (e: Exception) {
            Log.d("doLogin():error", "$e")
        }
    }

    fun login(view: View) {
        val emailText: TextView = findViewById(R.id.editMail) as TextView
        val mail: String = emailText.text.toString()
        val passwordText: TextView = findViewById(R.id.editPassword) as TextView
        val password: String = passwordText.text.toString()

        if (validate(mail,password)) {
            this.doLogin(mail, password) {
                val intent = Intent(this@LoginActivity, ShopActivity::class.java)
                intent.putExtra(EXTRA_MESSAGE, it.id)
                startActivity(intent)
            }
        }
    }

    fun validate(mail:String, password:String):Boolean {
        val errorText = findViewById(R.id.errorText) as TextView
        if (mail.isEmpty()) {
            errorText.text = getString(R.string.mail_empty)
            return false
        }
        if (password.isEmpty()) {
            errorText.text = getString(R.string.password_empty)
            return false
        }
        return true
    }
}