package com.example.diplov_v1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.ProductBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ProductActivity : AppCompatActivity() {
    private lateinit var bg: ProductBinding

    private lateinit var database: Db

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = ProductBinding.inflate(layoutInflater)
        setContentView(bg.root)

        database = Db.getDb(this)

        supportActionBar?.title = getString(R.string.title_activity_product)

        var productKcalValue = 0.0
        var productProteinValue = 0.0
        var productFatsValue = 0.0
        var productCarbValue = 0.0

        var productKcalNew = 0.0
        var productProteinNew = 0.0
        var productFatsNew = 0.0
        var productCarbNew = 0.0

        //var flag = 0

        //val flagCheck = intent.getIntExtra("flagCheck", 0)
        val productName = intent.getStringExtra("productName")!!
        var productWeight = intent.getIntExtra("productWeight", 100)

        val nutrName = intent.getStringExtra("nutrName")!!
        val date = intent.getStringExtra("date")!!
        //val selectedIndex = intent.getIntExtra("selectedIndex", 0)

        var productInfo: Any

        lifecycleScope.launch(Dispatchers.IO) {
/*
            val productInfo =
                productName?.let {
                    database.productsDao()
                        .getProductInfo(it)
                }
*/
            productInfo = database.productsDao().getProductInfo(productName)
            if (productInfo == null) {
                productInfo =
                    database.listNutrDao().getProductInfo(nutrName, productName, date, date)
            }

            withContext(Dispatchers.Main) {
                when (productInfo) {
                    is ProductsEntity -> {
                        productProteinValue = (productInfo as ProductsEntity).proteinValue
                        productFatsValue = (productInfo as ProductsEntity).fatValue
                        productCarbValue = (productInfo as ProductsEntity).carbohydrateValue
                        productKcalValue = (productInfo as ProductsEntity).kcalValue
                    }

                    is ListNutrEntity -> {
                        productProteinValue = (productInfo as ListNutrEntity).protein
                        productFatsValue = (productInfo as ListNutrEntity).fats
                        productCarbValue = (productInfo as ListNutrEntity).carb
                        productKcalValue = (productInfo as ListNutrEntity).kcal
                    }
                }

                bg.txtProductName.text = productName
                bg.txtProductCalories.text = productKcalValue.roundToInt().toString()
                bg.txtProductProtein.text = productProteinValue.roundToInt().toString()
                bg.txtProductFats.text = productFatsValue.roundToInt().toString()
                bg.txtProductCarb.text = productCarbValue.roundToInt().toString()
                bg.editTxtProductWeight.setText(productWeight.toString())
            }
        }

        bg.btnDoneProduct.setOnClickListener()
        {
            val intent = Intent()
            //intent.putExtra("flagToEating", flag)
            intent.putExtra("productNameToEating", productName)
            intent.putExtra("productWeightToEating", productWeight)
            intent.putExtra("productProteinValueToEating", productProteinNew)
            intent.putExtra("productFatsValueToEating", productFatsNew)
            intent.putExtra("productCarbValueToEating", productCarbNew)
            intent.putExtra("productKcalValueToEating", productKcalNew)

            setResult(RESULT_OK, intent)
            finish()
        }

        bg.editTxtProductWeight.addTextChangedListener()
        { editable ->
            val weightText = editable.toString()
            if (weightText.isNotEmpty()) {
                val weight = weightText.toIntOrNull()
                if (weight != null && weight > 0) {
                    productWeight = weight
                    productKcalNew = productKcalValue / 100 * weight
                    productProteinNew = productProteinValue / 100 * weight
                    productFatsNew = productFatsValue / 100 * weight
                    productCarbNew = productCarbValue / 100 * weight

                    bg.txtProductCalories.text = productKcalNew.roundToInt().toString()
                    bg.txtProductProtein.text = productProteinNew.roundToInt().toString()
                    bg.txtProductFats.text = productFatsNew.roundToInt().toString()
                    bg.txtProductCarb.text = productCarbNew.roundToInt().toString()
                }
            }
        }
    }
}