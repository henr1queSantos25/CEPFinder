package com.unicamp.henr1que.cepfinder

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun getEndereco(view: View) {
        val input = findViewById<EditText>(R.id.input)
        val output = findViewById<TextView>(R.id.result)

        if (input.text.count() != 8) {
            Toast.makeText(this, "O CEP deve conter exatamente 8 caracteres", Toast.LENGTH_SHORT).show()
            Log.e("CEP", "O CEP deve conter exatamente 8 caracteres")
            return
        }

        val url = "https://brasilapi.com.br/api/cep/v2/${input.text}"
        request(url, output)
    }

    fun request(url: String, output: TextView) {
        val client = OkHttpClient()

        val request = okhttp3.Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("HTTP", "Erro ao executar a requisição")
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        output.visibility = View.GONE
                        Toast.makeText(
                            this@MainActivity,
                            "CEP não encontrado. Verifique o número digitado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("HTTP", "Resposta inesperada: ${response.code}")
                    return
                }

                val resposta = response.body?.string() ?: return
                Log.d("HTTP", resposta)

                val json = JSONObject(resposta)

                val cep = json.optString("cep")
                val state = json.optString("state")
                val city = json.optString("city")
                val neighborhood = json.optString("neighborhood")
                val street = json.optString("street")

                val location = json.optJSONObject("location")
                val coordinates = location?.optJSONObject("coordinates")

                val latitude = coordinates?.optString("latitude")
                val longitude = coordinates?.optString("longitude")

                runOnUiThread {
                    output.text = """
                        CEP: $cep
                        Estado: $state
                        Cidade: $city
                        Bairro: $neighborhood
                        Logradouro: $street
                        Latitude: $latitude
                        Longitude: $longitude
                    """.trimIndent()
                    output.visibility = View.VISIBLE
                }
            }
        })

    }
}
