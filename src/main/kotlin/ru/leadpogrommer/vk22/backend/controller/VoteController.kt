package ru.leadpogrommer.vk22.backend.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import ru.leadpogrommer.vk22.backend.dto.GetVotesResponseDto
import ru.leadpogrommer.vk22.backend.dto.PostVoteRequestDto
import ru.leadpogrommer.vk22.backend.service.RateService
import ru.leadpogrommer.vk22.backend.service.VoteService
import java.util.*


@RestController
@RequestMapping("/")
class VoteController(
    val voteService: VoteService,
    val rateService: RateService
    ) {
    val phoneRegex = Regex("9\\d{9}")

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

        if(rateData.success){
            voteService.createVote(phone, req.artist)
            return ResponseEntity(responseHeaders, HttpStatus.CREATED)
        }else{
            return ResponseEntity(responseHeaders, HttpStatus.TOO_MANY_REQUESTS)
        }
    }

    @GetMapping("/votes")
    fun getVotes(): GetVotesResponseDto{
        return GetVotesResponseDto(voteService.getVotes())
    }

    fun convertPhone(phone: String): ULong?{
        phoneRegex.matchEntire(phone) ?: return null
        return phone.toULong()
    }
}