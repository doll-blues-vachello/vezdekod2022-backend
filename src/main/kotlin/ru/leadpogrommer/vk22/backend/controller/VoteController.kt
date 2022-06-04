package ru.leadpogrommer.vk22.backend.controller

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
import ru.leadpogrommer.vk22.backend.service.VoteService


@RestController
@RequestMapping("/")
class VoteController(val voteService: VoteService) {
    val phoneRegex = Regex("9\\d{9}")



    @PostMapping("/votes")
    fun postVote(@RequestBody req: PostVoteRequestDto): ResponseEntity<Unit>{
        val phone = convertPhone(req.phone) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone format")
        if(!voteService.artistExists(req.artist)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found")
        voteService.createVote(phone, req.artist)
        return ResponseEntity(HttpStatus.CREATED)
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