package com.example.utlite.view

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.utlite.R
import com.example.utlite.databinding.ActivityHomeBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Utils

class HomeActivity : AppCompatActivity() {
    lateinit var binding:ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=DataBindingUtil.setContentView(this,R.layout.activity_home)

        binding.cvVehicleDetection.setOnClickListener {
            navigateTo(VechileDetectionActivity::class.java)
        }
        binding.cvRfidTagMapping.setOnClickListener {
            navigateTo(VehicleTagMappingActivity::class.java)
        }
        binding.cvTrackVehicle.setOnClickListener {
            navigateTo(TrackVehicleActivity::class.java)
        }
        binding.cvTpScan.setOnClickListener {
            navigateTo(TppageActivity::class.java)
        }
        binding.cvAdminSetting.setOnClickListener {
            navigateTo(AdminActivity::class.java)
        }

        val isAdmin = Utils.getSharedPrefs(this@HomeActivity, Constants.KEY_IS_ADMIN)
        if (isAdmin == "true") {
            binding.cvAdminSetting.visibility = View.VISIBLE
        }
        binding.ivUserDetails.setOnClickListener {
            showLogoutPopup()
        }
    }
    fun Activity.navigateTo(targetActivity: Class<out Activity>) {
        startActivity(Intent(this, targetActivity))
    }
    fun showLogoutPopup() {
        val builder: AlertDialog.Builder
        val alert: AlertDialog
        builder = AlertDialog.Builder(this@HomeActivity)
        builder.setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                Utils.setSharedPrefs(this@HomeActivity, Constants.KEY_IS_ADMIN, "")
                Utils.setSharedPrefs(this@HomeActivity, Constants.KEY_USER_NAME, "")
                Utils.setSharedPrefs(this@HomeActivity, Constants.KEY_JWT_TOKEN, "")
                Utils.setSharedPrefsBoolean(this@HomeActivity, Constants.KEY_IS_LOGGED_IN, false)
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog: DialogInterface, id: Int -> dialog.cancel() }
        alert = builder.create()
        alert.show()
    }
}