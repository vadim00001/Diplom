package com.example.diplov_v1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diplov_v1.databinding.ProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class ProfileActivity : AppCompatActivity() {
    private lateinit var bg: ProfileBinding

    private val calendar = Calendar.getInstance()

    private lateinit var database: Db

    private var date: String = ""
    private var name: String = ""
    private var gender: Int = 0
    private var height: Double = 0.0
    private var weight: Double = 0.0
    private var age: Double = 0.0
    private var activityLvlId: Int = 0
    private var kcal: Double = 0.0
    private var protein: Double = 0.0
    private var fats: Double = 0.0
    private var carb: Double = 0.0

    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton

    private var launcherHelp: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = ProfileBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_profile)

        database = Db.getDb(this)

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val time = calendar.time
        date = formatter.format(time)

        setupSpinner(bg.spinActivLvl, R.array.ActiveLvl)
        setupSpinner(bg.spinFormula, R.array.Formula)
        setupSpinner(bg.spinGoal, R.array.Goal)

        bg.btnRaschet.setOnClickListener { calculate() }
        bg.btnSaveProfile.setOnClickListener { saveToDb() }

        addRadioButtons()

        bg.imageButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            intent.putExtra("fromKey", "profile")
            launcherHelp?.launch(intent)
        }

        launcherHelp =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                }
            }

        loadFromDb()
    }

    private fun addRadioButtons() {
        radioButton1 = RadioButton(this)
        radioButton1.text = getString(R.string.Male)
        radioButton1.id = View.generateViewId()
        bg.radioGender.addView(radioButton1)

        radioButton2 = RadioButton(this)
        radioButton2.text = getString(R.string.Female)
        radioButton2.id = View.generateViewId()
        bg.radioGender.addView(radioButton2)

        radioButton1.isChecked = true
    }


    private fun loadFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val profileData = database.profileDao().getProfileData()
            withContext(Dispatchers.Main) {
                if (profileData.isNotEmpty()) {
                    bg.editTxtName.setText(profileData.last().name)
                    if (profileData.last().gender == 1) radioButton2.isChecked = true
                    bg.editTxtHeight.setText(profileData.last().height.toString())
                    bg.editTxtWeight.setText(profileData.last().weight.toString())
                    bg.editTxtAge.setText(profileData.last().age.toString())
                    bg.spinActivLvl.setSelection(profileData.last().activityLvlId)
                    kcal = profileData.last().kcal
                    bg.txtCaloriesResult.text = kcal.roundToInt().toString()
                    protein = profileData.last().protein
                    bg.txtProteinResult.text = protein.roundToInt().toString()
                    fats = profileData.last().fats
                    bg.txtFatResult.text = fats.roundToInt().toString()
                    carb = profileData.last().carb
                    bg.txtCarbResult.text = carb.roundToInt().toString()
                }
            }
        }
    }

    private fun saveToDb() {
        if (bg.editTxtName.text.isNullOrEmpty() || bg.editTxtHeight.text.isNullOrEmpty() || bg.editTxtWeight.text.isNullOrEmpty() || bg.editTxtAge.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.Error_data), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButtonId: Int = bg.radioGender.checkedRadioButtonId
        val selectedRadioButton: RadioButton = findViewById(selectedRadioButtonId)
        if (selectedRadioButton.text == getString(R.string.Female)) gender = 1

        name = bg.editTxtName.text.toString()
        height = bg.editTxtHeight.text.toString().toDouble()
        weight = bg.editTxtWeight.text.toString().toDouble()
        age = bg.editTxtAge.text.toString().toDouble()
        activityLvlId = bg.spinActivLvl.selectedItemId.toInt()

        val profileData = ProfileEntity(
            null,
            date,
            name,
            gender,
            height,
            weight,
            age,
            activityLvlId,
            kcal,
            protein,
            fats,
            carb,
        )

        Thread {
            database.profileDao().insert(profileData)
        }.start()
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int) {
        val array = resources.getStringArray(arrayResId)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    private fun calculate() {
        if (bg.editTxtName.text.isNullOrEmpty() || bg.editTxtHeight.text.isNullOrEmpty() || bg.editTxtWeight.text.isNullOrEmpty() || bg.editTxtAge.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.Error_data), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButtonId: Int = bg.radioGender.checkedRadioButtonId
        val selectedRadioButton: RadioButton = findViewById(selectedRadioButtonId)
        if (selectedRadioButton.text == getString(R.string.Female)) gender = 1

        name = bg.editTxtName.text.toString()
        height = bg.editTxtHeight.text.toString().toDouble()
        weight = bg.editTxtWeight.text.toString().toDouble()
        age = bg.editTxtAge.text.toString().toDouble()
        activityLvlId = bg.spinActivLvl.selectedItemId.toInt()

        val activityLvlValue: Double = when (activityLvlId) {
            0 -> 1.0
            1 -> 1.2
            2 -> 1.375
            3 -> 1.46
            4 -> 1.55
            5 -> 1.64
            6 -> 1.72
            7 -> 1.9
            else -> -1.0
        }

        val formula = bg.spinFormula.selectedItemId

        val percentOfFat = bg.editTxtFatPercent.text

        val goal = bg.spinGoal.selectedItemId

        kcal = when (formula) {
            0L -> {
                if (gender == 0) {
                    (9.99 * weight) + (6.25 * height) - (4.92 * age) + 5
                } else {
                    ((9.99 * weight) + (6.25 * height) - (4.92 * age) - 161)
                }
            }

            1L -> {
                if (gender == 0) {
                    (88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age))
                } else {
                    (447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age))
                }
            }

            2L -> {
                if (percentOfFat.isNullOrEmpty()) {
                    Toast.makeText(
                        this, getString(R.string.Error_FatPercent), Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                (21.6 * (weight * (100 - percentOfFat.toString().toDouble()) / 100) + 370)
            }

            else -> 0.0
        }

        kcal *= activityLvlValue

        if (bg.editTxtProtein.text.isNullOrEmpty() || bg.editTxtFat.text.isNullOrEmpty() || bg.editTxtCarb.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.Error_BGU_Empty), Toast.LENGTH_SHORT).show()
            return
        }

        val proteinEdit = bg.editTxtProtein.text.toString().toInt()
        val fatsEdit = bg.editTxtFat.text.toString().toInt()
        val carbEdit = bg.editTxtCarb.text.toString().toInt()

        if (proteinEdit + fatsEdit + carbEdit != 100) {
            Toast.makeText(this, getString(R.string.Error_BGU), Toast.LENGTH_SHORT).show()
            bg.txtProteinResult.text = null
            bg.txtFatResult.text = null
            bg.txtCarbResult.text = null
            return
        }

        if (goal == 1L || goal == 2L) {
            val percent = bg.editTxtPercent.text?.toString()?.toIntOrNull()

            if (percent != null) {
                if (goal == 1L) {
                    kcal -= kcal * percent / 100.0
                } else {
                    kcal += kcal * percent / 100.0
                }
            } else {
                kcal *= if (goal == 1L) {
                    0.9
                } else {
                    1.1
                }
            }
        }

        bg.txtCaloriesResult.text = kcal.roundToInt().toString()

        protein = (kcal * proteinEdit / 100) / 4
        bg.txtProteinResult.text = protein.roundToInt().toString()

        fats = (kcal * fatsEdit / 100) / 9
        bg.txtFatResult.text = fats.roundToInt().toString()

        carb = (kcal * carbEdit / 100) / 4
        bg.txtCarbResult.text = carb.roundToInt().toString()

        saveToDb()
    }
}