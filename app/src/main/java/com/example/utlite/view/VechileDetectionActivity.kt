package com.example.utlite.view

import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.R
import com.example.utlite.databinding.ActivityVechileDetectionBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.RFIDHandler
import com.example.utlite.helper.Resource
import com.example.utlite.helper.SessionManager
import com.example.utlite.helper.Utils
import com.example.utlite.model.vehicledetection.GetLocationMasterDataByLocationIdResponse
import com.example.utlite.model.vehicledetection.Location
import com.example.utlite.model.vehicledetection.PostRfidModel
import com.example.utlite.repository.UtLiteRepository
import com.example.utlite.viewmodel.VehicleDetectionViewModel
import com.example.utlite.viewmodel.VehicleDetectionViewModelFactory
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty

class VechileDetectionActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {
    lateinit var binding: ActivityVechileDetectionBinding
    var isRfidOrVrn = true
    var parentLocationsModel: List<Location>? = null
    var childLocationModel: List<Location>? = null

    var child2LocationModel: ArrayList<GetLocationMasterDataByLocationIdResponse>? = null

    private var parentLocation: ArrayList<String>? = null
    private var parentLocationAdapter: ArrayAdapter<String>? = null

    private var childLocation: ArrayList<String>? = null
    private var childLocationAdapter: ArrayAdapter<String>? = null

    private var child2Location: ArrayList<String>? = null
    private var child2LocationAdapter: ArrayAdapter<String>? = null


    private var parentLocationMapping: HashMap<String, String>? = null
    private var childLocationMapping: HashMap<String, String>? = null
    private var child2LocationMapping: HashMap<String, String>? = null

