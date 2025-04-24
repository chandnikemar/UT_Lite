package com.example.utlite.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.utlite.R
import com.example.utlite.databinding.ActivityAdminBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Utils

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        if (!Utils.getSharedPrefs(this@AdminActivity, Constants.KEY_HTTP).isNullOrEmpty()) {
            binding.edHttp.setText(Utils.getSharedPrefs(this@AdminActivity, Constants.KEY_HTTP))
        }
        if (!Utils.getSharedPrefs(this@AdminActivity, Constants.KEY_SERVER_IP).isNullOrEmpty()) {
            binding.edServerIp.setText(
                Utils.getSharedPrefs(
                    this@AdminActivity,
                    Constants.KEY_SERVER_IP
                )
            )
        }
        if (!Utils.getSharedPrefs(this@AdminActivity, Constants.KEY_ANTENNA_POWER)
                .isNullOrEmpty()
        ) {
            binding.tvantenna.setText(
                Utils.getSharedPrefs(
                    this@AdminActivity,
                    Constants.KEY_ANTENNA_POWER
                )
            )
        }
        binding.btSubmit.setOnClickListener { checkInputUrl() }
        binding.btAntenna.setOnClickListener { checkInputAntenna() }

    }


    private fun checkInputAntenna() {
        val antenna = binding.textinputantenna.editText?.text.toString().trim()
        when {
            antenna.isEmpty() -> binding.textinputantenna.error = "Please enter the Antenna Power"
            antenna.toInt() > 300 -> binding.textinputantenna.error =
                "Entered Antenna Power should be less than 300"

            else -> {
                Utils.setSharedPrefs(this, Constants.KEY_ANTENNA_POWER, antenna)
                Utils.showCustomDialogFinish(this, "Antenna Power Successfully Updated")
            }
        }
    }


    private fun checkInputUrl() {
        val edHttp = binding.edHttp.text.toString().trim()
        val edServerIp = binding.edServerIp.text.toString().trim()

        if (edServerIp.isEmpty()) {
            binding.edServerIp.error = "Please enter IP address"
            return  // Stop execution if IP is empty
        }

        if (edHttp.isEmpty()) {
            binding.edHttp.error = "Please enter Http/Https"
            return  // Stop execution if HTTP is empty
        }

        Utils.setSharedPrefs(this@AdminActivity, Constants.KEY_IS_ADMIN, "")
        Utils.setSharedPrefs(this@AdminActivity, Constants.KEY_USER_NAME, "")
        Utils.setSharedPrefs(this@AdminActivity, Constants.KEY_JWT_TOKEN, "")
        Utils.setSharedPrefs(this@AdminActivity, Constants.KEY_SERVER_IP, edServerIp)
        Utils.setSharedPrefs(this@AdminActivity, Constants.KEY_HTTP, edHttp)
        showCustomDialogFinish(this, "Base URL Updated. Changes will take place after Re-Login")
    }

    private fun showCustomDialogFinish(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .show()
    }
}
