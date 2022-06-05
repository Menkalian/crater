package de.menkalian.crater.server.database.telemetrie

import de.menkalian.crater.data.telemetrie.LogReport
import de.menkalian.crater.data.telemetrie.TelemetrieReport
import de.menkalian.crater.server.database.IDatabase

/**
 * write-only database with telemetry data
 */
interface ITelemetrieDatabase : IDatabase {
    val uuid: String

    // CREATE
    fun saveTelemetrieReport(report: TelemetrieReport)

    // UPDATE
    fun setReporterName(name: String)
    fun setReporterEmail(email: String)
    fun setReportText(text: String)

    fun addLogReport(report: LogReport)
}