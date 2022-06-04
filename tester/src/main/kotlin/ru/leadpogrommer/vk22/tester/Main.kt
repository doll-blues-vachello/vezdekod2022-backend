package ru.leadpogrommer.vk22.tester

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue
import kotlin.math.ceil

data class RequestResult(val code: Int, val time: Double)

@OptIn(DelicateCoroutinesApi::class)
fun main(args: Array<String>){
    val parser = ArgParser("vk22 tester")
    val totalRequestCount by parser.option(ArgType.Int, shortName = "n").required()
    val clientCount by parser.option(ArgType.Int, shortName = "c").required()
    val artistsString by parser.option(ArgType.String, shortName = "a").required()
    val baseUrl by parser.argument(ArgType.String)

    parser.parse(args)

    println("Creating $totalRequestCount as $clientCount clients")
    val numThreads = Runtime.getRuntime().availableProcessors()
    println("Using $numThreads threads")
    val coroutineContext = newFixedThreadPoolContext(numThreads, "Request pool")
    val artists = artistsString.splitToSequence(',').toList()




    val requestsLeft = AtomicInteger(totalRequestCount)
    val statuses = mutableListOf<RequestResult>()
    val endMutex = Mutex()
    val jobs = mutableListOf <Job>()

    val globalStartTime = Date()
    for(i in 0 until clientCount){
        val job = GlobalScope.launch (coroutineContext){
            val random = Random()
            val phone = (random.nextInt().absoluteValue % 10000).toString().padStart(10, '9')
            val results = mutableListOf<RequestResult>()
            val client = HttpClient(CIO){
                install(ContentNegotiation){
                    json()
                }

            }

            while (requestsLeft.getAndDecrement() > 0){
                val startTime = Date()
                val response = client.request {
                    url(baseUrl.trimEnd('/') + "/votes")
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("phone" to phone, "artist" to artists[random.nextInt().absoluteValue % artists.size]))
                }
                val stopTime = Date()
                results.add(RequestResult(response.status.value, (stopTime.time - startTime.time).toDouble() / 1000))
            }
            endMutex.withLock {
                statuses.addAll(0, results)
            }
        }
        jobs.add(job)
    }
    runBlocking {
        jobs.joinAll()
    }
    val globalStopTime = Date()

    analyzeResults(statuses, globalStartTime.time/1000.0, globalStopTime.time/1000.0)
}

fun analyzeResults(results: List<RequestResult>, start: Double, stop: Double){
    val times = results.map { it.time }.sorted()
    val codes = results.map { it.code }

    println("[Summary]")
    println("Total: ${stop - start} secs")
    println("Slowest: ${times.maxOf { it }} secs")
    println("Fastest: ${times.minOf { it }} secs")
    println("Average: ${times.average()} secs ")
    println("Requests/sec: ${results.size / (stop - start)} secs")

    println("[Latency distribution]")
    for(p in arrayOf(10, 25, 50, 75, 90, 95, 99)){
        printPercentile(times, p.toDouble())
    }

    println("[Status code distribution]")

    val allCodes = codes.toSet().toList().sorted()
    allCodes.forEach{code ->
        println("[$code] ${codes.filter { code == it }.count()} responses")
    }


}

fun printPercentile(times: List<Double>, percentile: Double){
    val index = ceil(percentile / 100.0 * times.size).toInt()
    println("$percentile% in ${times[index - 1]} secs")

}

