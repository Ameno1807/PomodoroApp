package ru.jelezov.pomodoro.ui.viewHolder

import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.jelezov.pomodoro.R
import ru.jelezov.pomodoro.data.Stopwatch
import ru.jelezov.pomodoro.databinding.ItemListBinding
import ru.jelezov.pomodoro.interfaces.StopwatchListener
import ru.jelezov.pomodoro.utils.displayTime

class StopwatchViewHolder(
    private val binding: ItemListBinding,
    private val listener: StopwatchListener
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        if (stopwatch.currentMs == -1L) {
            binding.consMain.setBackgroundColor(ContextCompat.getColor(binding.consMain.context,
                R.color.red_light
            ))
            binding.deleteButton.setBackgroundColor(ContextCompat.getColor(binding.consMain.context,
                R.color.red_light
            ))
        } else  binding.consMain.setBackgroundColor(ContextCompat.getColor(binding.consMain.context,
            R.color.white
        ))

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer()
        }
        binding.customView.setPeriod(stopwatch.countMs)
        binding.customView.setCurrent(stopwatch.countMs - stopwatch.currentMs)
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.button.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }
        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.button.text = "Stop"
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.button.text = "Start"
        timer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.customView.setCurrent(stopwatch.countMs - stopwatch.currentMs)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

               if(stopwatch.currentMs <= 0L) {
                    stopTimer()
                    binding.customView.setCurrent(0L)
                    stopwatch.currentMs = 0
                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                    stopwatch.currentMs = -1L
                    binding.consMain.setBackgroundColor(ContextCompat.getColor(binding.consMain.context,
                        R.color.red_light
                    ))
                    binding.deleteButton.setBackgroundColor(ContextCompat.getColor(binding.consMain.context,
                       R.color.red_light
                   ))
                }
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }
        }
    }

    private companion object {
        private const val UNIT_TEN_MS = 100L
        private const val PERIOD  = 1000L * 60L * 60L * 24L // Day
    }

}
