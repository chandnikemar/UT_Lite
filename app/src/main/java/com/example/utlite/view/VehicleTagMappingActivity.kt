package com.example.utlite.view

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.R
import com.example.utlite.databinding.ActivityVehicleTagMappingBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.RFIDHandler
import com.example.utlite.helper.Resource
import com.example.utlite.helper.SessionManager
import com.example.utlite.helper.Utils
import com.example.utlite.model.vehicletagmapping.RfidMappingModel
import com.example.utlite.repository.UtLiteRepository
import com.example.utlite.viewmodel.VehicleTagMappingViewModel
import com.example.utlite.viewmodel.VehicleTagMappingViewModelFactory
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty

class VehicleTagMappingActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface  {
    lateinit var binding: ActivityVehicleTagMappingBinding
    private lateinit var progress: ProgressDialog
    private lateinit var viewModel: VehicleTagMappingViewModel
    lateinit var tagDataSet: MutableList<String>
    var rfidHandler: RFIDHandler? = null

    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_tag_mapping)
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        tagDataSet= mutableListOf()
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"
        setSupportActionBar(binding.vehicleTagMappingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val utLiteRepository = UtLiteRepository()
        val viewModelProviderFactory =
            VehicleTagMappingViewModelFactory(application, utLiteRepository)
        viewModel = ViewModelProvider(
            this,
            viewModelProviderFactory
        )[VehicleTagMappingViewModel::class.java]

        viewModel.rfidMappingMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                val rfid = binding.autoCompleteTextViewRfid.text.toString().trim()
                                binding.autoCompleteTextViewRfid.inputType = 0
                                if (binding.btnVehicleMapping.text == "Verify Tag") {
                                    binding.textInputLayoutVehicleno.visibility = View.VISIBLE
                                    binding.tvVrn.setText( resultResponse.vrn)
                                   binding. tvVrn.isFocusable = false
                                    binding.autoCompleteTextViewRfid.setText(rfid)
                                    binding.btnVehicleMapping.visibility = View.GONE
                                    Utils.showCustomDialog(this@VehicleTagMappingActivity, resultResponse.statusMessage)

                                } else {
                                    binding.textInputLayoutVehicleno.visibility = View.VISIBLE
                                    binding.tvVrn.setText(resultResponse.vrn)
                                    binding.tvVrn.isFocusable = false
                                    binding.autoCompleteTextViewRfid.setText(rfid)
                                    binding.btnVehicleMapping.visibility = View.GONE
                                    Utils.showCustomDialogFinish(this@VehicleTagMappingActivity, resultResponse.statusMessage)
                                }
                            } else {
                                Toasty.warning(this, "Not Found..!", Toasty.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toasty.error(this, "Exception..!", Toasty.LENGTH_SHORT).show()
                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    val errorMsg = response.message ?: "Unknown Error"
                    val vehicleData = response.data // Full model, even in error cases
                    Toasty.error(this, errorMsg, Toasty.LENGTH_SHORT).show()
                    if (vehicleData != null) {
                        if (vehicleData.status == "RecordNotFound") {
                            Utils.showCustomDialog(this@VehicleTagMappingActivity, vehicleData.statusMessage)
                            binding.textInputLayoutVehicleno.visibility = View.VISIBLE
                            binding.autoCompleteTextViewRfid.setText(binding.autoCompleteTextViewRfid.getText().toString().trim())
                            binding.btnVehicleMapping.text = "Map"
                        } else if (vehicleData.status == "Duplicate") {
                            showCustomDialog( "The vehicle already has a different RFID tag mapped. Do you want to overwrite?")
                            binding.textInputLayoutVehicleno.visibility = View.VISIBLE
                            binding.autoCompleteTextViewRfid.setText(binding.autoCompleteTextViewRfid.getText().toString().trim())
                            binding.tvRfid.isFocusable = false
                            binding.tvVrn.isFocusable = false
                            binding.btnVehicleMapping.visibility = View.GONE
                        }
                    }

                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {
                }
            }
        }
        binding.btnVehicleMapping.setOnClickListener {
            when (binding.btnVehicleMapping.text.toString().trim()) {
                "Verify tag" -> verifyTag()
                "Map" -> verifyTagAndVrn()
            }
        }
        try {
            initReader()
        }
        catch (e:Exception)
        {

        }
    }
    private fun verifyTag() {
        val scanRFIDInput = binding.autoCompleteTextViewRfid?.text.toString().trim()

        if (scanRFIDInput.isEmpty()) {
            binding.tvRfid.error = "Press trigger to Scan RFID"
        } else {
            callPostRfidMapApi(false)
        }
    }
    private fun verifyTagAndVrn() {
        val scanVRNInput = binding.textInputLayoutVehicleno.editText?.text.toString().trim()
        when {
            scanVRNInput.isEmpty() -> {
                binding.textInputLayoutVehicleno.error = "Please enter vehicle number"
            }
            scanVRNInput.length < 8 -> {
                binding.textInputLayoutVehicleno.error = "Please enter 8 to 10 digits VRN"
            }
            else -> {
                callPostRfidMapApi(false)
            }
        }
    }
    fun showCustomDialog( message: String) {
        AlertDialog.Builder(this@VehicleTagMappingActivity)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Cancel") { _, _ -> }
            .setNegativeButton("Yes") { _, _ ->
                callPostRfidMapApi(true)
            }
            .show()
    }
    private fun callPostRfidMapApi(override: Boolean) {
        try {
            val modal: RfidMappingModel = when {
                binding.btnVehicleMapping.getText() == "Verify tag" ->
                    RfidMappingModel("123456", "", binding.autoCompleteTextViewRfid.text.toString().trim(), "False")

                binding.btnVehicleMapping.text == "Map" && !override ->
                    RfidMappingModel("123456", binding.tvVrn.text.toString().trim(), binding.autoCompleteTextViewRfid.text.toString().trim(), "False")

                binding.btnVehicleMapping.text == "Map" && override ->
                    RfidMappingModel("123456", binding.tvVrn.text.toString().trim(), binding.autoCompleteTextViewRfid.text.toString().trim(), "True")

                else -> throw IllegalStateException("Invalid button state")
            }

            viewModel.rfidMapping(
                "",
                baseUrl,
                rfidMappingModel = modal
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    ///rfid

    private fun initReader() {
        rfidHandler = RFIDHandler()
        var antennaPower=60
        if(Utils.getSharedPrefs(this@VehicleTagMappingActivity, Constants.KEY_ANTENNA_POWER)=="")
        {
            antennaPower=120
        }
        else{
            antennaPower=Utils.getSharedPrefs(this@VehicleTagMappingActivity, Constants.KEY_ANTENNA_POWER)!!.toInt()

        }
        rfidHandler!!.init(this,this@VehicleTagMappingActivity,antennaPower )
    }

    override fun onPause() {
        super.onPause()
        rfidHandler!!.onPause()
    }

    override fun onResume() {
        super.onResume()


    }
    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler!!.onResume()
        Toast.makeText(this@VehicleTagMappingActivity, status, Toast.LENGTH_SHORT).show()
    }


    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
        runOnUiThread {
            var tagDataFromScan = tagData[0].tagID
            if(tagDataFromScan!=null)
            {
                runOnUiThread {
                    if (!tagDataSet.contains(tagDataFromScan)) {  // Fix: Add only if not present
                        tagDataSet.add(tagDataFromScan)
                    }

                    val adapter1 = ArrayAdapter(
                        this,
                        R.layout.dropdown_menu_popup_item,
                        tagDataSet
                    )

                    if (tagDataSet.size == 1) {
                        binding.autoCompleteTextViewRfid.setText(adapter1.getItem(0).toString(), false)
                    } else {
                        binding.autoCompleteTextViewRfid.setText("")
                        binding.tvRfid.error = "Select the RFID value from dropdown"
                    }

                    binding.autoCompleteTextViewRfid.setAdapter(adapter1)
                }
            }
            stopInventory()
        }
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()
    }
    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        rfidHandler!!.onDestroy()
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}