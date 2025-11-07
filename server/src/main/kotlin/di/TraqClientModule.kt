package jp.trap.mikke.di

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
object TraqClientModule {
    private val client =
        HttpClient(Apache) {
            install(ContentNegotiation) {
                json()
            }
        }

    @Single(createdAtStart = true)
    fun provideHttpClient(): HttpClient = client
}
