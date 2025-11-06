package jp.trap.mikke.features.ping.service

import jp.trap.mikke.features.ping.domain.model.PingResult
import org.koin.core.annotation.Single

@Single
class PingService {
    fun ping(): PingResult = PingResult("pong!")
}
