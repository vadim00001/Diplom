package com.example.diplov_v1

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.HealthIndicatorsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HealthIndicators : AppCompatActivity() {
    private lateinit var bg: HealthIndicatorsBinding
    private lateinit var database: Db

    private var date = ""
    private var height: Double = 0.0
    private var gender: Int = 0

    private var indicator: String = ""

    private var listItems: ArrayList<String> = ArrayList()
    private lateinit var adapterListView: ArrayAdapter<String>

    private var launcher: ActivityResultLauncher<Intent>? = null

    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = HealthIndicatorsBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_health_indicators)

        database = Db.getDb(this)

        updateCurrentDate(bg.txtDate)
        bg.txtDate.setOnClickListener { showDatePicker() }
        bg.btnDateBack.setOnClickListener { updateDateByOffset(-1) }
        bg.btnDateNext.setOnClickListener { updateDateByOffset(1) }

        val array = resources.getStringArray(R.array.HealthIndicators)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bg.spinnerHealth.adapter = adapter

        bg.floatingActionButton2.setOnClickListener {
            indicator = bg.spinnerHealth.selectedItem.toString()
            val intent = Intent(this, HealthIndicatorsAdd::class.java)
            intent.putExtra("indicator", indicator)
            when (bg.spinnerHealth.selectedItemId) {
                0L -> {
                    intent.putExtra("key", "fatPercent")
                    intent.putExtra("gender", gender)
                    intent.putExtra("height", height)
                }

                1L -> {
                    intent.putExtra("key", "glucose")
                }

                else -> {
                    intent.putExtra("key", "pressure")
                }
            }
            launcher?.launch(intent)
        }

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    loadFromDb()
                } else {
                    loadFromDb()
                }
            }

        bg.spinnerHealth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                indicator = parent.getItemAtPosition(position).toString()
                loadFromDb()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        bg.listViewHealth.setOnItemLongClickListener { _, _, position, _ ->
            val itemListView = bg.listViewHealth.getItemAtPosition(position) as String
            val itemInDb = itemListView.substring(9)

            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            bottomSheetDialog.setContentView(view)

            val btnOK = view.findViewById<Button>(R.id.btnOK)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)

            btnOK.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    database.healthIndicators().deleteItem(itemInDb, date, date)
                    withContext(Dispatchers.Main) {
                        loadFromDb()
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

        loadFromDb()
    }

    private fun loadFromDb() {
        listItems.clear()
        adapterListView = ArrayAdapter(
            this@HealthIndicators,
            android.R.layout.simple_list_item_1,
            listItems
        )
        bg.listViewHealth.adapter = adapterListView

        lifecycleScope.launch(Dispatchers.IO) {
            val profileData = database.profileDao().getProfileData()
            val healthData = database.healthIndicators().getDayData(indicator, date, date)
            withContext(Dispatchers.Main) {
                if (profileData.isNotEmpty()) {
                    height = profileData.last().height
                    gender = profileData.last().gender
                }

                val chart: LineChart = findViewById(R.id.chartHealth)
                chart.clear()

                if (healthData.isNotEmpty()) {
                    healthData.forEach { listItems.add("${it.time}\t\t\t\t${it.items}") }
                    adapterListView = ArrayAdapter(
                        this@HealthIndicators,
                        android.R.layout.simple_list_item_1,
                        listItems
                    )
                    bg.listViewHealth.adapter = adapterListView

                    var itemsList = healthData.map { it.items }

                    if (indicator == getString(R.string.Pressure)) {
                        val splitItems = itemsList.map { splitItem(it) }
                        val listByIndex = splitItems[0].indices.map { index ->
                            splitItems.map { it[index] }
                        }
                        val sistList = listByIndex[0]
                        val diastList = listByIndex[1]
                        val pulseList = listByIndex[2]

                        val valuesSist = sistList.mapIndexed { index, value ->
                            Entry(
                                index.toFloat(),
                                value.toFloat()
                            )
                        }
                        val setValueSist = LineDataSet(valuesSist, "Систолическое")
                        setValueSist.color = android.graphics.Color.RED
                        setValueSist.valueTextSize = 10f
                        setValueSist.lineWidth = 2f

                        val valuesDiast = diastList.mapIndexed { index, value ->
                            Entry(
                                index.toFloat(),
                                value.toFloat()
                            )
                        }
                        val setValueDiast = LineDataSet(valuesDiast, "Диастолическое")
                        setValueDiast.color = android.graphics.Color.GREEN
                        setValueDiast.valueTextSize = 10f
                        setValueDiast.lineWidth = 2f

                        val valuesPulse = pulseList.mapIndexed { index, value ->
                            Entry(
                                index.toFloat(),
                                value.toFloat()
                            )
                        }
                        val setValuesPulse = LineDataSet(valuesPulse, "Пульс")
                        setValuesPulse.color = android.graphics.Color.CYAN
                        setValuesPulse.valueTextSize = 10f
                        setValuesPulse.lineWidth = 2f

                        val dataSets = ArrayList<ILineDataSet>()
                        dataSets.add(setValueSist)
                        dataSets.add(setValueDiast)
                        dataSets.add(setValuesPulse)
                        val data = LineData(dataSets)
                        chart.data = data
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter()
                        chart.description.text = "Артериальное давление"
                    } else {
                        itemsList = itemsList.map { it.replace(Regex("[^\\d.]"), "") }
                        val values = itemsList.mapIndexed { index, value ->
                            Entry(
                                index.toFloat(),
                                value.toFloat()
                            )
                        }
                        val setValue = LineDataSet(values, "Данные")
                        setValue.color = android.graphics.Color.CYAN
                        setValue.valueTextSize = 10f
                        setValue.lineWidth = 2f

                        val dataSets = ArrayList<ILineDataSet>()
                        dataSets.add(setValue)
                        val data = LineData(dataSets)
                        chart.data = data
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter()
                        chart.description.text = ""
                    }
                }
            }
        }
    }

    private fun splitItem(item: String): List<String> {
        val parts = item.split(" / ", " (")
        return listOf(parts[0], parts[1], parts[2].removeSuffix(")"))
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