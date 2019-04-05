package io.horizontalsystems.feeratekit.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders


class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    lateinit var ratesTextView: TextView
    lateinit var ratesRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        ratesTextView = findViewById(R.id.feeRates)
        ratesRefresh = findViewById(R.id.refreshButton)

        viewModel.rates.observe(this, Observer { rates ->
            ratesTextView.text = rates.joinToString("\n\n") { rate ->
                "Coin: ${rate.coin.code}\n" +
                "Low: ${rate.lowPriority}\n" +
                "Medium: ${rate.mediumPriority}\n" +
                "High: ${rate.highPriority}"
            }
        })

        ratesRefresh.setOnClickListener {
            viewModel.refresh()
        }
    }
}
