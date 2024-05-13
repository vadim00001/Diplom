package com.example.diplov_v1

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.NutritionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class NutritionActivity : AppCompatActivity() {
    private lateinit var bg: NutritionBinding

    private lateinit var database: Db

    private val calendar = Calendar.getInstance()

    private lateinit var adapter: ArrayAdapter<String>
    private var listNutr: ArrayList<String> = ArrayList()

    private var launcher: ActivityResultLauncher<Intent>? = null

    private var kcalTotal: Double = 0.0
    private var proteinTotal: Double = 0.0
    private var fatsTotal: Double = 0.0
    private var carbTotal: Double = 0.0
    private var kcalProfile: Double = 0.0
    private var proteinProfile: Double = 0.0
    private var fatsProfile: Double = 0.0
    private var carbProfile: Double = 0.0

    private var date: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = NutritionBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.NutritionCard)

        database = Db.getDb(this)

        val items = ArrayList<String>()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        bg.listViewMeal.adapter = adapter

        updateCurrentDate(bg.txtDate)
        bg.txtDate.setOnClickListener { showDatePicker() }
        bg.btnDateBack.setOnClickListener { updateDateByOffset(-1) }
        bg.btnDateNext.setOnClickListener { updateDateByOffset(1) }

        bg.btnDoneNutr.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

        bg.floatingActionButton4.setOnClickListener { showPopupMenu(bg.floatingActionButton4) }

        bg.listViewMeal.setOnItemClickListener { _, _, position, _ ->
            val selectedItem =
                bg.listViewMeal.getItemAtPosition(position) as String
            val intent = Intent(this, ListNutrition::class.java)
            intent.putExtra("nutrName", selectedItem)
            intent.putExtra("date", date)
            launcher?.launch(intent)
        }

        bg.listViewMeal.setOnItemLongClickListener { _, _, position, _ ->
            val nutrName = bg.listViewMeal.getItemAtPosition(position) as String

            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            bottomSheetDialog.setContentView(view)

            val btnOK = view.findViewById<Button>(R.id.btnOK)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)

            btnOK.setOnClickListener {
                Thread {
                    bottomSheetDialog.dismiss()
                    database.listNutrDao().deleteNutrition(nutrName, date, date)
                    loadFromDb()
                }.start()
            }

            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()

            true
        }

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    loadFromDb()
                }
                if (result.resultCode == RESULT_CANCELED) {
                    loadFromDb()
                }
            }
    }

    private fun setTxtViewText() {
        bg.txtCalories.text = "${kcalTotal.roundToInt()}"
        bg.txtProtein.text = "${proteinTotal.roundToInt()}"
        bg.txtFats.text = "${fatsTotal.roundToInt()}"
        bg.txtCarb.text = "${carbTotal.roundToInt()}"

        bg.txtCaloriesProfile.text = "${kcalProfile.roundToInt()}"
        bg.txtProteinProfile.text = "${proteinProfile.roundToInt()}"
        bg.txtFatsProfile.text = "${fatsProfile.roundToInt()}"
        bg.txtCarbProfile.text = "${carbProfile.roundToInt()}"
    }

    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val profileData = database.profileDao().getProfileData()
            val nutrList = database.listNutrDao().getDayData(date, date)
            withContext(Dispatchers.Main) {
                kcalTotal = 0.0
                proteinTotal = 0.0
                fatsTotal = 0.0
                carbTotal = 0.0

                listNutr.clear()
                adapter.clear()

                val kcalTotalList = nutrList.map { it.kcal }
                kcalTotalList.forEach { kcalTotal += it }

                val proteinTotalList = nutrList.map { it.protein }
                proteinTotalList.forEach { proteinTotal += it }

                val fatsTotalList = nutrList.map { it.fats }
                fatsTotalList.forEach { fatsTotal += it }

                val carbTotalList = nutrList.map { it.carb }
                carbTotalList.forEach { carbTotal += it }

                if (profileData.isNotEmpty()) {
                    kcalProfile = profileData.last().kcal
                    proteinProfile = profileData.last().protein
                    fatsProfile = profileData.last().fats
                    carbProfile = profileData.last().carb
                }

                setTxtViewText()

                val nutrNameList = nutrList.map { it.nutrName }
                nutrNameList.forEach {
                    if (!listNutr.contains(it)) {
                        listNutr.add(it)
                        adapter.add(listNutr.last())
                    }
                }
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_popup_nutr)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val itemName = when (item.itemId) {
                R.id.breakfast -> getString(R.string.breakfast)
                R.id.lunch -> getString(R.string.lunch)
                R.id.dinner -> getString(R.string.dinner)
                R.id.morning_snack -> getString(R.string.morning_snack)
                R.id.afternoon_snack -> getString(R.string.afternoon_snack)
                R.id.evening_snack -> getString(R.string.evening_snack)
                else -> return@setOnMenuItemClickListener false
            }

            if (!listNutr.contains(itemName)) {
                listNutr.add(itemName)
                adapter.add(listNutr.last())
            }

            true
        }
        popupMenu.show()
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                handleSelectedDate(selectedYear, selectedMonth, selectedDayOfMonth)
            },
            year,
            month,
            dayOfMonth
        ).show()
    }

    private fun handleSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(year, month, dayOfMonth)
        updateCurrentDate(bg.txtDate)
    }

    private fun updateCurrentDate(textView: TextView) {
        val dateFormat = SimpleDateFormat("EEE, d MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        textView.text = currentDate

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val time = calendar.time
        date = formatter.format(time)

        loadFromDb()
    }

    private fun updateDateByOffset(offset: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, offset)
        updateCurrentDate(bg.txtDate)
    }
}
