package ru.leadpogrommer.vk22.shared.dto

data class PostVoteRequestDto(
    val phone: String,
    val artist: String,
){

}

data class GetVotesResponseDto(val data: List<Inner>){
    constructor(artistData: Map<String, ULong>) : this(artistData.map { Inner(it.key, it.value) }.toList()) {
    }

    constructor(): this(emptyList())
    data class Inner(val name: String, val votes: ULong){
        constructor(): this("", 0U)
    }
}

data class GetVotesStatsDto(val data: List<IntervalDto>){
    constructor(): this(emptyList())
}