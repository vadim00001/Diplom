package com.example.diplov_v1

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.EatingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EatingActivity : AppCompatActivity() {
    private lateinit var bg: EatingBinding

    private lateinit var database: Db

    private var launcher: ActivityResultLauncher<Intent>? = null
    private var launcherAddProduct: ActivityResultLauncher<Intent>? = null

    private var listNutrData: List<ListNutrEntity> = listOf()

    private var nutrName: String = ""
    private var date: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = EatingBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_eating)

        nutrName = intent.getStringExtra("nutrName")!!
        date = intent.getStringExtra("date")!!

        database = Db.getDb(this)

        search(query = "")

        var selectedIndex = 0

        bg.editTxtSearch.addTextChangedListener {
            val query = it.toString()
            search(query)
        }

        bg.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedIndex = bg.tabLayout.selectedTabPosition
                if (selectedIndex == 1) {
                    Thread {
                        val data = database.productsDao().getRecentProducts()
                        runOnUiThread {
                            updateList(data)
                        }
                    }.start()
                } else if (selectedIndex == 2) {
                    Thread {
                        val data = database.productsDao().getUserProducts()
                        runOnUiThread {
                            updateList(data)
                        }
                    }.start()
                } else search(query = "")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        bg.floatingButton.setOnClickListener {
            val intent = Intent(this, AddUserProduct::class.java)
            launcherAddProduct?.launch(intent)
        }

        launcherAddProduct =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                }
            }

        bg.listViewEating.setOnItemClickListener { _, _, position, _ ->
            val selectedItem =
                bg.listViewEating.getItemAtPosition(position) as String

            lifecycleScope.launch(Dispatchers.IO) {
                listNutrData = database.listNutrDao().getNutrNameProducts(nutrName, date, date)

                withContext(Dispatchers.Main) {
                    val listProductName = listNutrData.map { it.productName }
                    if (listProductName.contains(selectedItem)) {
                        Toast.makeText(
                            this@EatingActivity,
                            getString(R.string.Error_AlreadyExist),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = Intent(this@EatingActivity, ProductActivity::class.java)
                        intent.putExtra("productName", selectedItem)
                        intent.putExtra("nutrName", nutrName)
                        intent.putExtra("date", date)
                        launcher?.launch(intent)
                    }
                }
            }
        }

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val productNameToListNutr = result.data?.getStringExtra("productNameToEating")
                    val productWeightToListNutr =
                        result.data?.getIntExtra("productWeightToEating", 0)
                    val productKcalValueToListNutr =
                        result.data?.getDoubleExtra("productKcalValueToEating", 0.0)
                    val productProteinValueToListNutr =
                        result.data?.getDoubleExtra("productProteinValueToEating", 0.0)
                    val productFatsValueToListNutr =
                        result.data?.getDoubleExtra("productFatsValueToEating", 0.0)
                    val productCarbValueToListNutr =
                        result.data?.getDoubleExtra("productCarbValueToEating", 0.0)

                    val intent = Intent()
                    intent.putExtra("productNameToListNutr", productNameToListNutr)
                    intent.putExtra("productWeightToListNutr", productWeightToListNutr)
                    intent.putExtra("productKcalValueToListNutr", productKcalValueToListNutr)
                    intent.putExtra("productProteinValueToListNutr", productProteinValueToListNutr)
                    intent.putExtra("productFatsValueToListNutr", productFatsValueToListNutr)
                    intent.putExtra("productCarbValueToListNutr", productCarbValueToListNutr)

                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

        bg.listViewEating.setOnItemLongClickListener { _, _, position, _ ->
            if (selectedIndex == 0 || selectedIndex == 1) {
                return@setOnItemLongClickListener false
            }

            val productName = bg.listViewEating.getItemAtPosition(position) as String

            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            bottomSheetDialog.setContentView(view)

            val btnOK = view.findViewById<Button>(R.id.btnOK)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)

            btnOK.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    database.productsDao().deleteUserProduct(productName)
                    val result = database.productsDao().getAllProducts()
                    withContext(Dispatchers.Main) {
                        bottomSheetDialog.dismiss()
                        updateList(result)
                    }
                }
            }

            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()

            true
        }
    }

    private fun search(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = database.productsDao()
                .search("%$query%")
            withContext(Dispatchers.Main) {
                updateList(result)
            }
        }
    }

    private fun updateList(result: List<ProductsEntity>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            result.map { it.productName })
        bg.listViewEating.adapter = adapter
    }
}