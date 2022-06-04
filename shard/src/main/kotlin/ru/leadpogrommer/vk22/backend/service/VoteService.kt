package ru.leadpogrommer.vk22.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import ru.leadpogrommer.vk22.shared.dto.IntervalDto
import java.lang.Math.min
//import java.lang.I
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Service
@Scope(SCOPE_SINGLETON)
class VoteService {
    @Value("\${vk22.artists}")
    private lateinit var artistsTmp: Array<String>

    var firstVoteTime = 0L
        protected set
    var lastVoteTime = 0L
        protected set

    lateinit var artists: Set<String>
        protected set


    private val votes = ConcurrentHashMap<String, MutableList<Date>>()

    @PostConstruct
    fun init(){
        artists = artistsTmp.toSet()
        artists.forEach {
            votes[it] = mutableListOf()
        }
    }

    fun artistExists(artist: String) = artist in artists


    fun createVote(user: ULong, artist: String){
        val date = Date()
        if(firstVoteTime == 0L){
            firstVoteTime = date.time/1000L
        }
        lastVoteTime = date.time/1000L + 1
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



    fun getVotesByIntervals(start: Long, end: Long, intervalsCount: Int, artists: Set<String>):List<IntervalDto>{
        val maxPossibleIntervals = end - start
        val finalIntervalsCount = min(maxPossibleIntervals.toInt(), intervalsCount)

        val delta = (end - start).toDouble() / finalIntervalsCount
        val intervals = (0 until finalIntervalsCount).map {
            (start + (delta*it).toInt() until start + (delta*(it+1)).toInt())

        }.toList()

        return intervals.map {interval ->
            val votesCount = this.votes.filter { it.key in artists }.map {
                synchronized(it.value){
                    it.value.filter {date ->
                        date.time/1000L in interval
                    }.count()
                }
            }.sum()
            IntervalDto(interval.first, interval.last+1, votesCount)
        }.toList()

    }

}

