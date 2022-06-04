package ru.leadpogrommer.vk22.backend.dto

class PostVoteRequestDto(
    val phone: String,
    val artist: String,
)

class GetVotesResponseDto(artistData: Map<String, ULong>){
    data class Inner(val name: String, val votes: ULong)
    val data = artistData.map { Inner(it.key, it.value) }.toList()
}