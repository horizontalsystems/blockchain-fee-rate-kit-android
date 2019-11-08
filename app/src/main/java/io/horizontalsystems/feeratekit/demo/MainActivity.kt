package io.horizontalsystems.feeratekit.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.feeratekit.model.FeeRate

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    lateinit var ratesTextView: TextView
    private lateinit var ratesRefreshBtn: Button
    private lateinit var statusInfoBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        ratesTextView = findViewById(R.id.feeRates)
        ratesRefreshBtn = findViewById(R.id.refreshButton)
        statusInfoBtn = findViewById(R.id.statusInfoButton)


        viewModel.feeRateData.observe(this, Observer { rate ->
            ratesTextView.text = "${ratesTextView.text}\n\n${getText(rate)}"
        })

        ratesRefreshBtn.setOnClickListener {

            ratesTextView.text = "Getting values ... "
            viewModel.refresh()
        }

        statusInfoBtn.setOnClickListener {

            ratesTextView.text = "Getting values ... "
            viewModel.getStatusInfo()
        }
    }

    private fun getText(data: Any): String {

        if (data::class == FeeRate::class) {

            val rate = data as FeeRate

            return "Coin: ${rate.coin.code}\n" +
                    "Low: rate=${rate.lowPriority}, duration=${rate.lowPriorityDuration / 60} minutes\n" +
                    "Medium: rate=${rate.mediumPriority}, duration=${rate.mediumPriorityDuration / 60} minutes\n" +
                    "High: rate=${rate.highPriority}, duration=${rate.highPriorityDuration / 60} minutes"

        } else {

            return data.toString()
        }
    }
}
