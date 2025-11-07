package jp.trap.mikke.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jp.trap.mikke.config.Environment
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import javax.sql.DataSource

@Module
object DatabaseModule {
    @Single(createdAtStart = true)
    fun provideDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = Environment.DB_URL
        config.username = Environment.DB_USER
        config.password = Environment.DB_PASS
        config.driverClassName = "org.mariadb.jdbc.Driver"
        config.maximumPoolSize = 10
        config.validate()

        val dataSource = HikariDataSource(config)

        try {
            Flyway
                .configure()
                .dataSource(dataSource)
                .load()
                .migrate()
        } catch (e: Exception) {
            throw RuntimeException("Failed to migrate database", e)
        }

        Database.connect(dataSource)

        return dataSource
    }
}
