package com.example.utlite.view

import android.app.ProgressDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.model.TransitGenrate.TransitVehicalRequest
import com.example.utlite.viewmodel.transitPass.TransitPassViewModel
import com.example.utlite.viewmodel.transitPass.transitPassViewModelFactory
import com.example.utlite.R
import com.example.utlite.databinding.ActivityChhattishgharBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.SessionManager
import com.example.utlite.repository.UtLiteRepository
import es.dmoral.toasty.Toasty


class ChhattishgharActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChhattishgharBinding
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private lateinit var progress: ProgressBar
    private lateinit var progressD: ProgressDialog
    private var token: String? = ""
    private lateinit var viewModel: TransitPassViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chhattishghar)
        session = SessionManager(this)
        val userDetails = session.getUserDetails()

        if (userDetails.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {
            token = userDetails["jwtToken"]
        }
        setSupportActionBar(binding.TPToolBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"
        progressD = ProgressDialog(this)
        progressD.setMessage("Please Wait...")
        val utLiteRepository = UtLiteRepository()
        val viewModelProviderFactory = transitPassViewModelFactory(application, utLiteRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[TransitPassViewModel::class.java]
        viewModel.transitPassResponseMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                    binding.layoutCH.visibility= View.GONE
                    binding.frmDetailsCH.visibility = View.VISIBLE
                    binding.mcvBtn.visibility = View.GONE
                    val transitPassDetails = response.data?.transitPassDetailsResponse
                    val transitData = response.data

                    // Setting values for UI fields, use "N/A" if null
                    binding.tvValueTPNumber.text =
                        transitPassDetails?.transitPassNo.toString() ?: "N/A"
                    binding.tvValueVRN.text = transitPassDetails?.vrn.toString() ?: "N/A"
                    binding.tvValueDriver.text=transitPassDetails?.driverDetails.toString()
                    binding.tvVehicleOwner.text =
                        transitPassDetails?.transporterDetails.toString() ?: "N/A"
                    binding.tvValueTare.text = transitPassDetails?.tareWeight?.toString() ?: "N/A"
                    binding.tvValueGross.text = transitPassDetails?.grossWeight?.toString() ?: "N/A"
                    binding.tvValueNet.text = transitPassDetails?.netWeight?.toString() ?: "N/A"
                    binding.tvValueMineral.text =
                        transitPassDetails?.mineralName?.toString() ?: "N/A"
                    binding.tvValueGrade.text = transitPassDetails?.grade ?: "N/A"


                    // Get the status of VRN and RFID tag mapping
                    val isVRNRegistered = transitData?.isVRNRegistered
                    val isRFIDTagMapped = transitData?.isRFIDTagMapped

                    // Check if both VRN is registered and RFID tag is mapped
                    if (isVRNRegistered == true && isRFIDTagMapped == true) {
                        binding.btnSubmit.visibility = View.VISIBLE // Show the Submit button
                        binding.btnSubmit.setBackgroundResource(R.drawable.round_corners_30dp)
                        // On Button click
                        binding.btnSubmit.setOnClickListener {


                            if (transitPassDetails != null) {


                                val transitVehicalRequest = TransitVehicalRequest(
                                    transitPassId = transitPassDetails.transitPassId,
                                    transitPassNo = transitPassDetails.transitPassNo,
                                    vrn = transitData.vrn ?: "N/A",
                                    driverDetails = transitPassDetails.driverDetails ?: "N/A",
                                    transporterDetails = transitPassDetails.transporterDetails
                                        ?: "N/A",
                                    tareWeight = transitPassDetails.tareWeight.toInt(),        // Convert to Int
                                    grossWeight = transitPassDetails.grossWeight.toInt(),      // Convert to Int
                                    netWeight = transitPassDetails.netWeight.toInt(),  // Keep as Double Int
                                    mineralName = transitPassDetails.mineralName ?: "N/A",
                                    grade = transitPassDetails.grade ?: "N/A",
                                    transitPassType = transitPassDetails.transitPassType ?: "N/A",
                                    status = transitPassDetails.status ?: "N/A",
                                    isActive = transitPassDetails.isActive,
                                    createdBy = transitPassDetails.createdBy ?: "System",
                                    createdDate = transitPassDetails.createdDate
                                        ?: "2025-03-24T10:23:43.6412076", // Example date
                                    modifiedBy = null,
                                    modifiedDate = null,

                                    )

                                Log.d(
                                    "TransitPassActivity",
                                    "Creating TransitPass with data: $transitVehicalRequest"
                                )

//                                Toasty.success(this, "Transit pass is created", Toasty.LENGTH_SHORT).show()

                                // Send the request to create the TransitPass
                                val bearerToken = token ?: ""
                                viewModel.createTransitPass(
                                    bearerToken,
                                    baseUrl,
                                    transitVehicalRequest
                                )
                            } else {
                                Toasty.error(this, "Transit Data is missing", Toasty.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {


                        // Show the appropriate dialog based on registration and mapping status
                        when {
                            isVRNRegistered == false && isRFIDTagMapped == false -> {
                                showDialogToRegisterVRNAndMapRFID()
                                binding.tvValueVRN.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.red
                                    )
                                )  // Using color from resources
                                binding.etRFID.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.red
                                    )
                                )
                                binding.btnSubmit.isEnabled = false
                                binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)  // Set a specific disabled color
                            }

                            isVRNRegistered == false -> {
                                showDialogToRegisterVRN()
                                binding.etRFID.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.red
                                    )
                                )
                                binding.btnSubmit.isEnabled = false
                                binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                            }

                            isRFIDTagMapped == false -> {
                                showDialogToMapRFIDTag()
                                binding.etRFID.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.red
                                    )
                                )
                                binding.btnSubmit.isEnabled = false
                                binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled)
                            }
                        }
                    }


                    // Success toast message
                    Toasty.success(
                        this,
                        "Transit Pass details fetched successfully",
                        Toasty.LENGTH_SHORT
                    ).show()
                    dismissProgressBar()
                }

                is Resource.Error -> {

                    // Handle error response
                    Toasty.error(this, "Failed to fetch transit pass details", Toasty.LENGTH_SHORT)
                        .show()
                    dismissProgressBar()
                }

                is Resource.Loading -> {
                    // Show progress while waiting for the response
                    showProgressBar()
                }
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
                    showProgressBar()
                }
            }
        }
        binding.mcvBtn.setOnClickListener {
            val tpNumberStr = binding.etSearch.text.toString().trim()

            if (tpNumberStr.isNotEmpty()) {
                val tpNumber = tpNumberStr.toIntOrNull()

                if (tpNumber != null) {
                    val type = "TP" // Default type
                    val bearerToken = token ?: ""

                    // Call ViewModel to fetch Transit Pass details
                    viewModel.getTransitPassDetails(bearerToken, baseUrl, tpNumber)
                } else {
                    // Show error if TP Number is not valid
                    Toasty.error(this, "Please enter a valid TP Number", Toasty.LENGTH_SHORT).show()
                }
            } else {
                // Show error if TP Number is empty
                Toasty.error(this, "Please enter a valid TP Number", Toasty.LENGTH_SHORT).show()
            }
        }
        binding.btnClear.setOnClickListener {
            // Clear the text fields when the button is clicked

            binding.tvValueVRN.text = "" // Clear the VRN value
            binding.tvValueTPNumber.text = "" // Clear the RFID value
            binding.tvValueTare.text = "" // Clear the Tare value
            binding.tvValueGross.text = "" // Clear the Gross weight
            binding.tvValueNet.text = "" // Clear the Net weight
            binding.tvValueMineral.text = "" // Clear the Mineral value

            binding.tvModelRFID.text = "" // Clear the VRN value

            binding.etSearch.text?.clear()

            binding.layoutCH.visibility= View.VISIBLE
            binding.mcvBtn.visibility = View.VISIBLE
            // Optionally, you can hide the details frame if you want to reset the UI
            binding.frmDetailsCH.visibility = View.GONE

            // Reset the state of other UI components like buttons
            binding.btnSubmit.isEnabled = true // Enable the submit button again
            binding.btnSubmit.setBackgroundResource(R.drawable.clear_btn_round_disabled) // Set the background resource for the submit button
        }
    }
        private fun showDialogToRegisterVRN() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR!!")
        builder.setMessage("Your VRN is not registered")
        builder.setPositiveButton("OK") { dialog, _ ->

            dialog.dismiss()

        }

        // Show the dialog
        builder.create().show()
    }


    private fun showDialogToMapRFIDTag() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR!!")
        builder.setMessage("Your RFID tag is not mapped")
        builder.setPositiveButton("OK") { dialog, _ ->
            // Handle the OK button click
            dialog.dismiss()
            // You can navigate to the appropriate screens or trigger the necessary actions.
        }

        // Show the dialog
        builder.create().show()
    }
    private fun showDialogToRegisterVRNAndMapRFID() {
        val builder = AlertDialog.Builder(this)


        val titleText = "  ERROR!!"
        val spannableTitle = SpannableString(titleText)


        spannableTitle.setSpan(ForegroundColorSpan(Color.RED), 0, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


        val warningIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert)
        warningIcon?.setBounds(0, 0, warningIcon.intrinsicWidth, warningIcon.intrinsicHeight)


        val iconSpan = ImageSpan(warningIcon!!, ImageSpan.ALIGN_BASELINE)
        spannableTitle.setSpan(iconSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // Attach the icon to the first character


        builder.setTitle(spannableTitle)

        builder.setMessage("Your VRN is not registered and RFID tag is not mapped")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Show the dialog
        builder.create().show()
    }

    private fun showProgressBar() {
        progressD.show()
    }
    private fun dismissProgressBar() {
        progressD.dismiss()
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