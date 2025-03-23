package com.elaine.countdowntimer

import android.os.Handler
import android.os.Looper
import android.widget.TextView

class CountdownTimer(

    private val textView: TextView,

    private val startTimeInMillis: Long

) {

    private var timeLeftInMillis = startTimeInMillis

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var runnable: Runnable

    private var isTimerRunning = false


    fun startTimer() {

        if (isTimerRunning) return

        isTimerRunning = true


        runnable = object : Runnable {

            override fun run() {

                if (timeLeftInMillis > 0) {

                    timeLeftInMillis -= 1000

                    updateUIWithTimeLeft(timeLeftInMillis)

                    handler.postDelayed(this, 1000)

                } else {

                    onFinish()

                }

            }

        }

        handler.post(runnable)

    }


    fun pauseTimer() {

        if (!isTimerRunning) return

        handler.removeCallbacks(runnable)

        isTimerRunning = false

    }


    fun resetTimer() {

        pauseTimer() // Stop the current timer

        timeLeftInMillis = startTimeInMillis // Reset time to the original start time

        updateUIWithTimeLeft(timeLeftInMillis) // Update UI with reset time

    }


    private fun onFinish() {

        textView.text = "Time's up!"

        isTimerRunning = false

    }


    private fun updateUIWithTimeLeft(timeLeft: Long) {

        val secondsRemaining = timeLeft / 1000

        textView.text = "$secondsRemaining seconds remaining"

    }

}