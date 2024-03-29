package ru.leadpogrommer.vk22.backend.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import ru.leadpogrommer.vk22.shared.dto.GetVotesResponseDto
import ru.leadpogrommer.vk22.shared.dto.PostVoteRequestDto
import ru.leadpogrommer.vk22.backend.service.RateService
import ru.leadpogrommer.vk22.backend.service.VoteService
import ru.leadpogrommer.vk22.shared.dto.GetVotesStatsDto
import java.util.*
import java.util.logging.Logger


@RestController
@RequestMapping("/")
class VoteController(
    val voteService: VoteService,
    val rateService: RateService
    ) {
    private val phoneRegex = Regex("9\\d{9}")
    private val log = Logger.getLogger("VOTES")

    @PostMapping("/votes")
    fun postVote(@RequestBody req: PostVoteRequestDto,): ResponseEntity<Unit>{
        val phone = convertPhone(req.phone) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone format")
        if(!voteService.artistExists(req.artist)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found")

        val rateData = rateService.makeRequest(phone)
        val responseHeaders = HttpHeaders()
        responseHeaders.apply {
            set("x-ratelimit-limit", "${rateData.limit}")
            set("x-ratelimit-remaining", rateData.remaining.toString())
            set("x-ratelimit-reset", rateData.resetTime.toString())
            if(!rateData.success)set("Retry-After", "${rateData.resetTime - Date().time/1000}")
        }

        log.info("Voting for ${req.artist}")

        if(rateData.success){
            voteService.createVote(phone, req.artist)
            return ResponseEntity(responseHeaders, HttpStatus.CREATED)
        }else{
            return ResponseEntity(responseHeaders, HttpStatus.TOO_MANY_REQUESTS)
        }
    }

    @GetMapping("/votes")
    fun getVotes(): GetVotesResponseDto{
        log.info("Getting votes")
        return GetVotesResponseDto(voteService.getVotes())
    }

    @GetMapping("/votes/stats")
    fun getVotesStats(
        @RequestParam(name = "from", required = false) _start: Long?,
        @RequestParam(name = "to", required = false) _end: Long?,
        @RequestParam(name = "intervals", required = false) _intervalsCount: Int?,
        @RequestParam(name = "artists", required = false) _artists: Array<String>?
    ): GetVotesStatsDto{
        val start = _start ?: voteService.firstVoteTime
        val end = _end ?: (voteService.lastVoteTime)
        val intervalsCount = _intervalsCount ?: 10
        val artists = _artists?.toSet() ?: voteService.artists

        log.info("Getting stats: from=$start, end=$end, intervals=$intervalsCount, artists=$artists")

        val resp = voteService.getVotesByIntervals(start, end, intervalsCount, artists)

        return GetVotesStatsDto(resp)

    }

    fun convertPhone(phone: String): ULong?{
        phoneRegex.matchEntire(phone) ?: return null
        return phone.toULong()
    }
}