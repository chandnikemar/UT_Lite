package com.example.utlite.view

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.R
import com.example.utlite.databinding.ActivityLoginBinding
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.SessionManager
import com.example.utlite.helper.Utils
import com.example.utlite.repository.UtLiteRepository
import com.example.utlite.viewmodel.LoginViewModel
import com.example.utlite.viewmodel.LoginViewModelFactory
import com.kemarport.hindalco.model.LoginRequest
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var progress: ProgressDialog

    private lateinit var progressDialog: ProgressDialog
    var installedVersionCode = 0


    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.hide()

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        val androidId: String =
            Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)


        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val packageManager = applicationContext.packageManager

        try {
            // Get the package info for the app
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionCode = packageInfo.versionCode
            // Retrieve the version code and version name

            val versionName = packageInfo.versionName
            installedVersionCode = versionCode
            // binding.tvBuildNo.setText(installedVersionCode.toString())
            //  binding.tvAppVersion.setText("V${versionName}")
            // Log or display the version information
            Log.d("Version", "Version Name: $versionName")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        Log.d("Version", "Version Code: $installedVersionCode")
        val utLiteRepository = UtLiteRepository()
        val viewModelProviderFactory = LoginViewModelFactory(application, utLiteRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"
       // baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText"
        binding.buttonLogin.setOnClickListener {
            login()
        }
        if (Utils.getSharedPrefsBoolean(this, Constants.KEY_IS_LOGGED_IN)) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }
        viewModel.loginMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            session.createLoginSession(
                                firstName = resultResponse.firstName,
                                lastName = resultResponse.lastName,
                                email = resultResponse.email,
                                mobileNumber = resultResponse.mobileNumber.toString(),
                                isVerified = resultResponse.isVerified.toString(),
                                userName = resultResponse.username,
                                jwtToken = resultResponse.jwtToken,
                                refreshToken = resultResponse.refreshToken,
                                roleName = resultResponse.role,
                            )
                            Utils.setSharedPrefsBoolean(
                                this@LoginActivity,
                                Constants.KEY_IS_LOGGED_IN,
                                true
                            )
                            startActivity()
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@LoginActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@LoginActivity,
                            "Login failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }


                else -> {}
            }
        }


    }

    private fun showMessageDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun startActivity() {
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        finish()
    }


    fun login() {
        try {
            if (!validateEmail() || !validatePassword()) {
                return
            }
            val userId = binding.edUserName.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()

            if (userId == "admin" && password == "Pass@123") {
                startAdmin()
            } else {
                // Validate user input
                val validationMessage = validateInput(userId, password)
                if (validationMessage == null) {
                    val loginRequest = LoginRequest(password, userId)
                    viewModel.login(baseUrl, loginRequest)
                } else {
                    showErrorMessage(validationMessage)
                }
            }

        } catch (e: Exception) {
            showErrorMessage(e.printStackTrace().toString())
        }
    }

    private fun validateEmail(): Boolean {
        val emailInput: String =
            binding.edUserName.getText().toString().trim { it <= ' ' }
        return if (emailInput.isEmpty()) {
            binding.textinputusername.setError("Username can't be empty")
            false
        } else {
            binding.textinputusername.setError(null)
            true
        }
    }

    private fun validatePassword(): Boolean {
        val passwordInput: String =
            binding.edPassword.getText().toString().trim { it <= ' ' }
        return if (passwordInput.isEmpty()) {
            binding.textinputpassword.setError("Password can't be empty")
            false
        } else if (passwordInput.length < 6) {
            binding.textinputpassword.setError("Invalid password")
            false
        } else {
            binding.textinputpassword.setError(null)
            true
        }
    }

    fun startAdmin() {
        Utils.setSharedPrefs(this@LoginActivity, Constants.KEY_IS_ADMIN, "true")
        Utils.setSharedPrefs(this@LoginActivity, Constants.KEY_USER_NAME, "Administrator")
        Utils.setSharedPrefs(this@LoginActivity, Constants.KEY_JWT_TOKEN, "local")
        Utils.setSharedPrefsBoolean(this@LoginActivity, Constants.KEY_IS_LOGGED_IN, true)
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        finish()
    }

    private fun validateInput(userId: String, password: String): String? {
        return when {
            userId.isEmpty() || password.isEmpty() -> "Please enter valid credentials"
            userId.length < 5 -> "Please enter at least 5 characters for the username"
            password.length < 6 -> "Please enter a password with more than 6 characters"
            else -> null
        }
    }

    private fun showErrorMessage(message: String) {
        Toasty.warning(this@LoginActivity, message, Toasty.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }
}