package com.example.diplov_v1

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.ListNutritionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ListNutrition : AppCompatActivity() {
    private lateinit var bg: ListNutritionBinding

    private lateinit var database: Db

    private var listProducts: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>

    private var launcher: ActivityResultLauncher<Intent>? = null
    private var launcherEdit: ActivityResultLauncher<Intent>? = null

    private var nutrName: String = ""
    private var productName: String = ""
    private var productWeightValue: Int = 0
    private var productKcalValue: Double = 0.0
    private var productProteinValue: Double = 0.0
    private var productFatsValue: Double = 0.0
    private var productCarbValue: Double = 0.0

    private var sumProductsKcalValue: Double = 0.0
    private var sumProductsProteinValue: Double = 0.0
    private var sumProductsFatsValue: Double = 0.0
    private var sumProductsCarbValue: Double = 0.0

    private var date: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = ListNutritionBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_list_nutrition)

        database = Db.getDb(this)

        nutrName = intent.getStringExtra("nutrName")!!
        date = intent.getStringExtra("date")!!
        bg.txtMealName.text = nutrName

        bg.floatingActionButton.setOnClickListener {
            val intent = Intent(this, EatingActivity::class.java)
            intent.putExtra("nutrName", nutrName)
            intent.putExtra("date", date)
            launcher?.launch(intent)
        }

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    productName = result.data?.getStringExtra("productNameToListNutr")!!

                    productWeightValue = result.data?.getIntExtra("productWeightToListNutr", 0)!!

                    productKcalValue =
                        result.data?.getDoubleExtra("productKcalValueToListNutr", 0.0)!!
                    sumProductsKcalValue += productKcalValue

                    productProteinValue =
                        result.data?.getDoubleExtra("productProteinValueToListNutr", 0.0)!!
                    sumProductsProteinValue += productProteinValue

                    productFatsValue =
                        result.data?.getDoubleExtra("productFatsValueToListNutr", 0.0)!!
                    sumProductsFatsValue += productFatsValue

                    productCarbValue =
                        result.data?.getDoubleExtra("productCarbValueToListNutr", 0.0)!!
                    sumProductsCarbValue += productCarbValue

                    listProducts.add(productName)
                    adapter.add(listProducts.last())
                    bg.listView.adapter = adapter

                    updateCountProduct()
                    saveProductToDb()
                    setTxtViewText()
                }
            }

        bg.btnDone.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

        bg.listView.setOnItemClickListener { _, _, position, _ ->
            productName = bg.listView.getItemAtPosition(position) as String
            lifecycleScope.launch(Dispatchers.IO) {
                val productInfo =
                    database.listNutrDao().getProductInfo(nutrName, productName, date, date)
                withContext(Dispatchers.Main) {
                    val productWeight = productInfo.weight
                    val intent = Intent(this@ListNutrition, ProductActivity::class.java)
                    intent.putExtra("productName", productName)
                    intent.putExtra("productWeight", productWeight)
                    intent.putExtra("nutrName", nutrName)
                    intent.putExtra("date", date)
                    launcherEdit?.launch(intent)
                }
            }
        }

        launcherEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val productInfo =
                            database.listNutrDao()
                                .getProductInfo(nutrName, productName, date, date)
                        withContext(Dispatchers.Main) {
                            sumProductsKcalValue -= productInfo.kcal
                            sumProductsProteinValue -= productInfo.protein
                            sumProductsFatsValue -= productInfo.fats
                            sumProductsCarbValue -= productInfo.carb

                            productWeightValue =
                                result.data?.getIntExtra("productWeightToEating", 0)!!
                            productKcalValue =
                                result.data?.getDoubleExtra("productKcalValueToEating", 0.0)!!
                            productProteinValue =
                                result.data?.getDoubleExtra(
                                    "productProteinValueToEating",
                                    0.0
                                )!!
                            productFatsValue =
                                result.data?.getDoubleExtra("productFatsValueToEating", 0.0)!!
                            productCarbValue =
                                result.data?.getDoubleExtra("productCarbValueToEating", 0.0)!!

                            sumProductsKcalValue += productKcalValue
                            sumProductsProteinValue += productProteinValue
                            sumProductsFatsValue += productFatsValue
                            sumProductsCarbValue += productCarbValue

                            Thread {
                                database.listNutrDao()
                                    .updateProduct(
                                        nutrName,
                                        productName,
                                        productWeightValue,
                                        productKcalValue,
                                        productProteinValue,
                                        productFatsValue,
                                        productCarbValue,
                                        date,
                                        date
                                    )
                            }.start()
                            setTxtViewText()
                        }
                    }
                }
            }

        bg.listView.setOnItemLongClickListener { _, _, position, _ ->
            productName = bg.listView.getItemAtPosition(position) as String

            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            bottomSheetDialog.setContentView(view)

            val btnOK = view.findViewById<Button>(R.id.btnOK)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)

            btnOK.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val productInfo =
                        database.listNutrDao().getProductInfo(nutrName, productName, date, date)
                    database.listNutrDao().deleteProduct(nutrName, productName, date, date)
                    withContext(Dispatchers.Main) {
                        bottomSheetDialog.dismiss()

                        listProducts.remove(productName)
                        adapter.remove(productName)
                        bg.listView.adapter = adapter

                        sumProductsKcalValue -= productInfo.kcal
                        sumProductsProteinValue -= productInfo.protein
                        sumProductsFatsValue -= productInfo.fats
                        sumProductsCarbValue -= productInfo.carb

                        setTxtViewText()
                    }
                }
            }

            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()

            true
        }

        loadFromDb()
    }

    private fun updateCountProduct() {
        Thread {
            var count = database.productsDao().getProductInfo(productName).count
            count += 1
            database.productsDao().updateCount(count, productName)
        }.start()
    }

    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val data = database.listNutrDao().getNutrNameProducts(nutrName, date, date)
            withContext(Dispatchers.Main) {
                val productNames = data.map { it.productName }
                adapter = ArrayAdapter<String>(
                    this@ListNutrition,
                    android.R.layout.simple_list_item_1,
                    productNames
                )
                bg.listView.adapter = adapter

                data.forEach {
                    sumProductsKcalValue += it.kcal
                    sumProductsProteinValue += it.protein
                    sumProductsFatsValue += it.fats
                    sumProductsCarbValue += it.carb
                }

                setTxtViewText()
            }
        }
    }

    private fun saveProductToDb() {
        val productInfo = ListNutrEntity(
            null,
            0,
            0,
            nutrName,
            date,
            productName,
            productWeightValue,
            productKcalValue,
            productProteinValue,
            productFatsValue,
            productCarbValue
        )

        Thread {
            database.listNutrDao().insertData(productInfo)
        }.start()
    }

    private fun setTxtViewText() {
        bg.txtKcalValue.text = "\t\tКалории: ${sumProductsKcalValue.roundToInt()} ккал"
        bg.txtProteinValue.text = "\t\tБелки: ${sumProductsProteinValue.roundToInt()} г"
        bg.txtFatsValue.text = "\t\tЖиры: ${sumProductsFatsValue.roundToInt()} г"
        bg.txtCarbValue.text = "\t\tУглеводы: ${sumProductsCarbValue.roundToInt()} г"
    }
}
