package de.menkalian.crater.server.service

import de.menkalian.crater.server.database.task.ITaskDatabase
import de.menkalian.crater.server.util.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ScheduledVersionUpdateService(
    private val taskDatabase: ITaskDatabase
) {
    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    fun updateDatabaseVersion() {
        logger().error("Updating content version in database")
        taskDatabase.updateVersion()
    }
}