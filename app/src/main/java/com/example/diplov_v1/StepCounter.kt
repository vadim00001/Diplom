package com.example.diplov_v1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
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

class StepCounter : AppCompatActivity(), SensorEventListener {
    private lateinit var bg: StepCounterBinding

    private lateinit var database: Db

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    private var stepCountAtStartOfDay: Int = 0
    private var currentStepCount: Int = 0
    private var stepsNow: Int = 0
    private var stepsNorm: Int = 5000

    private var date: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = StepCounterBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_step_counter2)

        database = Db.getDb(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        getPermission()

        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val time = calendar.time
        date = formatter.format(time)

        bg.btnSave.setOnClickListener { btnSaveClick() }
    }

    private fun getPermission() {
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

    private fun btnSaveClick() {
        if (bg.editTxtStepsNorm.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.ErrorSteps), Toast.LENGTH_SHORT).show()
            return
        }
        stepsNorm = bg.editTxtStepsNorm.text.toString().toInt()

        bg.progressBar.max = stepsNorm
        val stringTxt = "$stepsNow / $stepsNorm"
        bg.txtProgress.text = stringTxt

        updateStepsNorm()
    }

    private fun updateStepsNorm() {
        Thread {
            database.stepsCounterDao().updateStepsNorm(stepsNorm, date, date)
        }.start()
    }

    override fun onResume() {
        super.onResume()
        stepCounterSensor?.also { stepCounter ->
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            currentStepCount = event.values[0].toInt()
        }
        updateCurrentSteps()
        loadFromDb()
    }

    private fun updateCurrentSteps() {
        Thread {
            database.stepsCounterDao().updateCurrentSteps(currentStepCount, date, date)
        }.start()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun calculate() {
        val dist = (stepsNow * 0.72).roundToInt()
        val kcal = (stepsNow * 0.04).roundToInt()
        bg.txtDist.text = "$dist м"
        bg.txtCal.text = "$kcal ккал"
    }

    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val stepsData = database.stepsCounterDao().getDayData(date, date)
            withContext(Dispatchers.Main) {
                if (stepsData.isNotEmpty()) {
                    stepsNorm = stepsData.last().stepsNorm
                    stepCountAtStartOfDay = stepsData.first().stepCountStart
                }
                stepsNow = currentStepCount - stepCountAtStartOfDay

                calculate()
                bg.editTxtStepsNorm.setText(stepsNorm.toString())
                bg.progressBar.max = stepsNorm
                bg.progressBar.progress = stepsNow
                val stringTxt = "$stepsNow / $stepsNorm"
                bg.txtProgress.text = stringTxt
            }
        }
    }
}