package ru.leadpogrommer.vk22.balancer.service

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
class VoteTimeService {
    var lastVoteTime = 0L
    var firstVoteTime = 0L
}