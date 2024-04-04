package com.example.diplov_v1

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.StepCounterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class StepCounterActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var bg: StepCounterBinding
    private lateinit var database: Db

    private var sensorManager: SensorManager? = null
    private var totalSteps = 0
    private var previousTotalSteps = 0
    private var offsetSteps = 0
    private var currentSteps = 0
    private var stepsNorm = 6000
    private var dist = 0
    private var kcal = 0
    private var time = 0
    private var count = 0
    private lateinit var stepsData: List<StepsCounterEntity>
    private val calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = StepCounterBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.name_app)

        resetSteps()

        updateCurrentDate(bg.txtDate)
        bg.txtDate.setOnClickListener { showDatePicker() }
        bg.btnDateBack.setOnClickListener { updateDateByOffset(-1) }
        bg.btnDateNext.setOnClickListener { updateDateByOffset(1) }

        database = Db.getDb(this)

        //loadFromDb()

        bg.editTxtStepsNorm.setText(stepsNorm.toString())
        bg.txtDist.text = "$dist м"
        bg.txtCal.text = "$kcal ккал"

        bg.btnSave.setOnClickListener {
            if (bg.editTxtStepsNorm.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.ErrorSteps), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            stepsNorm = bg.editTxtStepsNorm.text.toString().toInt()
            bg.progressBar.max = stepsNorm
            bg.txtProgress.text = "$currentSteps / $stepsNorm"
            //saveToDb()
        }

        bg.btnDeleteSteps.setOnClickListener {
            Thread {
                database.stepsCounterDao().deleteStepsData()
            }.start()
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                0
            )
        }
    }

    private fun calculate() {
        dist = (currentSteps * 0.72).roundToInt()
        kcal = (currentSteps * 0.04).roundToInt()
        bg.txtDist.text = "$dist м"
        bg.txtCal.text = "$kcal ккал"
    }

/*
    private fun saveToDb() {
        val data = StepsCounterEntity(
            null,
            0,
            stepsNorm,
            currentSteps
            //dist,
            //kcal
            //time,
            //count
        )
        Thread {
            database.stepsCounterDao().insert(data)
        }.start()
    }
*/

    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            //stepsData = database.stepsCounterDao().getStepsData()
            withContext(Dispatchers.Main) {
                if (!stepsData.isNullOrEmpty()) {
                    val lastStepsData = stepsData.last()
                    stepsNorm = lastStepsData.stepsNorm
                    //currentSteps = lastStepsData.progress
                    //dist = lastStepsData.dist
                    //kcal = lastStepsData.kcal
                    //time = lastStepsData.time
                    //count = lastStepsData.count
                    bg.editTxtStepsNorm.setText(stepsNorm.toString())
                    bg.progressBar.max = stepsNorm
                    bg.progressBar.progress = currentSteps
                    bg.txtProgress.text = "$currentSteps / $stepsNorm"
                    bg.txtDist.text = "$dist м"
                    bg.txtCal.text = "$kcal ккал"
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        /*
                var stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                if (stepSensor == null) {
                    stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                }
                if (stepSensor != null) {
                    sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
                }
        */
    }

    override fun onPause() {
        super.onPause()
        //saveToDb()
    }

    override fun onDestroy() {
        super.onDestroy()
        //saveToDb()
    }

    private fun resetSteps() {
        bg.txtProgress.setOnClickListener {
            currentSteps = 0
            bg.progressBar.progress = currentSteps
            bg.txtProgress.text = "$currentSteps / $stepsNorm"
        }
    }



    /*
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Не используется в этом примере
        }
    */

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

        val selectedDate = "$dayOfMonth/${month + 1}/$year"

        //resetPreviousTotalSteps()
    }

    private fun updateCurrentDate(textView: TextView) {
        val dateFormat = SimpleDateFormat("EEE, d MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        textView.text = currentDate
    }

    private fun updateDateByOffset(offset: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, offset)
        updateCurrentDate(bg.txtDate)
    }

}
