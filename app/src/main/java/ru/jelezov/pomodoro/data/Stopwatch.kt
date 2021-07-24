package ru.jelezov.pomodoro.data

data class Stopwatch (
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    var countMs: Long
    )