package ru.leadpogrommer.vk22.balancer.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.json.JacksonJsonParser
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.client.toEntity
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import ru.leadpogrommer.vk22.balancer.service.VoteTimeService
import ru.leadpogrommer.vk22.shared.dto.GetVotesResponseDto
import ru.leadpogrommer.vk22.shared.dto.GetVotesStatsDto
import ru.leadpogrommer.vk22.shared.dto.IntervalDto
import ru.leadpogrommer.vk22.shared.dto.PostVoteRequestDto
import java.util.*
import java.util.zip.CRC32
import kotlin.math.absoluteValue


@RestController
@RequestMapping("/")
class BalancerController(val voteTime: VoteTimeService){
    @Value("\${vk22.balancer.shards}")
    val shards: Array<String> = emptyArray()

    fun clientForShard(shard: String) = WebClient.builder()
        .baseUrl(shard)
        .build()

    @PostMapping("/votes")
    fun postVote(@RequestBody req: PostVoteRequestDto, ): ResponseEntity<Unit> {
        val shardNumber  = CRC32().run {
            update(req.phone.toByteArray())
            value.toInt().absoluteValue
        } % shards.size
        val startDate = Date()
        val client = clientForShard(shards[shardNumber])
        val response = client.post()
            .uri("/votes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(req))
            .retrieve()
            .onStatus({true}, { it->Mono.empty()})
            .toEntity<Unit>()
            .block() ?: throw ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT)

        if(response.statusCode == HttpStatus.CREATED){
            if(voteTime.firstVoteTime == 0L){
                voteTime.firstVoteTime = startDate.time / 1000
            }
            voteTime.lastVoteTime = Date().time / 1000 + 1
        }
        return response
    }

    @GetMapping("/votes")
    fun getVotes(): GetVotesResponseDto {
        val response = shards.map {shard->
            clientForShard(shard).get()
                .uri("/votes")
                .retrieve()
                .toEntity<GetVotesResponseDto>()
                .block() ?: throw throw ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT)
        }.fold(mutableMapOf<String, ULong>()){ acc, item ->
            item.body!!.data.map {
                acc[it.name] = (acc[it.name]?:0U) + it.votes
            }
            acc
        }
        return GetVotesResponseDto(response)


    }


    @GetMapping("/votes/stats")
    fun getVotesStats(
        @RequestParam(name = "from", required = false) _start: Long?,
        @RequestParam(name = "to", required = false) _end: Long?,
        @RequestParam(name = "intervals", required = false) _intervalsCount: Int?,
        @RequestParam(name = "artists", required = false) _artists: Array<String>?
    ): GetVotesStatsDto {
        val start = _start ?: voteTime.firstVoteTime
        val end = _end ?: voteTime.lastVoteTime

        val responses = shards.map { shard ->
            clientForShard(shard).get()
                .uri { ub ->
                    ub.path("/votes/stats")
                    ub.queryParam("from", start)
                    ub.queryParam("to", end)
                    _intervalsCount?.let { ub.queryParam("intervals", _intervalsCount) }
                    _artists?.let { ub.queryParam("artists", _artists.joinToString(",")) }
                    ub.build()
                }
                .retrieve()
                .toEntity<GetVotesStatsDto>()
                .block() ?: throw ResponseStatusException(HttpStatus.BAD_GATEWAY)
        }.toList()
        val result = responses[0].body!!.data.map {
            Pair(it.start, it.end) to 0
        }.toMap().toMutableMap()

        for (response in responses){
            for(interval in response.body!!.data){
                val key = Pair(interval.start, interval.end)
                result[key] = result[key]!! + interval.votes
            }
        }

        val intervals = result.map {
            IntervalDto(it.key.first, it.key.second, it.value)
        }

        return GetVotesStatsDto(intervals)

    }

}