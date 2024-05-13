package com.example.diplov_v1

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.WaterBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class WaterActivity : AppCompatActivity() {
    private lateinit var bg: WaterBinding

    private lateinit var database: Db

    private var waterNorm = 2000
    private var glassVolume = 250
    private var progress = 0
    private var date = ""
    private var waterItem: String = ""

    private var listWater: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>

    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = WaterBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_water)

        database = Db.getDb(this)

        updateCurrentDate(bg.txtDate)
        bg.txtDate.setOnClickListener { showDatePicker() }
        bg.btnDateBack.setOnClickListener { updateDateByOffset(-1) }
        bg.btnDateNext.setOnClickListener { updateDateByOffset(1) }

        bg.btnDrinkWater.setOnClickListener { btnDrinkWaterClick() }

        bg.listviewWater.setOnItemLongClickListener { _, _, position, _ ->
            val waterItem = bg.listviewWater.getItemAtPosition(position) as String

            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            bottomSheetDialog.setContentView(view)

            val btnOK = view.findViewById<Button>(R.id.btnOK)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)

            btnOK.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val itemInfo = database.waterCounterDao().getItemInfo(waterItem, date, date)
                    database.waterCounterDao().deleteItem(waterItem, date, date)
                    withContext(Dispatchers.Main) {
                        progress -= itemInfo.glassVolume
                        bg.progressBar.progress = progress
                        val stringTxt = "$progress / $waterNorm"
                        bg.txtProgress.text = stringTxt

                        listWater.remove(waterItem)
                        adapter.remove(waterItem)

                        bottomSheetDialog.dismiss()
                    }
                }
            }

            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()

            true
        }

        adapter = ArrayAdapter(
            this@WaterActivity,
            android.R.layout.simple_list_item_1,
            listWater
        )
    }

    private fun btnDrinkWaterClick() {
        if (bg.editTxtWaterNorm.text.isNullOrEmpty() || bg.editTxtGlassVolume.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.ErrorWater), Toast.LENGTH_SHORT).show()
            return
        }
        waterNorm = bg.editTxtWaterNorm.text.toString().toInt()
        glassVolume = bg.editTxtGlassVolume.text.toString().toInt()

        val calendarTime = Calendar.getInstance()
        val currentTime = calendarTime.time
        val formatter = SimpleDateFormat("HH:mm:ss")
        val time = formatter.format(currentTime)

        progress += glassVolume
        waterItem = "$time\t\t\t\t$glassVolume мл"
        listWater.add(waterItem)

        bg.progressBar.max = waterNorm
        bg.progressBar.progress = progress
        val stringTxt = "$progress / $waterNorm"
        bg.txtProgress.text = stringTxt
        bg.listviewWater.adapter = adapter

        saveToDb()
    }

    private fun saveToDb() {
        val data = WaterCounterEntity(
            null,
            date,
            0,
            waterNorm,
            glassVolume,
            waterItem
        )

        Thread {
            database.waterCounterDao().insert(data)
        }.start()
    }

    private fun loadFromDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            val waterData = database.waterCounterDao().getDayData(date, date)
            val profileData = database.profileDao().getProfileData()
            withContext(Dispatchers.Main) {
                progress = 0
                waterNorm = 2000
                glassVolume = 250
                listWater.clear()
                adapter.clear()

                if (waterData.isNotEmpty()) {
                    waterNorm = waterData.last().waterNorm
                    glassVolume = waterData.last().glassVolume
                    waterData.forEach { progress += it.glassVolume }

                    listWater = waterData.map { it.waterItem } as ArrayList<String>

                    adapter = ArrayAdapter(
                        this@WaterActivity,
                        android.R.layout.simple_list_item_1,
                        listWater
                    )
                } else {
                    adapter = ArrayAdapter(
                        this@WaterActivity,
                        android.R.layout.simple_list_item_1,
                        listWater
                    )

                    if (profileData.isNotEmpty()) {
                        waterNorm = profileData.last().weight.roundToInt() * 30
                    }
                }

                bg.editTxtWaterNorm.setText(waterNorm.toString())
                bg.editTxtGlassVolume.setText(glassVolume.toString())
                bg.progressBar.max = waterNorm
                bg.progressBar.progress = progress
                bg.listviewWater.adapter = adapter
                val stringTxt = "$progress / $waterNorm"
                bg.txtProgress.text = stringTxt
            }
        }
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

        loadFromDB()
    }

    private fun updateDateByOffset(offset: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, offset)
        updateCurrentDate(bg.txtDate)
    }
}