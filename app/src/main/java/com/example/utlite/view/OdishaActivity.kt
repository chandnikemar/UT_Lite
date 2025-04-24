package com.example.utlite.view

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.R
import com.example.utlite.databinding.ActivityOdishaBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.SessionManager
import com.example.utlite.model.TransitGenrate.TransitVehicalRequest
import com.example.utlite.model.TransitGenrate.QrTransitPassRequest
import com.example.utlite.repository.UtLiteRepository
import com.example.utlite.viewmodel.transitPass.TransitPassViewModel
import com.example.utlite.viewmodel.transitPass.transitPassViewModelFactory
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerResults
import com.symbol.emdk.barcode.StatusData
import es.dmoral.toasty.Toasty

class OdishaActivity : AppCompatActivity(), EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener {

    lateinit var binding: ActivityOdishaBinding

    var isBarcodeInit = false
    private lateinit var session: SessionManager
    private var token: String? = ""
    private lateinit var progress: ProgressDialog

    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private lateinit var viewModel: TransitPassViewModel
    private var emdkManager: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var scanner: Scanner? = null
    companion object {
        const val REQUEST_CODE = 7777
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_odisha)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_odisha)
        session = SessionManager(this)

        val userDetails = session.getUserDetails()
        if (userDetails.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {
            token = userDetails["jwtToken"]
        }

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        setSupportActionBar(binding.TPToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        serverIpSharedPrefText = userDetails[Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails[Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"


        val utLiteRepository = UtLiteRepository()
        val viewModelProviderFactory = transitPassViewModelFactory(application, utLiteRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[TransitPassViewModel::class.java]

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            initBarcode()
        }

        viewModel.qrTransitPassResponseMutable.observe(this) { response ->
            when (response) {
                is Resource.Loading -> {
                    binding.frmDetailsOD.visibility = View.VISIBLE
                    binding.layoutCH.visibility=View.GONE
                    Log.d("API_Response", "API is loading")
                }

                is Resource.Success -> {
                    binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                    val transitPassDetails = response.data?.transitPassDetailsResponse
                    val transitData = response.data

                    binding.etRFID.text = transitData?.rfidTagNo?.takeIf { it.isNotEmpty() } ?: "N/A"
//                    binding.tvValueVRN.text = transitPassDetails?.vrn.toString() ?: "N/A"
//                    binding.tvVehicleOwner.text = transitPassDetails?.transporterDetails.toString() ?: "N/A"
//                    binding.tvValueTare.text = transitPassDetails?.tareWeight?.toString() ?: "N/A"
//                    binding.tvValueGross.text = transitPassDetails?.grossWeight?.toString() ?: "N/A"
//                    binding.tvValueNet.text = transitPassDetails?.netWeight?.toString() ?: "N/A"
//
//                    binding.tvValueGrade.text = transitPassDetails?.grade ?: "N/A"
                    Log.d("TransitPassActivity", "Creating TransitPass with data: $transitPassDetails")
                    val isVRNRegistered = transitData?.isVRNRegistered
                    val isRFIDTagMapped = transitData?.isRFIDTagMapped

                    if (isVRNRegistered == true && isRFIDTagMapped == true) {
                        binding.btnSubmit.setBackgroundResource(R.drawable.round_corners_30dp)
                        binding.btnSubmit.setOnClickListener {
                            if (transitPassDetails != null) {
                                val transitVehicalRequest = TransitVehicalRequest(
                                    transitPassId = transitPassDetails.transitPassId,
                                    transitPassNo = transitPassDetails.transitPassNo,
                                    vrn = transitData.vrn ?: "N/A",
                                    driverDetails = transitPassDetails.driverDetails ?: "N/A",
                                    transporterDetails = transitPassDetails.transporterDetails ?: "N/A",
                                    tareWeight = transitPassDetails.tareWeight.toInt(),
                                    grossWeight = transitPassDetails.grossWeight.toInt(),
                                    netWeight = transitPassDetails.netWeight.toInt(),
                                    mineralName = transitPassDetails.mineralName ?: "N/A",
                                    grade = transitPassDetails.grade ?: "N/A",
                                    transitPassType = transitPassDetails.transitPassType ?: "N/A",
                                    status = transitPassDetails.status ?: "N/A",
                                    isActive = transitPassDetails.isActive,
                                    createdBy = transitPassDetails.createdBy ?: "System",
                                    createdDate = transitPassDetails.createdDate ?: "2025-03-24T10:23:43.6412076",
                                    modifiedBy = null,
                                    modifiedDate = null
                                )

                                Log.d("TransitPassActivity", "Creating TransitPass with data: $transitVehicalRequest")
                                val bearerToken = token ?: ""
                                viewModel.createTransitPass(bearerToken, baseUrl, transitVehicalRequest)
                            }
                        }
                    } else {
                        when {
                            isVRNRegistered == false && isRFIDTagMapped == false -> {
                                showDialogToRegisterVRNAndMapRFID()
                                binding.tvValueVRN.setTextColor(ContextCompat.getColor(this, R.color.red))
                                binding.etRFID.setTextColor(ContextCompat.getColor(this, R.color.red))
                                binding.btnSubmit.isEnabled = false
                                binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                            }
                            isVRNRegistered == false -> {
                                showDialogToRegisterVRN()
                                binding.tvValueVRN.setTextColor(ContextCompat.getColor(this, R.color.red))
                                binding.btnSubmit.isEnabled = false
                                binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                            }
                            isRFIDTagMapped == false -> {
                                showDialogToMapRFIDTag()
                                binding.etRFID.setTextColor(ContextCompat.getColor(this, R.color.red))
                                binding.btnSubmit.isEnabled = false
                                binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                            }
                        }
                    }

                    Toasty.success(this, "Transit Pass details fetched successfully", Toasty.LENGTH_SHORT).show()
                    dismissProgressBar()
                }

                is Resource.Error -> {
                    Log.e("API_Response", "API call failed: ${response.message}")
                }

                else -> {}
            }
        }
        viewModel.transitPassCreateResponseMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    // Handle the success case
                    Log.d("TransitPassActivitySuccess", "Success: ${response.data}")

                    // Check if responseMessage is not null, if it is, use it as success message
                    val successMessage = response.data?.responseMessage
                    val errorMessage = response.data?.errorMessage

                    if (successMessage != null) {
                        // Show success message using Toasty
                        Toasty.success(this, successMessage, Toasty.LENGTH_SHORT).show()
                    } else {
                        // If there's no responseMessage, show the general success info (optional)
                        if (errorMessage != null) {
                            Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                        }
                    }

                    dismissProgressBar()
                }

                is Resource.Error -> {
                    // Handle error case
                    Log.d("TransitPassActivityError", "Error: ${response.message}")

                    // Check if responseMessage is null, then fall back to errorMessage
                    val errorMessage = response.data?.errorMessage ?: "Unknown error occurred"

                    // Show error message using Toasty
                    Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                    dismissProgressBar()
                }

                is Resource.Loading -> {
                    // Show progress while waiting for the API response

                }

                else -> {}
            }
        }

        binding.etSearch.setOnClickListener {
            val intent = Intent(this, ScanBarcodeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
        binding.btnClear.setOnClickListener {
            binding.tvValueVRN.text = ""
            binding.tvValueTare.text = ""
            binding.tvValueGross.text = ""
            binding.tvValueNet.text = ""
            binding.tvValueMineral.text = ""
            binding.etSearch.text?.clear()
            binding.frmDetailsOD.visibility = View.GONE
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
            binding.layoutCH.visibility=View.VISIBLE
        }
    }

    private fun initBarcode() {
//        isBarcodeInit = true

        val results = EMDKManager.getEMDKManager(this@OdishaActivity, this)
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(ContentValues.TAG, "EMDKManager object request failed!")
        } else {
            Log.e(ContentValues.TAG, "EMDKManager object initialization is in progress.......")
        }
    }

    override fun onOpened(emdkManager: EMDKManager?) {
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            this.emdkManager = emdkManager
            initBarcodeManager()
            initScanner()
        }
    }

    override fun onClosed() {
        try {
            scanner?.apply {
                disable()
                removeDataListener(this@OdishaActivity)
                removeStatusListener(this@OdishaActivity)
                release()
            }
            scanner = null

            barcodeManager = null
            emdkManager?.release()
            emdkManager = null
        } catch (e: Exception) {
            Log.e("EMDK", "Error during EMDK cleanup", e)
        }
    }

    private fun initBarcodeManager() {
        barcodeManager =
            emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        if (barcodeManager == null) {
            Toast.makeText(
                this@OdishaActivity, "Barcode scanning is not supported.", Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun initScanner() {
        try {
            if (barcodeManager == null) {
                barcodeManager = emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as? BarcodeManager
            }

            // Avoid reusing an already-enabled scanner
            if (scanner != null) {
                scanner?.release()
                scanner = null
            }

            scanner = barcodeManager?.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            scanner?.apply {
                addDataListener(this@OdishaActivity)
                addStatusListener(this@OdishaActivity)
                triggerType = Scanner.TriggerType.HARD
                enable()
            }

        } catch (e: ScannerException) {
            Log.e("Scanner", "Failed to enable scanner", e)
            Toast.makeText(this, "Scanner failed to enable: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private fun showDialogToRegisterVRN() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This VRN is not registered. Please register the VRN.")
            .setCancelable(false)
//            .setPositiveButton("Register") { dialog, id ->
//                val intent = Intent(this@OdishaActivity, HomeActivity::class.java)
//                startActivity(intent)
//            }
            .setNegativeButton("ok") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun showDialogToMapRFIDTag() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This RFID tag is not mapped. Please map the RFID tag.")
            .setCancelable(false)
//            .setPositiveButton("Map RFID") { dialog, id ->
//                val intent = Intent(this@OdishaActivity, HomeActivity::class.java)
//                startActivity(intent)
//            }
            .setNegativeButton("Ok") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun showDialogToRegisterVRNAndMapRFID() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Both VRN is not registered and RFID tag is not mapped. Please register VRN and map RFID tag.")
            .setCancelable(false)
//            .setPositiveButton("Proceed") { dialog, id ->
//                val intent = Intent(this@OdishaActivity, HomeActivity::class.java)
//                startActivity(intent)
//            }
            .setNegativeButton("OK") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun dismissProgressBar() {
        if (progress.isShowing) {
            progress.dismiss()
        }
    }
//    override fun onData(scanDataCollection: ScanDataCollection?) {
//        var dataStr: String? = ""
//        if (scanDataCollection != null && scanDataCollection.result == ScannerResults.SUCCESS) {
//            val scanData = scanDataCollection.scanData
//            for (data in scanData) {
//                val barcodeData = data.data
//                val labelType = data.labelType
//                dataStr = barcodeData
//            }
//            runOnUiThread { binding.etSearch.setText(dataStr) }
//
//        }
//    }
    override fun onData(scanDataCollection: ScanDataCollection?) {
       var dataStr: String? = ""



        if (scanDataCollection == null) return

        val dataCollection = scanDataCollection.scanData
        if (scanDataCollection != null && scanDataCollection.result == ScannerResults.SUCCESS) {
            val scanData = dataCollection[0]


            val barcodeData = scanData.data
            val dataColumns = barcodeData.split("|")
            Log.d("ScannedData", "dataColumns: ${dataColumns.joinToString(", ")}")
            Log.d("ScannedData/Index", "Scanned data columns: ")
            dataColumns.forEachIndexed { index, column ->
                Log.d("ScannedData", "Index: $index, Value: $column")
            }


            if (dataColumns.size > 34) {
           val  vrn = dataColumns[34]
                val transitPassNo = dataColumns[3]//"I12509666/912"
//                val vrn = "CG13AT6111"
                val driverDetails =dataColumns[15]//""// dataColumns[7]//""  // Empty in your case
                val transporterDetails =dataColumns[8]
                val tareWeight = dataColumns[32]//16.450
                val grossWeight =  dataColumns[31]//53.610
                val netWeight = dataColumns[33]//37.160
                val mineralName =  dataColumns[22]//"Coal"
                val grade =dataColumns[24]+dataColumns[25] //"ROM - G13"

                runOnUiThread {
                    binding.etSearch.setText(vrn)

                    binding.tvValueTPNumberOD.text =  transitPassNo
                    binding.tvValueVRN.text= vrn

                    binding.tvValueDriver.text=driverDetails
                    binding.tvVehicleOwner.text=transporterDetails
                    binding.tvValueTare.text=tareWeight
                    binding.tvValueGross.text=grossWeight
                    binding.tvValueNet.text=netWeight

                    binding.tvValueMineral.text=mineralName
                    binding.tvValueGrade.text=grade

                }
                val qrTransitPassRequest = QrTransitPassRequest(
                        transitPassNo = transitPassNo,
                vrn = vrn,
                driverDetails = driverDetails,
                transporterDetails = transporterDetails,
                tareWeight = tareWeight,
                grossWeight = grossWeight,
                netWeight = netWeight,
                mineralName = mineralName,
                grade = grade
                )
                viewModel.addTransitPassByQRCode(token.toString(), baseUrl, qrTransitPassRequest)
            } else {
                Log.e(ContentValues.TAG, "Scanned data does not contain enough columns.")
            }
        }
    }

    override fun onStatus(statusData: StatusData) {
        val state = statusData.state
        var statusStr = ""
        when (state) {
            StatusData.ScannerStates.IDLE -> {
                statusStr = statusData.friendlyName + " is enabled and idle..."
                setConfig()
                try {
                    scanner!!.read()
                } catch (e: ScannerException) {
                }
            }

            StatusData.ScannerStates.WAITING -> statusStr =
                "Scanner is waiting for trigger press..."

            StatusData.ScannerStates.SCANNING -> statusStr = "Scanning..."
            StatusData.ScannerStates.DISABLED -> {}
            StatusData.ScannerStates.ERROR -> statusStr = "An error has occurred."
            else -> {}
        }
        setStatusText(statusStr)
    }

    private fun setConfig() {
        if (scanner != null) {
            try {
                val config = scanner!!.config
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true
                }
                scanner!!.config = config
            } catch (e: ScannerException) {
                Log.e("TAG", e.message!!)
            }
        }
    }
    fun setStatusText(msg: String) {
        Log.e("TAG", "StatusText: $msg")
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            scanner?.apply {
                disable()
                removeDataListener(this@OdishaActivity)
                removeStatusListener(this@OdishaActivity)
                release()
            }
            scanner = null

            barcodeManager = null
            emdkManager?.release()
            emdkManager = null
        } catch (e: Exception) {
            Log.e("Scanner", "Error releasing scanner", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }
}
