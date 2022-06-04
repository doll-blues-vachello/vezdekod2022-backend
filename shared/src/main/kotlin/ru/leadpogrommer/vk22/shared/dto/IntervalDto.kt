package ru.leadpogrommer.vk22.shared.dto

data class IntervalDto(

    val start: Long,
    val end: Long,
    val votes: Int
){
    constructor(): this(0L, 0L, 0)
}