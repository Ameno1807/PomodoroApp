package ru.jelezov.pomodoro.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import ru.jelezov.pomodoro.customView.MyTimePicker
import ru.jelezov.pomodoro.data.Stopwatch
import ru.jelezov.pomodoro.databinding.ActivityMainBinding
import ru.jelezov.pomodoro.interfaces.StopwatchListener
import ru.jelezov.pomodoro.ui.adapter.StopwatchAdapter
import ru.jelezov.pomodoro.ui.foreground.ForegroundService
import ru.jelezov.pomodoro.utils.COMMAND_ID
import ru.jelezov.pomodoro.utils.COMMAND_START
import ru.jelezov.pomodoro.utils.COMMAND_STOP
import ru.jelezov.pomodoro.utils.STARTED_TIMER_TIME_MS


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var timerState = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter

        }

        binding.floatingActionButton.setOnClickListener {
           if (stopwatches.size < 14) {
               addTimer()
           } else Toast.makeText(applicationContext, "Слишком много таймеров", Toast.LENGTH_SHORT).show()
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (timerState != -1 && stopwatches.size != 0) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            val currentTimer = stopwatches.find { it.id == timerState }
            startIntent.putExtra(STARTED_TIMER_TIME_MS, currentTimer?.currentMs ?: 9999999)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun start(id: Int) {
        if(timerState != -1) {
            val oldTimer = stopwatches.find { it.id == timerState }
            changeStopwatch(timerState, oldTimer?.currentMs ?: 0, false)
        }
        changeStopwatch(id, null, true)
        timerState = id
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
        timerState = -1
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, isStarted, it.countMs))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)

    }


    private fun addTimer() {
        val timePicker = MyTimePicker()
        timePicker.setTitle("Select time")
        timePicker.setOnTimeSetOption("Set time") {hour, minute, second ->
            var timeSeconds = second + minute * 60L + hour * 3600L
            timeSeconds *= 1000L
            Log.e("Tag", "Time -> $timeSeconds")
            stopwatches.add(Stopwatch(nextId++, timeSeconds, false, timeSeconds))
            stopwatchAdapter.submitList(stopwatches.toList())
        }
        timePicker.show(supportFragmentManager, "time_picker")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun onDestroy() {
        super.onDestroy()
    }
}