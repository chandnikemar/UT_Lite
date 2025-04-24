package com.example.utlite.view

import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.utlite.R
import com.example.utlite.adapter.TrackVehicleAdapter
import com.example.utlite.databinding.ActivityTrackVehicleBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.RFIDHandler
import com.example.utlite.helper.Resource
import com.example.utlite.helper.SessionManager
import com.example.utlite.helper.Utils
import com.example.utlite.model.vehicledetection.Location
import com.example.utlite.model.vehicletracking.JobMilestone
import com.example.utlite.model.vehicletracking.TrackVehicleModel
import com.example.utlite.repository.UtLiteRepository
import com.example.utlite.viewmodel.TrackVehicleViewModel
import com.example.utlite.viewmodel.TrackVehicleViewModelFactory
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty

class TrackVehicleActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {
    lateinit var binding: ActivityTrackVehicleBinding
    private lateinit var progress: ProgressDialog
    private lateinit var viewModel: TrackVehicleViewModel

    private var trackVehicleAdapter: TrackVehicleAdapter? = null
    lateinit var jobMilestoneList: ArrayList<JobMilestone>

    var isRfidOrVrn = true
    lateinit var tagDataSet: MutableList<String>
    var rfidHandler: RFIDHandler? = null

    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track_vehicle)

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        tagDataSet= mutableListOf()
        jobMilestoneList = ArrayList()


        session = SessionManager(this)
        userDetails = session.getUserDetails()
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"


        setSupportActionBar(binding.trackVehicleToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        val utLiteRepository = UtLiteRepository()
        val viewModelProviderFactory = TrackVehicleViewModelFactory(application, utLiteRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[TrackVehicleViewModel::class.java]

        binding.rgVehicleDetails.check(binding.rgVehicleDetails.getChildAt(0).id)

        binding.rgVehicleDetails.setOnCheckedChangeListener { buttonView, selected ->
            if (selected == binding.rbScanRfid.getId()) {
                isRfidOrVrn = true
                binding.tvRfid.visibility = View.VISIBLE
                binding.tvVrn.visibility = View.GONE

            } else if (selected == binding.rbVrn.getId()) {
                isRfidOrVrn = false
                binding.tvRfid.visibility = View.GONE
                binding.textInputLayoutVehicleno.visibility = View.VISIBLE
            }
        }

        trackVehicleAdapter = TrackVehicleAdapter(this@TrackVehicleActivity, jobMilestoneList)
        binding.rvTrackVehicle.adapter = trackVehicleAdapter
        binding.rvTrackVehicle.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvTrackVehicle.setHasFixedSize(true)
        viewModel.trackVehicleDetailsMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    jobMilestoneList.clear()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                if(resultResponse.vehicleTransactionDetails.jobMilestones?.size!! >0)
                                {
                                    binding.clScanRfidTag.visibility=View.GONE
                                    binding.clVehicleDetailsBody.visibility=View.VISIBLE
                                    binding.tvvrn.setText(resultResponse.vehicleTransactionDetails.vrn)
                                    binding.tvtxn.setText(resultResponse.vehicleTransactionDetails.vehicleTransactionCode)
                                    binding.tvDriverName.setText(resultResponse.vehicleTransactionDetails.driverName)
                                    val transactionType = when (resultResponse.vehicleTransactionDetails.tranType) {
                                        1 -> "Outbound"
                                        2 -> "Inbound"
                                        3 -> "Internal"
                                        else -> "Unknown"
                                    }
                                    binding.tvtransaction.append(transactionType)

                                    jobMilestoneList.addAll(resultResponse.vehicleTransactionDetails.jobMilestones)
                                }
                                else
                                {
                                    setToDefault()
                                    Toasty.error(this, "Exception..!", Toasty.LENGTH_SHORT).show()
                                }
                            } else {
                                setToDefault()
                                Toasty.warning(this, "Not Found..!", Toasty.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            setToDefault()
                            Toasty.error(this, "Exception..!", Toasty.LENGTH_SHORT).show()
                        }


                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(this, resultResponse, Toasty.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {
                }
            }
        }

        binding.btnScanRfid.setOnClickListener {
            callVehicleData()
        }
        binding.clearAll.setOnClickListener {
            setToDefault()
        }
        try {
            initReader()
        }
        catch (e:Exception)
        {

        }
    }
    private fun setToDefault()
    {
        binding.rgVehicleDetails.check(binding.rgVehicleDetails.getChildAt(0).id)
        binding.clScanRfidTag.visibility=View.VISIBLE
        binding.clVehicleDetailsBody.visibility=View.GONE
        binding.autoCompleteTextViewRfid.setText("")
        binding.tvVrn.setText("")
        jobMilestoneList.clear()
    }
    private fun callVehicleData() {
        try {
            val scanRFIDInput = binding.tvRfid.editText?.text.toString().trim()
            val vrnInput = binding.tvVrn.text.toString().trim()

            when {
                isRfidOrVrn && scanRFIDInput.isEmpty() -> {
                    binding.tvRfid.error = "Press trigger to Scan RFID"
                    return
                }
                !isRfidOrVrn && vrnInput.isEmpty() -> {
                    binding.tvVrn.error = "Please enter VRN"
                    return
                }
                !isRfidOrVrn && vrnInput.length < 8 -> {
                    binding.tvVrn.error = "Please enter 8 to 10 digits VRN"
                    return
                }
            }

            val requestModel = TrackVehicleModel(requestId = "12345",
                rfidTagNo =
                if (isRfidOrVrn) scanRFIDInput else "", vrn = if (!isRfidOrVrn) vrnInput else "")

            viewModel.getTrackVehicleDetails(
                "",
                baseUrl,
                requestModel
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
        if(Utils.getSharedPrefs(this@TrackVehicleActivity, Constants.KEY_ANTENNA_POWER)=="")
        {
            antennaPower=120
        }
        else{
            antennaPower=
                Utils.getSharedPrefs(this@TrackVehicleActivity, Constants.KEY_ANTENNA_POWER)!!.toInt()
        }
        rfidHandler!!.init(this,this@TrackVehicleActivity,antennaPower )
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
        Toast.makeText(this@TrackVehicleActivity, status, Toast.LENGTH_SHORT).show()
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