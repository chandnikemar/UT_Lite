package com.example.utlite.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.example.utlite.R

import com.example.utlite.databinding.ActivityTppageBinding

class TppageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTppageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tppage)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tppage)

        setSupportActionBar(binding.TPToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.odishaButton.setOnClickListener {
            navigateTo(OdishaActivity::class.java)
        }
        binding.chhattisgarhButton.setOnClickListener {
            navigateTo(ChhattishgharActivity::class.java)
        }


    }
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
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
}