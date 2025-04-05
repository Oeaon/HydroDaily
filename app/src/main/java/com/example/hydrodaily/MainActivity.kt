package com.example.hydrodaily

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputBinding
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


import androidx.core.content.ContextCompat
import com.example.hydrodaily.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CHANNEL_NAME = "HidroDaily Channel"
        private const val CHANNEL_ID = "HidroDailyChannelID"
        private const val CHANNEL_DESCRIPTION = "Notifications for HidroDaily"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var weightInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var calculateButton: ImageButton
    private lateinit var resultText: TextView
    private lateinit var btnSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)


        initViews()
        initLoadAds()
        configureSpinner()
        configureNotificationChannel()
        requestNotificationPermissionIfNeeded()
        configureCalculateButton()
    }

    private fun initLoadAds() {
        val adRequest = AdRequest.Builder().build()
        binding.bannerAds.loadAd(adRequest)
    }

    private fun initViews() {
        weightInput = findViewById(R.id.weightInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        activityLevelSpinner = findViewById(R.id.activityLevelSpinner)
        calculateButton = findViewById(R.id.calculateButton)
        resultText = findViewById(R.id.resultText)
        btnSound = MediaPlayer.create(this, R.raw.water_drop_sound_btn)

        val weightImg: ImageView = findViewById(R.id.imageW8)
        val heightImg: ImageView = findViewById(R.id.imageHeight)
        val activityImg: ImageView = findViewById(R.id.imageActivity)

        setImageSizes(calculateButton, weightImg, heightImg, activityImg)
    }

    private fun configureSpinner() {
        val spinnerItems = resources.getStringArray(R.array.activity_levels)
        val spinnerGenderItems = resources.getStringArray(R.array.Gender)
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text_style, spinnerItems)
        val genderAdapter = ArrayAdapter(this, R.layout.spinner_item_text_style, spinnerGenderItems)
        activityLevelSpinner.adapter = adapter
        genderSpinner.adapter = genderAdapter
    }

    private fun configureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermission()
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result


            })
        }
    }

    private fun configureCalculateButton() {
        val toastRetryWeight = Toast.makeText(
            this, getString(R.string.valid_weight), Toast.LENGTH_LONG
        )

        calculateButton.setOnClickListener {
            val weight = weightInput.text.toString().toDoubleOrNull()
            val genderIndex = genderSpinner.selectedItemPosition
            val activityLevelIndex = activityLevelSpinner.selectedItemPosition

            btnSound.start()
            if (weight == null || weight <= 0 || genderIndex== 0|| activityLevelIndex == 0) {
                resultText.visibility = View.INVISIBLE
                toastRetryWeight.show()
            } else {
                val waterIntake = calculateWaterIntake(weight, genderIndex, activityLevelIndex)
                resultText.visibility = View.VISIBLE
                resultText.text = getString(R.string.result_text, waterIntake.toString().take(4))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermission() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.notification_permission_title))
            .setMessage(getString(R.string.notification_permission_message))
            .setPositiveButton(getString(R.string.acept)) { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()


    }

    private fun calculateWaterIntake(weight: Double, gender: Int, activityLevel: Int): Double {
        val baseIntake = (weight * 0.035)
        val genderAdjustment = if (gender == 2) 0.5 else 0.0
        val activityAdjustment = when (activityLevel) {
            2 -> 0.5  // Moderado
            3 -> 1.0  // Alto
            else -> 0.0  // Bajo
        }

        return baseIntake + activityAdjustment + genderAdjustment
    }

    private fun setImageSizes(
        button: ImageButton,
        weightImg: ImageView,
        heightImg: ImageView,
        activityImg: ImageView
    ) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.widthPixels

        button.updateLayoutParams(screenWidth, screenHeight, 0.40)
        weightImg.updateLayoutParams(screenWidth, screenHeight, 0.15)
        heightImg.updateLayoutParams(screenWidth, screenHeight, 0.15)
        activityImg.updateLayoutParams(screenWidth, screenHeight, 0.15)
    }

    private fun ImageView.updateLayoutParams(screenWidth: Int, screenHeight: Int, scale: Double) {
        layoutParams.width = (screenWidth * scale).toInt()
        layoutParams.height = (screenHeight * scale).toInt()
    }
}