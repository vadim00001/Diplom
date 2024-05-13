package com.example.diplov_v1

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.example.diplov_v1.databinding.HealthIndicatorsAddBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log
import kotlin.math.roundToInt

class HealthIndicatorsAdd : AppCompatActivity() {
    private lateinit var bg: HealthIndicatorsAddBinding
    private lateinit var database: Db

    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = HealthIndicatorsAddBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_health_fat_procent)

        database = Db.getDb(this)

        addRadioButtons()

        val calendar: Calendar = Calendar.getInstance()
        val formatterDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeCalendar = calendar.time
        val date = formatterDate.format(timeCalendar)

        val formatterTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = formatterTime.format(timeCalendar)

        val key = intent.getStringExtra("key")
        val indicator = intent.getStringExtra("indicator")!!

        var item: String

        when (key) {
            "fatPercent" -> {
                val gender = intent.getIntExtra("gender", 0)
                var height = intent.getDoubleExtra("height", 0.0)
                bg.editTxt1.setText(height.toString())
                var res = 0.0

                bg.txtTitle.text = getString(R.string.FatProcentCalculate)
                bg.txt1.text = getString(R.string.Height)
                bg.txt2.text = getString(R.string.Neck)
                bg.txt3.text = getString(R.string.Waist)
                bg.txt4.text = getString(R.string.Hips)
                bg.txt5.text = getString(R.string.forWomen)
                bg.radioGroup.isGone = true

                bg.btnDoneHealth.setOnClickListener {
                    if (bg.editTxt1.text.isNullOrEmpty() || bg.editTxt2.text.isNullOrEmpty() || bg.editTxt3.text.isNullOrEmpty()) {
                        Toast.makeText(this, R.string.ErrorDataHealth, Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    height = bg.editTxt1.text.toString().toDouble()
                    val neck = bg.editTxt2.text.toString().toDouble()
                    val waist = bg.editTxt3.text.toString().toDouble()

                    if (gender == 0) {
                        res = 495 / (1.0324 - 0.19077 * (log(waist - neck, 10.0)) + 0.15456 * (log(
                            height,
                            10.0
                        ))) - 450
                    }

                    if (gender == 1) {
                        if (bg.editTxt4.text.isNullOrEmpty()) {
                            Toast.makeText(this, R.string.ErrorDataHealth, Toast.LENGTH_SHORT)
                                .show()
                            return@setOnClickListener
                        }

                        val hips = bg.editTxt4.text.toString().toDouble()

                        res = 495 / (1.29579 - 0.35004 * (log(
                            waist + hips - neck,
                            10.0
                        )) + 0.22100 * (log(height, 10.0))) - 450
                    }

                    item = "${res.roundToInt()}%"

                    val data = HealthIndicatorsEntity(
                        null,
                        0,
                        date,
                        time,
                        indicator,
                        item
                    )

                    Thread {
                        database.healthIndicators().insert(data)
                    }.start()

                    finish()
                }
            }

            "glucose" -> {
                bg.txtTitle.text = getString(R.string.Glucose)
                bg.txt1.text = getString(R.string.GlucoseValue)
                bg.txt2.isGone = true
                bg.txt3.isGone = true
                bg.txt4.isGone = true
                bg.txt5.isGone = true
                bg.editTxt2.isGone = true
                bg.editTxt3.isGone = true
                bg.editTxt4.isGone = true

                bg.btnDoneHealth.setOnClickListener {
                    if (bg.editTxt1.text.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            getString(R.string.ErrorDataHealth),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnClickListener
                    }

                    val selectedRadioButtonId: Int = bg.radioGroup.checkedRadioButtonId
                    val selectedRadioButton: RadioButton = findViewById(selectedRadioButtonId)

                    item = "${bg.editTxt1.text} мг/дл\t\t\t\t${selectedRadioButton.text}"

                    val data = HealthIndicatorsEntity(
                        null,
                        0,
                        date,
                        time,
                        indicator,
                        item
                    )

                    Thread {
                        database.healthIndicators().insert(data)
                    }.start()

                    finish()
                }
            }

            "pressure" -> {
                bg.txtTitle.text = getString(R.string.Pressure)
                bg.txt1.text = getString(R.string.PressureSist)
                bg.txt2.text = getString(R.string.PressureDiast)
                bg.txt3.text = getString(R.string.Pulse)
                bg.txt4.isGone = true
                bg.txt5.isGone = true
                bg.editTxt4.isGone = true
                bg.radioGroup.isGone = true

                bg.btnDoneHealth.setOnClickListener {
                    if (bg.editTxt1.text.isNullOrEmpty() || bg.editTxt2.text.isNullOrEmpty() || bg.editTxt3.text.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            getString(R.string.ErrorDataHealth),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnClickListener
                    }

                    item = "${bg.editTxt1.text} / ${bg.editTxt2.text} (${bg.editTxt3.text})"

                    val data = HealthIndicatorsEntity(
                        null,
                        0,
                        date,
                        time,
                        indicator,
                        item
                    )

                    Thread {
                        database.healthIndicators().insert(data)
                    }.start()

                    finish()
                }
            }
        }
    }

    private fun addRadioButtons() {
        radioButton1 = RadioButton(this)
        radioButton1.text = getString(R.string.GlucoseBeforeFood)
        radioButton1.id = View.generateViewId()
        bg.radioGroup.addView(radioButton1)

        radioButton2 = RadioButton(this)
        radioButton2.text = getString(R.string.GlucoseAfterFood)
        radioButton2.id = View.generateViewId()
        bg.radioGroup.addView(radioButton2)

        radioButton3 = RadioButton(this)
        radioButton3.text = getString(R.string.NightGlucose)
        radioButton3.id = View.generateViewId()
        bg.radioGroup.addView(radioButton3)

        radioButton1.isChecked = true
    }
}