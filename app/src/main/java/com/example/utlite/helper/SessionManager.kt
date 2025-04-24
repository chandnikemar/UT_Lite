package com.example.utlite.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Html
import androidx.appcompat.app.AlertDialog
import com.example.utlite.helper.Constants.KEY_HTTP
import com.example.utlite.helper.Constants.KEY_JWT_TOKEN
import com.example.utlite.helper.Constants.KEY_REFRESH_TOKEN
import com.example.utlite.helper.Constants.KEY_SERVER_IP
import com.example.utlite.helper.Constants.KEY_USER_EMAIL
import com.example.utlite.helper.Constants.KEY_USER_FIRST_NAME
import com.example.utlite.helper.Constants.KEY_USER_ID
import com.example.utlite.helper.Constants.KEY_USER_IS_VERIFIED
import com.example.utlite.helper.Constants.KEY_USER_LAST_NAME
import com.example.utlite.helper.Constants.KEY_USER_MOBILE_NUMBER
import com.example.utlite.helper.Constants.KEY_USER_NAME
import com.example.utlite.helper.Constants.ROLE_NAME
import com.example.utlite.view.LoginActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SessionManager(context: Context) {
    // Shared Preferences
    var sharedPrefer: SharedPreferences

    // Editor for Shared preferences
    var editor: SharedPreferences.Editor

    // Context
    var context: Context

    // Shared Pref mode
    var PRIVATE_MODE = 0

    // Constructor
    init {
        this.context = context
        sharedPrefer = context.getSharedPreferences(Constants.SHARED_PREF, PRIVATE_MODE)
        editor = sharedPrefer.edit()
    }

    fun getBaseUrl(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user["baseUrl"] = sharedPrefer.getString(Constants.SETTINGS_BASE_URL, null)
        return user
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user["userName"] = sharedPrefer.getString(KEY_USER_NAME, null)
        user["jwtToken"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        user["refreshToken"] = sharedPrefer.getString(KEY_REFRESH_TOKEN, null)
        user[KEY_USER_ID] = sharedPrefer.getString(KEY_USER_ID, null)
        user[ROLE_NAME] = sharedPrefer.getString(ROLE_NAME, null)
        user[KEY_SERVER_IP] = sharedPrefer.getString(KEY_SERVER_IP, null)
        user[KEY_HTTP] = sharedPrefer.getString(KEY_HTTP, null)
        return user
    }

    fun createLoginSession(
        firstName: String?,
        lastName: String?,
        email: String?,
        mobileNumber: String?,
        isVerified: String?,
        userName: String?,
        jwtToken: String?,
        refreshToken: String?,
        roleName:String?,
    ) {

        //editor.putString(KEY_USERID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_FIRST_NAME, firstName)
        editor.putString(KEY_USER_LAST_NAME, lastName)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_MOBILE_NUMBER, mobileNumber)
        editor.putString(KEY_USER_IS_VERIFIED, isVerified)



        //editor.putString(KEY_RDT_ID, rdtId)
        //editor.putString(KEY_TERMINAL, terminal)
        //editor.putBoolean(KEY_ISLOGGEDIN, true)
        editor.putString(KEY_JWT_TOKEN, jwtToken)
        editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        editor.putString(ROLE_NAME, roleName)


        // commit changes
        editor.commit()
    }

    fun logoutUser() {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, false)
        editor.commit()
    }
    fun showToastAndHandleErrors(resultResponse: String,context: Activity) {

        when (resultResponse) {
            "Unauthorized", "Authentication token expired", Constants.CONFIG_ERROR -> {
                showCustomDialog(
                    "Session Expired",
                    "Please re-login to continue",
                    context
                )
            }
        }
    }
    fun showCustomDialog(title: String?, message: String?,context: Activity) {
        var alertDialog: AlertDialog? = null
        val builder: AlertDialog.Builder
        if (title.equals(""))
            builder = AlertDialog.Builder(context)
                .setMessage(Html.fromHtml(message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else if (message.equals(""))
            builder = AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else
            builder = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    if (title.equals("Session Expired")) {
                        logout(context)
                    } else {
                        alertDialog?.dismiss()
                    }
                }
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    private fun logout(context: Activity) {
        logoutUser()
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        context.finish()
    }
    /**
     * Call this method on/after login to store the details in session
     */
    /*fun createLoginSession(
        userId: String?,
        userName: String?,
        rdtId: String?,
        terminal: String?,
        jwtToken: String?,
        refreshToken: String?
    ) {

        editor.putString(Constants.USER_ID, userId)
        editor.putString(Constants.USERNAME, userName)
        editor.putString(Constants.RDT_ID, rdtId)
        editor.putString(Constants.KEY_TERMINAL, terminal)
        //editor.putBoolean(Constants.KEY_ISLOGGEDIN, true)
        editor.putString(Constants.KEY_JWT_TOKEN, jwtToken)
        editor.putString(Constants.KEY_REFRESH_TOKEN, refreshToken)

        // commit changes
        editor.commit()
    }

    fun logoutUser() {
        editor.putBoolean(Constants.KEY_ISLOGGEDIN, false)
        editor.commit()
    }*/

    /**
     * Call this method anywhere in the project to Get the stored session data
     */
 /*   fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user["userId"] = sharedPrefer.getString(Constants.USER_ID, null)
        user["userName"] = sharedPrefer.getString(Constants.USERNAME, null)
        user["rdtId"] = sharedPrefer.getString(Constants.RDT_ID, null)
        user["terminal"] = sharedPrefer.getString(Constants.KEY_TERMINAL, null)
        user["jwtToken"] = sharedPrefer.getString(Constants.KEY_JWT_TOKEN, null)
        user["refreshToken"] = sharedPrefer.getString(Constants.KEY_REFRESH_TOKEN, null)
        return user
    }

    fun isAlreadyLoggedIn(): HashMap<String, Boolean> {
        val user = HashMap<String, Boolean>()
        user["isLoggedIn"] = sharedPrefer.getBoolean(Constants.KEY_ISLOGGEDIN, false)
        return user
    }

    fun getAdminDetails(): HashMap<String, String?> {
        val admin = HashMap<String, String?>()
        admin["serverIp"] = sharedPrefer.getString(Constants.SERVER_IP, null)
        admin["port"] = sharedPrefer.getString(Constants.PORT, null)
        return admin
    }
    fun getHeaderDetails(): HashMap<String, String?> {
        val user_header = HashMap<String, String?>()
        user_header["UserId"] = sharedPrefer.getString(Constants.USER_ID, null)
        user_header["RDTId"] = sharedPrefer.getString(Constants.RDT_ID, null)
        user_header["TerminalId"] = sharedPrefer.getString(Constants.KEY_TERMINAL, null)
        user_header["Token"] = sharedPrefer.getString(Constants.KEY_JWT_TOKEN, null)
        return user_header
    }

    fun saveAdminDetails(serverIp: String?, portNumber: String?) {
        editor.putString(Constants.SERVER_IP, serverIp)
        editor.putString(Constants.PORT, portNumber)
        editor.putBoolean(Constants.KEY_ISLOGGEDIN, false)
        editor.commit()
    }*/

    fun clearSharedPrefs() {
        editor.clear()
        editor.commit()
    }
    fun setSharedPrefsBoolean(context: Context, key: String?, value: Boolean) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
    fun setSharedPrefsString(context: Context, key: String?, value: String) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }
}