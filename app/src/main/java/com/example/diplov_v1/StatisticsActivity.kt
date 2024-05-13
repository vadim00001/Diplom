package com.example.diplov_v1

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.StatisticsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class StatisticsActivity : AppCompatActivity() {
    private lateinit var bg: StatisticsBinding

    private lateinit var database: Db

    private var startDate: String = ""
    private var endDate: String = ""

    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = StatisticsBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_statistics)

        database = Db.getDb(this)

        bg.txtDate.setOnClickListener { showDatePicker() }

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDateCalendar = calendar.time
        endDate = formatter.format(endDateCalendar)

        val startDateCalendar = Calendar.getInstance()
        startDateCalendar.add(Calendar.DAY_OF_YEAR, -7)
        startDate = formatter.format(startDateCalendar.time)

        updatePeriodTextView()
    }

    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val profileData = database.profileDao().getProfileData()
            val nutrData = database.listNutrDao().getAllData()
            val stepsData = database.stepsCounterDao().getStepsData()
            val waterData = database.waterCounterDao().getWaterData()

            val profileDataPeriod = database.profileDao().getDayData(startDate, endDate)
            val nutrDataPeriod = database.listNutrDao().getDayData(startDate, endDate)
            val stepsDataPeriod = database.stepsCounterDao().getDayData(startDate, endDate)
            val waterDataPeriod = database.waterCounterDao().getDayData(startDate, endDate)
            withContext(Dispatchers.Main) {
                bg.txtWeightChangeTotal.text = ""
                bg.txtWeightChange.text = ""
                bg.txtWeightAVG.text = ""
                val chartWeight: LineChart = findViewById(R.id.chartWeight)
                chartWeight.clear()
                if (profileData.isNotEmpty()) {
                    val weightChangeTotal = profileData.last().weight - profileData.first().weight
                    bg.txtWeightChangeTotal.text =
                        "${weightChangeTotal.roundToInt()} кг"
                }

                if (profileDataPeriod.isNotEmpty()) {
                    val weightChangePeriod =
                        profileDataPeriod.last().weight - profileDataPeriod.first().weight
                    val weightList = profileDataPeriod.map { it.weight }
                    val weightAVGPeriod = weightList.average()
                    var dateProfilePeriod = profileDataPeriod.map { it.date }
                    dateProfilePeriod = dateProfilePeriod.map { it.substring(5) }

                    bg.txtWeightChange.text = "${weightChangePeriod.roundToInt()} кг"
                    bg.txtWeightAVG.text = "${weightAVGPeriod.roundToInt()} кг"

                    val valuesWeight = weightList.mapIndexed { index, weight ->
                        Entry(
                            index.toFloat(),
                            weight.toFloat()
                        )
                    }
                    val setWeight = LineDataSet(valuesWeight, "Вес (кг)")
                    setWeight.color = android.graphics.Color.CYAN
                    setWeight.valueTextSize = 10f
                    setWeight.lineWidth = 2f

                    val dataSetsWeight = ArrayList<ILineDataSet>()
                    dataSetsWeight.add(setWeight)
                    val dataWeight = LineData(dataSetsWeight)
                    chartWeight.data = dataWeight
                    chartWeight.xAxis.valueFormatter = IndexAxisValueFormatter(dateProfilePeriod)
                    chartWeight.description.text = "Изменение веса"
                }

                bg.txtCPFCAll.text = ""
                if (nutrData.isNotEmpty()) {
                    val kcalTotalList: List<Int> = nutrData.map { it.kcal.roundToInt() }
                    val proteinTotalList: List<Int> = nutrData.map { it.protein.roundToInt() }
                    val fatsTotalList: List<Int> = nutrData.map { it.fats.roundToInt() }
                    val carbTotalList: List<Int> = nutrData.map { it.carb.roundToInt() }

                    val kcalTotalSum = kcalTotalList.sum()
                    val proteinTotalSum = proteinTotalList.sum()
                    val fatsTotalSum = fatsTotalList.sum()
                    val carbTotalSum = carbTotalList.sum()

                    bg.txtCPFCAll.text =
                        "$kcalTotalSum/$proteinTotalSum/$fatsTotalSum/$carbTotalSum"
                }

                bg.txtCPFCPeriod.text = ""
                bg.txtCPFCAverage.text = ""
                val chartKcal: LineChart = findViewById(R.id.chartKcal)
                chartKcal.clear()
                val chartPFC: LineChart = findViewById(R.id.chartPFC)
                chartPFC.clear()

                if (nutrDataPeriod.isNotEmpty()) {
                    val kcalPeriodList: List<Int> = nutrDataPeriod.map { it.kcal.roundToInt() }
                    val proteinPeriodList: List<Int> =
                        nutrDataPeriod.map { it.protein.roundToInt() }
                    val fatsPeriodList: List<Int> = nutrDataPeriod.map { it.fats.roundToInt() }
                    val carbPeriodList: List<Int> = nutrDataPeriod.map { it.carb.roundToInt() }

                    val kcalPeriodSum = kcalPeriodList.sum()
                    val proteinPeriodSum = proteinPeriodList.sum()
                    val fatsPeriodSum = fatsPeriodList.sum()
                    val carbPeriodSum = carbPeriodList.sum()

                    bg.txtCPFCPeriod.text =
                        "$kcalPeriodSum/$proteinPeriodSum/$fatsPeriodSum/$carbPeriodSum"


                    val nutrDataByDate = nutrDataPeriod.groupBy { it.date }

                    val kcalSumByDate =
                        nutrDataByDate.mapValues { (_, values) -> values.sumOf { it.kcal } }
                    val proteinSumByDate =
                        nutrDataByDate.mapValues { (_, values) -> values.sumOf { it.protein } }
                    val fatsSumByDate =
                        nutrDataByDate.mapValues { (_, values) -> values.sumOf { it.fats } }
                    val carbSumByDate =
                        nutrDataByDate.mapValues { (_, values) -> values.sumOf { it.carb } }

                    var dateNutrPeriod = nutrDataByDate.keys.toList()
                    dateNutrPeriod = dateNutrPeriod.map { it.substring(5) }

                    val kcalDataList = kcalSumByDate.values.toList()
                    val proteinDataList = proteinSumByDate.values.toList()
                    val fatsDataList = fatsSumByDate.values.toList()
                    val carbDataList = carbSumByDate.values.toList()

                    val kcalPeriodAVG = kcalPeriodSum / dateNutrPeriod.count()
                    val proteinPeriodAVG = proteinPeriodSum / dateNutrPeriod.count()
                    val fatsPeriodAVG = fatsPeriodSum / dateNutrPeriod.count()
                    val carbPeriodAVG = carbPeriodSum / dateNutrPeriod.count()

                    bg.txtCPFCAverage.text =
                        "${kcalPeriodAVG}/${proteinPeriodAVG}/${fatsPeriodAVG}/${carbPeriodAVG}"

                    val valuesKcal = kcalDataList.mapIndexed { index, kcal ->
                        Entry(
                            index.toFloat(),
                            kcal.toFloat()
                        )
                    }
                    val setKcal = LineDataSet(valuesKcal, "Калории")
                    setKcal.color = android.graphics.Color.CYAN
                    setKcal.valueTextSize = 10f
                    setKcal.lineWidth = 2f

                    val dataSetsKcal = ArrayList<ILineDataSet>()
                    dataSetsKcal.add(setKcal)
                    val dataKcal = LineData(dataSetsKcal)
                    chartKcal.data = dataKcal
                    chartKcal.xAxis.valueFormatter = IndexAxisValueFormatter(dateNutrPeriod)
                    chartKcal.description.text = "Потребление калорий"

                    val valuesProtein = proteinDataList.mapIndexed { index, protein ->
                        Entry(
                            index.toFloat(),
                            protein.toFloat()
                        )
                    }
                    val valuesFats = fatsDataList.mapIndexed { index, fats ->
                        Entry(
                            index.toFloat(),
                            fats.toFloat()
                        )
                    }
                    val valuesCarb = carbDataList.mapIndexed { index, carb ->
                        Entry(
                            index.toFloat(),
                            carb.toFloat()
                        )
                    }
                    val setProtein = LineDataSet(valuesProtein, "Белки")
                    setProtein.color = android.graphics.Color.CYAN
                    setProtein.valueTextSize = 10f
                    setProtein.lineWidth = 2f

                    val setFats = LineDataSet(valuesFats, "Жиры")
                    setFats.color = android.graphics.Color.RED
                    setFats.valueTextSize = 10f
                    setFats.lineWidth = 2f

                    val setCarb = LineDataSet(valuesCarb, "Углеводы")
                    setCarb.color = android.graphics.Color.GREEN
                    setCarb.valueTextSize = 10f
                    setCarb.lineWidth = 2f

                    val dataSetsPFC = ArrayList<ILineDataSet>()
                    dataSetsPFC.add(setProtein)
                    dataSetsPFC.add(setFats)
                    dataSetsPFC.add(setCarb)
                    val dataPFC = LineData(dataSetsPFC)

                    chartPFC.data = dataPFC
                    chartPFC.xAxis.valueFormatter = IndexAxisValueFormatter(dateNutrPeriod)
                    chartPFC.description.text = "БЖУ (г)"
                }

                bg.txtStepsTotal.text = ""
                bg.txtStepsPeriod.text = ""
                val chartSteps: LineChart = findViewById(R.id.chartSteps)
                chartSteps.clear()

                if (stepsData.isNotEmpty()) {
                    var stepsCountTotal = 0
                    for (data in stepsData) {
                        val stepsCountEnd = data.stepCountEnd
                        val stepsCountStart = data.stepCountStart
                        stepsCountTotal += stepsCountEnd - stepsCountStart
                    }
                    bg.txtStepsTotal.text = stepsCountTotal.toString()
                }

                if (stepsDataPeriod.isNotEmpty()) {
                    var stepsCountPeriodTotal = 0
                    for (data in stepsDataPeriod) {
                        val stepsCountEnd = data.stepCountEnd
                        val stepsCountStart = data.stepCountStart
                        stepsCountPeriodTotal += stepsCountEnd - stepsCountStart
                    }
                    bg.txtStepsPeriod.text = stepsCountPeriodTotal.toString()

                    val valuesSteps = stepsDataPeriod.mapIndexed {index, steps ->
                        Entry(
                            index.toFloat(),
                            (steps.stepCountEnd - steps.stepCountStart).toFloat()
                        )
                    }

                    val dates = stepsDataPeriod.map { it.date.substring(5) }
                    chartSteps.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return dates[value.toInt()]
                        }
                    }

                    val setSteps = LineDataSet(valuesSteps, "Шаги")
                    setSteps.color = android.graphics.Color.CYAN
                    setSteps.valueTextSize = 10f
                    setSteps.lineWidth = 2f

                    val dataSetsSteps = ArrayList<ILineDataSet>()
                    dataSetsSteps.add(setSteps)
                    val dataSteps = LineData(dataSetsSteps)
                    chartSteps.data = dataSteps
                    chartSteps.description.text = "Количество шагов"
                }

                bg.txtWaterTotal.text = ""
                bg.txtWaterPeriod.text = ""
                val chartWater: LineChart = findViewById(R.id.chartWater)
                chartWater.clear()

                if (waterData.isNotEmpty()) {
                    val waterDataList = waterData.map { it.glassVolume }
                    val waterTotal = waterDataList.sum()
                    bg.txtWaterTotal.text = "$waterTotal мл"
                }

                if (waterDataPeriod.isNotEmpty()) {
                    val waterDataListt = waterDataPeriod.map { it.glassVolume }
                    val waterPeriod = waterDataListt.sum()
                    bg.txtWaterPeriod.text = "$waterPeriod мл"

                    val waterDataByDate = waterDataPeriod.groupBy { it.date }
                    val waterSumByDate =
                        waterDataByDate.mapValues { (_, values) -> values.sumOf { it.glassVolume } }

                    var dateWaterPeriod = waterSumByDate.keys.toList()
                    dateWaterPeriod = dateWaterPeriod.map { it.substring(5) }
                    val waterDataList = waterSumByDate.values.toList()

                    val valuesWater = waterDataList.mapIndexed { index, water ->
                        Entry(
                            index.toFloat(),
                            water.toFloat()
                        )
                    }
                    val setWater = LineDataSet(valuesWater, "Вода (мл)")
                    setWater.color = android.graphics.Color.CYAN
                    setWater.valueTextSize = 10f
                    setWater.lineWidth = 2f

                    val dataSetsWater = ArrayList<ILineDataSet>()
                    dataSetsWater.add(setWater)
                    val dataWater = LineData(dataSetsWater)
                    chartWater.data = dataWater
                    chartWater.xAxis.valueFormatter = IndexAxisValueFormatter(dateWaterPeriod)
                    chartWater.description.text = "Потребление воды"
                }
            }
        }
    }

    private fun showDatePicker() {
        showStartDatePicker()
    }

    private fun showStartDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                handleSelectedDate(selectedYear, selectedMonth, selectedDayOfMonth, true)
                showEndDatePicker()
            },
            year,
            month,
            dayOfMonth
        ).show()
    }

    private fun showEndDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                handleSelectedDate(selectedYear, selectedMonth, selectedDayOfMonth, false)
                updatePeriodTextView()
            },
            year,
            month,
            dayOfMonth
        ).show()
    }

    private fun handleSelectedDate(year: Int, month: Int, dayOfMonth: Int, isStartDate: Boolean) {
        calendar.set(year, month, dayOfMonth)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = formatter.format(calendar.time)
        if (isStartDate) {
            startDate = selectedDate
        } else {
            endDate = selectedDate
        }
    }

    private fun updatePeriodTextView() {
        bg.txtDate.text = "$startDate / $endDate"
        loadFromDb()
    }
}