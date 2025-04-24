package com.example.utlite.view

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.utlite.R
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class QrcodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView  // Declare the scanner view
    private lateinit var qrCodeResult: EditText  // Reference to the EditText for showing result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)  // Use your layout XML file

        // Initialize EditText for displaying the QR code result
        qrCodeResult = findViewById(R.id.qrCodeResult)

        // Initialize the scanner view programmatically
        mScannerView = ZXingScannerView(this)
        // Assuming you have a layout container for the scanner view
        val container = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container)
        container.addView(mScannerView)

        // Set the result handler to this activity
        mScannerView.setResultHandler(this)
    }

    override fun onResume() {
        super.onResume()
        mScannerView.startCamera()  // Start the camera when the activity resumes
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()  // Stop the camera when the activity is paused
    }

    override fun handleResult(result: Result) {
        // Display the scanned result in the EditText field
        qrCodeResult.setText(result.text)

        // Optionally, you can show a Toast with the result or stop the scanner
        Toast.makeText(this, "Scanned: ${result.text}", Toast.LENGTH_LONG).show()

        // Stop the scanner after scanning
        mScannerView.stopCamera()

        // Optionally, you can restart the scanner after a short delay
        // mScannerView.resumeCameraPreview(this)
    }
}
