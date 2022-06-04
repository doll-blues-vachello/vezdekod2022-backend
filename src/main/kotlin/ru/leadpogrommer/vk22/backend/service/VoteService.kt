package ru.leadpogrommer.vk22.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.annotation.PostConstruct

@Service
@Scope(SCOPE_SINGLETON)
class VoteService {
    @Value("\${vk22.artists}")
    private lateinit var artists: Array<String>
    private lateinit var artistsSet: Set<String>

    private val votes = ConcurrentHashMap<String, MutableList<Date>>()

    @PostConstruct
    fun init(){
        artistsSet = artists.toSet()
        artistsSet.forEach {
            votes[it] = mutableListOf()
        }
    }

    fun artistExists(artist: String) = artist in artistsSet


    fun createVote(user: ULong, artist: String){
        val date = Date()
        val artistList = votes[artist]!!
        synchronized(artistList){
            artistList.add(date)
        }
    }

    fun getVotes(): Map<String, ULong>{
        return votes.mapValues {
            synchronized(it.value){
                it.value.size.toULong()
            }
        }
    }

}

