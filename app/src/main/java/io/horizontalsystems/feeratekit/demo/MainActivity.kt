package io.horizontalsystems.feeratekit.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    lateinit var ratesTextView: TextView
    private lateinit var ratesRefreshBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        ratesTextView = findViewById(R.id.feeRates)
        ratesRefreshBtn = findViewById(R.id.refreshButton)

        viewModel.feeRateData.observe(this, Observer { fees ->
            ratesTextView.text = fees
        })

        ratesRefreshBtn.setOnClickListener {
            ratesTextView.text = "Getting values ... "
            viewModel.refresh()
        }

    }

}