    private val selectedParentLocationId = 0
    private var selectedChildLocationId = 0
    private var selectedChild2LocationId = 0
    private lateinit var progress: ProgressDialog
    private lateinit var viewModel: VehicleDetectionViewModel
    lateinit var tagDataSet: MutableList<String>
    var rfidHandler: RFIDHandler? = null

    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vechile_detection)
        binding.rgVehicleDetails.check(binding.rgVehicleDetails.getChildAt(0).id)
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"
        setSupportActionBar(binding.vehicleDetectionToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        tagDataSet= mutableListOf()
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
        parentLocationMapping = HashMap()
        childLocationMapping = HashMap()
        child2LocationMapping = HashMap()
        parentLocation = ArrayList()
        childLocation = ArrayList()
        child2Location = ArrayList()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val utLiteRepository = UtLiteRepository()
        val viewModelProviderFactory =
            VehicleDetectionViewModelFactory(application, utLiteRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[VehicleDetectionViewModel::class.java]


        viewModel.vehicleLocationListMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                parentLocationsModel = resultResponse.locations
                                for (location in parentLocationsModel!!) {
                                    parentLocation!!.add(location.displayName)
                                    parentLocationMapping!![location.displayName] =
                                        location.locationCode
                                }
                                populateParentLocationDropdown(parentLocation!!)
                            } else {
                                binding.textInputLayoutlocation.visibility = View.GONE
                                binding.textInputLayoutChild3.visibility = View.GONE
                                binding.textInputLayoutChild2.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            binding.textInputLayoutlocation.visibility = View.GONE
                            binding.textInputLayoutChild3.visibility = View.GONE
                            binding.textInputLayoutChild2.visibility = View.GONE
                        }


                    }
                }

                is Resource.Error -> {

                    binding.textInputLayoutlocation.visibility = View.GONE
                    binding.textInputLayoutChild3.visibility = View.GONE
                    binding.textInputLayoutChild2.visibility = View.GONE
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
        viewModel.vehicleLocationListChild2MutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    childLocation!!.clear()
                    binding.textInputLayoutChild3.visibility = View.GONE
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                childLocationModel = resultResponse.locations
                                for (location in childLocationModel!!) {
                                    childLocation!!.add(location.displayName)
                                    childLocationMapping!![location.displayName] =
                                        location.locationId.toString()
                                }
                                if (childLocation!!.size > 0) {
                                    binding.textInputLayoutChild2.visibility = View.VISIBLE
                                    populateChildDropdown(childLocation!!)
                                } else {
                                    binding.textInputLayoutChild3.visibility = View.GONE
                                    binding.textInputLayoutChild2.visibility = View.GONE
                                }
                            } else {
                                binding.textInputLayoutChild3.visibility = View.GONE
                                binding.textInputLayoutChild2.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            binding.textInputLayoutChild3.visibility = View.GONE
                            binding.textInputLayoutChild2.visibility = View.GONE
                        }


                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    binding.textInputLayoutChild3.visibility = View.GONE
                    binding.textInputLayoutChild2.visibility = View.GONE
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
        viewModel.getLocationMasterDataByLocationLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    child2Location!!.clear()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                child2LocationModel = resultResponse
                                if (child2LocationModel?.size!! > 0) {
                                    for (location in child2LocationModel!!) {
                                        child2Location!!.add(location.deviceName)
                                        child2LocationMapping!![location.deviceLocationMappingId.toString()] =
                                            location.deviceName
                                    }
                                    if (child2Location!!.size > 0) {
                                        binding.textInputLayoutChild3.visibility = View.VISIBLE
                                        populateChild2Dropdown(child2Location!!)
                                    } else {
                                        binding.textInputLayoutChild3.visibility = View.GONE
                                    }
                                } else {
                                    binding.textInputLayoutChild3.visibility = View.GONE
                                }
                            } else {

                            }
                        } catch (e: Exception) {

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
        viewModel.postRfidLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    setToDefault()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                if (resultResponse.statusMessage != null) {
                                    Utils.showCustomDialogFinish(
                                        this@VechileDetectionActivity,
                                        resultResponse.statusMessage
                                    )
                                } else {
                                    Utils.showCustomDialogFinish(
                                        this@VechileDetectionActivity,
                                        "Success"
                                    )
                                    // finish();
                                }
                            } else {

                            }
                        } catch (e: Exception) {

                        }


                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    setToDefault()
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
            confirmInput()
        }

        populateDropdown()
        getChild1LocationDefaultDataApi()

        try {
            initReader()
        }
        catch (e:Exception)
        {

        }
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    fun populateDropdown() {
        val adapter1 = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            resources.getStringArray(R.array.vehicle_detectionreasons)
        )
        val editTextFilledExposedDropdown1: AutoCompleteTextView =
            findViewById(R.id.autoCompleteTextView_reason)

        editTextFilledExposedDropdown1.setAdapter(adapter1)
    }

    fun populateParentLocationDropdown(locationDataArray: ArrayList<String>) {
        parentLocationAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            locationDataArray
        )
        binding.autoCompleteTextViewLocation.setAdapter(parentLocationAdapter)
        binding.autoCompleteTextViewLocation.setOnItemClickListener { adapterView, _, _, _ ->
            val selectedItem = binding.autoCompleteTextViewLocation.text.toString()
            val selectedKey = parentLocationMapping?.get(selectedItem)

            selectedKey?.let {
                getChild2LocationDefaultDataApi(it)
            }
        }
    }

    fun populateChildDropdown(locationDataArray: ArrayList<String>) {
        binding.autoCompleteTextViewLocationChild2.setText("")
        childLocationAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            locationDataArray
        )
        binding.autoCompleteTextViewLocationChild2.setAdapter(childLocationAdapter)
        binding.autoCompleteTextViewLocationChild2.setOnItemClickListener { adapterView, _, i, _ ->
            val selectedItem = binding.autoCompleteTextViewLocationChild2.text.toString()
            val selectedKey = childLocationMapping?.get(selectedItem)

            selectedKey?.let {
                selectedChildLocationId = it.toInt()
                getChild3LocationDefaultDataApi(it.toInt())
            }
        }
    }

    fun populateChild2Dropdown(locationDataArray: ArrayList<String>) {
        binding.autoCompleteTextViewLocationChild3.setText("")

        child2LocationAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            locationDataArray
        )

        binding.autoCompleteTextViewLocationChild3.setAdapter(child2LocationAdapter)

        binding.autoCompleteTextViewLocationChild3.setOnItemClickListener { adapterView, _, _, _ ->
            val selectedItem = binding.autoCompleteTextViewLocationChild3.text.toString()
            val selectedKey = child2LocationMapping?.entries?.find { it.value == selectedItem }?.key
            selectedKey?.let {
                selectedChild2LocationId = it.toInt()
            }
        }
    }

    fun confirmInput() {
        if (!validateReason() || !validateLocation() || !validateRFIDorVRN()) {
            return
        }
        postRfidApi()

    }

    private fun validateReason(): Boolean {
        val reasonInput: String =
            binding.autoCompleteTextViewReason.getText().toString().trim { it <= ' ' }
        return if (reasonInput.isEmpty()) {
            binding.textInputLayoutreasons.setError("Please Select a reason")
            false
        } else {
            binding.textInputLayoutreasons.setError(null)
            true
        }
    }

    private fun validateLocation(): Boolean {
        return if (selectedChild2LocationId == 0) {
            Toast.makeText(this, "Please Select Location", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateRFIDorVRN(): Boolean {
        val scanRFIDInput: String = binding.autoCompleteTextViewRfid.getText().toString().trim { it <= ' ' }
        val vrnInput: String =
            binding.tvVrn.getText().toString().trim { it <= ' ' }
        if (isRfidOrVrn  && scanRFIDInput.isEmpty()) {
           binding.tvRfid.setError("Press trigger to Scan RFID")
            return false
        } else if (isRfidOrVrn==false && vrnInput.isEmpty()) {
            binding.textInputLayoutVehicleno.setError("Please enter VRN")
            return false
        } else if (isRfidOrVrn==false && vrnInput.length < 8) {
            binding.textInputLayoutVehicleno.setError("Please enter 8 to 10 digits VRN")
        }
        return true
    }

    private fun getChild1LocationDefaultDataApi() {
        try {
            viewModel.getVehicleLocationList(
                "",
                baseUrl = baseUrl,
                requestId = 123456,
                parentLocationCode = "null"
            )
        } catch (e: Exception) {

        }
    }

    private fun getChild2LocationDefaultDataApi(parentLocationCode: String) {
        try {
            viewModel.getVehicleLocationListChild2(
                "",
                baseUrl = baseUrl,
                requestId = 123456,
                parentLocationCode = parentLocationCode
            )

        } catch (e: Exception) {

        }
    }

    private fun getChild3LocationDefaultDataApi(locationId: Int) {
        try {
            viewModel.getLocationMasterDataByLocationId(
                "",
                baseUrl = baseUrl,
                requestId = 123456,
                locationId = locationId
            )

        } catch (e: Exception) {

        }
    }

    private fun postRfidApi() {
        val vrn = binding.tvVrn.text.toString().trim()
        val rfid = binding.autoCompleteTextViewRfid.text.toString().trim()
        try {
            val modal: PostRfidModel
            if (isRfidOrVrn) {
                modal = PostRfidModel(
                    "123456",
                    rfid,
                    selectedChild2LocationId.toString(),
                    "",
                    binding.autoCompleteTextViewReason.text.toString().trim()
                )
            } else {
                modal = PostRfidModel(
                    "123456",
                    "",
                    selectedChild2LocationId.toString(),
                    vrn,
                    binding.autoCompleteTextViewReason.text.toString().trim()
                )
            }


            viewModel.postRfid(
                "",
                baseUrl = baseUrl,
                modal
            )

        } catch (e: Exception) {

        }
    }

    private fun setToDefault() {
        getChild1LocationDefaultDataApi()
        selectedChild2LocationId = 0
        binding.textInputLayoutChild2.visibility = View.GONE
        binding.textInputLayoutChild3.visibility = View.GONE
        binding.autoCompleteTextViewReason.setText("")
        binding.autoCompleteTextViewRfid.setText("")
        binding.autoCompleteTextViewLocation.setText("")
        //TagDataSet.clear()
    }


    ///rfid

    private fun initReader() {
        rfidHandler = RFIDHandler()
        var antennaPower=60
        if(Utils.getSharedPrefs(this@VechileDetectionActivity, Constants.KEY_ANTENNA_POWER)=="")
        {
            antennaPower=120
        }
        else{
            antennaPower=Utils.getSharedPrefs(this@VechileDetectionActivity, Constants.KEY_ANTENNA_POWER)!!.toInt()
        }
        rfidHandler!!.init(this,this@VechileDetectionActivity,antennaPower )
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
        Toast.makeText(this@VechileDetectionActivity, status, Toast.LENGTH_SHORT).show()
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