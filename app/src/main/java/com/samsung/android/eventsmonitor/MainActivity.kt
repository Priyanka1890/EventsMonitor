/*
 * Copyright 2022 Samsung Electronics Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.android.eventsmonitor

import android.Manifest

import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.samsung.android.eventsmonitor.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

const val TAG = "Events Monitor"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var permissionGranted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGranted =
            applicationContext.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                permissionGranted = result
            }

        HealthServicesManager.getInstance(applicationContext)
            .setHealthEventsObserver(healthEventsObserver)

        if (!getPermissionGranted()) {
            requestPermissions()
            return
        }
    }

    private var healthEventsObserver: HealthEventsObserver = object : HealthEventsObserver {
        override fun onEventDataChanged(eventData: EventData) {
            runOnUiThread {
                binding.textEventTypeValue.text = eventData.eventType
                binding.textEventTimeValue.text = eventData.eventTime
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSubscribeButtonClick(view: View) {
        if (!getPermissionGranted()) {
            Toast.makeText(
                applicationContext,
                R.string.not_all_permissions_granted,
                Toast.LENGTH_LONG
            ).show()
            requestPermissions()
            return
        }

        lifecycleScope.launch {
            if (!HealthServicesManager.getInstance(applicationContext)
                    .hasHealthEventsCapability()
            ) {
                Toast.makeText(
                    applicationContext,
                    R.string.not_all_health_events_available,
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            if (HealthServicesManager.getInstance(applicationContext).isRegistered()) {
                HealthServicesManager.getInstance(applicationContext).unregisterHealthEventsData()
                binding.btnSubscribe.setText(R.string.subscribe_label)
            } else {
                HealthServicesManager.getInstance(applicationContext).registerForHealthEventsData()
                binding.btnSubscribe.setText(R.string.unsubscribe_label)
            }
        }
    }

    private fun getPermissionGranted(): Boolean {
        return permissionGranted
    }

    private fun requestPermissions() {
        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
    }private fun handleFallDetection() {
        // Implement the action to be taken upon fall detection, such as displaying an alert, sending notifications, etc.
        Toast.makeText(this, "Fall detected!", Toast.LENGTH_SHORT).show()

        // Add your fall detection logic here
        // For example, initiate SOS service, emergency call, or send SMS
        initiateEmergencyCall() // Example: Initiating emergency call
        sendEmergencySMS()      // Example: Sending emergency SMS
    }

    private fun initiateEmergencyCall() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:+4915143605020") // Replace  with the actual emergency number
        startActivity(intent)
    }

    private fun sendEmergencySMS() {
        val emergencyNumber = "+4915143605020" // Replace with actual emergency number
        val message = "Emergency: I've fallen and need help!"
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(emergencyNumber, null, message, null, null)
        Log.e("sms", "sent sms!")
    }


}