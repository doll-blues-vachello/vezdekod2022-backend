package ru.leadpogrommer.vk22.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.Date


data class RateData(val limit: Int, val remaining: Int, val resetTime: Long, val success: Boolean)

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
class RateService {
    @Value("\${vk22.requestsPerPeriod}")
    val requestsPerPeriod: Int = 0
    @Value("\${vk22.ratePeriod}")
    val resetPeriod: Int = 0

    val users = mutableMapOf<ULong, Bucket>()

    // true - able to make request
    // false - ate limit exceeded
    fun makeRequest(user: ULong): RateData{
        synchronized(users){
            if(user !in users.keys){
                users[user] = Bucket(Date(), requestsPerPeriod - 1)
                return RateData(requestsPerPeriod, requestsPerPeriod - 1, Date().time/1000L + resetPeriod, true)
            }
            val bucket = users[user]!!
            if(Date().time/1000 - bucket.resetTime.time/1000 > resetPeriod){
                bucket.count = requestsPerPeriod
                bucket.resetTime = Date()
            }
            val success: Boolean
            if(bucket.count > 0){
                bucket.count--
                success = true
            }else{
                success = false
            }
            return RateData(requestsPerPeriod, bucket.count, bucket.resetTime.time/1000 + resetPeriod, success)
        }
    }


    class Bucket(var resetTime: Date, var count: Int)
}