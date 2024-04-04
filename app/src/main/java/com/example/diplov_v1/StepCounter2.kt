package com.example.diplov_v1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
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

/*
class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            setWorkRequestForNextDay(context)
        }
    }
}
*/

/*
fun setWorkRequestForNextDay(context: Context) {
    val current = Calendar.getInstance()
    val nextDay = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val delay = nextDay.timeInMillis - current.timeInMillis

    val workRequest = PeriodicWorkRequestBuilder<MyWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
    Log.d("MyLog", "WorkerExist")
}

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), SensorEventListener {
    private var stepCountAtStartOfDay: Int = 0
    private var currentSteps: Int = 0
    private var stepsNorm: Int = 5000
    private var date: String = ""

    override fun doWork(): Result {

        val database: Db = Db.getDb(applicationContext)


        stepCountAtStartOfDay = currentSteps

        val data = StepsCounterEntity(
            null,
            date,
            0,
            stepsNorm,
            stepCountAtStartOfDay,
            0
        )
        Thread {
            database.stepsCounterDao().insert(data)
        }.start()

        return Result.success()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        currentSteps = event!!.values[0].toInt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
}
*/

class StepCounter2 : AppCompatActivity(), SensorEventListener {
    private lateinit var bg: StepCounterBinding

    private lateinit var database: Db

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    private var stepCountAtStartOfDay: Int = 0
    //private var stepCountEnd: Int = 0
    private var currentStepCount: Int = 0
    private var stepsNow: Int = 0
    private var stepsNorm: Int = 5000

    private var date: String = ""




    /*
        private val midnightReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                resetPreviousTotalSteps()
            }
        }
    */

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

/*
         updateCurrentDate(bg.txtDate)
        bg.txtDate.setOnClickListener { showDatePicker() }
        bg.btnDateBack.setOnClickListener { updateDateByOffset(-1) }
        bg.btnDateNext.setOnClickListener { updateDateByOffset(1) }
*/

            //setWorkRequestForNextDay(this)

        bg.showlog.setOnClickListener {
            val data = TestEntity(null, "bruh")

            Thread {
                //database.testDao().insert(data)
                Log.d("MyLog", database.testDao().getAllData().toString())
            }.start()
        }

        //setAlarmForNextDay(this)


        //calculate()


        //bg.editTxtStepsNorm.setText(stepsNorm.toString())


        /*
                // Регистрируем BroadcastReceiver
                registerReceiver(midnightReceiver, IntentFilter("MIDNIGHT_BROADCAST"))

                // Создаем AlarmManager
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent("MIDNIGHT_BROADCAST")
                val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                // Устанавливаем время на начало следующего дня
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }

                // Настраиваем AlarmManager на повторение каждый день
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
        */




        bg.btnSave.setOnClickListener { btnSaveClick() }

        bg.btnDeleteSteps.setOnClickListener { btnDeleteClick() }

        bg.btnShowLog.setOnClickListener {
            Thread {
                Log.d("MyLog", database.stepsCounterDao().getStepsData().toString())
            }.start()
        }

        //bg.btnDateBack.setOnClickListener { updateDateByOffset(-1) }
        //bg.btnDateNext.setOnClickListener { updateDateByOffset(1) }

        //getPermissionn()

        //loadFromDb()
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

    private fun getPermissionn() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_BOOT_COMPLETED
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_BOOT_COMPLETED),
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
        //loadFromDb()
    }

    private fun updateStepsNorm() {
        //saveToDb()
                Thread {
                    database.stepsCounterDao().updateStepsNorm(stepsNorm, date, date)
                }.start()
    }

    private fun btnDeleteClick() {
        Thread {
            database.stepsCounterDao().deleteStepsData()
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

        //saveToDb()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            currentStepCount = event.values[0].toInt()

            //stepsToday = currentStepCount - stepCountAtStartOfDay

            //bg.progressBar.progress = stepsToday
            //val stringTxt = "$stepsToday / $stepsNorm"
            //bg.txtProgress.text = stringTxt

        }
/*
        if (stepCountAtStartOfDay == 0) {
            updateCurrentDate(bg.txtDate)
        }
*/
        //updateCurrentDate()
        updateCurrentSteps()
        loadFromDb()
        //calculate()


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

    private fun saveToDb() {
        val data = StepsCounterEntity(
            null,
            date,
            0,
            stepsNorm,
            stepCountAtStartOfDay,
            currentStepCount
        )
        Thread {
            database.stepsCounterDao().insert(data)
        }.start()
    }

    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val stepsData = database.stepsCounterDao().getDayData(date, date)
            withContext(Dispatchers.Main) {
/*
                Log.d(
                    "MyLog",
                    "${stepsData.map { it.stepCountStart }.first()} ${stepsData.map { it.stepCountEnd }.last()} ${stepsData.map { it.stepsNorm }.last()}"
                )
*/
                //stepsNorm = 4393
                //stepsToday = 0
                //stepCountAtStartOfDay = currentStepCount


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




















/*
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
        updateCurrentDate()

        val selectedDate = "$dayOfMonth/${month + 1}/$year"

        //loadFromDb()
    }

    private fun resetPreviousTotalSteps() {
        stepCountAtStartOfDay = currentStepCount
        saveToDb()
    }
*/

/*
    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("EEE, d MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        //textView.text = currentDate

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val time = calendar.time
        date = formatter.format(time)

        //saveToDb()

        lifecycleScope.launch (Dispatchers.IO) {
            val data = database.stepsCounterDao().getDayData(date, date)

            withContext(Dispatchers.Main) {
                if (data.isEmpty()) {
                    stepCountAtStartOfDay = currentStepCount
                    saveToDb()
                    loadFromDb()
                }
            }
        }




        //saveToDb()
        //loadFromDb()
    }

    private fun updateDateByOffset(offset: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, offset)
        updateCurrentDate()
        loadFromDb()
    }
*/
}