package com.example.diplov_v1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diplov_v1.databinding.AddUserProductBinding

class AddUserProduct : AppCompatActivity() {
    private lateinit var bg: AddUserProductBinding

    private lateinit var database: Db

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = AddUserProductBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_add_user_product)

        database = Db.getDb(this)

        var productName: String
        var kcal: Double
        var protein: Double
        var fats: Double
        var carb: Double

        bg.btnAddUserProduct.setOnClickListener {
            if (bg.edTxtProductName.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.ErrorProductName), Toast.LENGTH_SHORT).show()
            }  else if (bg.edTxtKcal.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.ErrorCalories), Toast.LENGTH_SHORT).show()
            } else {
                productName = bg.edTxtProductName.text.toString()
                kcal = bg.edTxtKcal.text.toString().toDouble()
                protein =
                    bg.edTxtProtein.text.toString().takeIf { it.isNotBlank() }?.toDoubleOrNull()
                        ?: 0.0
                fats = bg.edTxtFats.text.toString().takeIf { it.isNotBlank() }?.toDoubleOrNull()
                    ?: 0.0
                carb = bg.edTxtCarb.text.toString().takeIf { it.isNotBlank() }?.toDoubleOrNull()
                    ?: 0.0

                val userProduct = ProductsEntity(
                    null,
                    1,
                    0,
                    productName,
                    protein,
                    fats,
                    carb,
                    kcal
                )

                Thread {
                    database.productsDao().insertProduct(userProduct)
                }.start()

                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
