package com.elaine.countdowntimer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {


    private lateinit var countdownTimer: CountdownTimer


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        val timerTextView: TextView = findViewById(R.id.timer_text_view)

        val startButton: Button = findViewById(R.id.start_button)

        val pauseButton: Button = findViewById(R.id.pause_button)

        val resetButton: Button = findViewById(R.id.reset_button)


        countdownTimer = CountdownTimer(timerTextView, 30000) // 30 seconds timer


        startButton.setOnClickListener {

            countdownTimer.startTimer()

        }


        pauseButton.setOnClickListener {

            countdownTimer.pauseTimer()

        }


        resetButton.setOnClickListener {

            countdownTimer.resetTimer()

        }

    }

}