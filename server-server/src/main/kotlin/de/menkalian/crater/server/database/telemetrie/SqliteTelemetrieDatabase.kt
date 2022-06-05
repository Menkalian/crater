package de.menkalian.crater.server.database.telemetrie

import de.menkalian.crater.data.telemetrie.LogReport
import de.menkalian.crater.data.telemetrie.TelemetrieReport
import de.menkalian.crater.server.database.shared.MetaDataAwareDatabaseExtension
import de.menkalian.crater.server.database.telemetrie.dao.LogData
import de.menkalian.crater.variables.Crater
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.info.BuildProperties

class SqliteTelemetrieDatabase(override val uuid: String, build: BuildProperties, dbFilePath: String) : ITelemetrieDatabase {
    companion object {
        private const val DATABASE_SCHEMA_VERSION = 1
    }

    override var isOpen: Boolean = false
    override val dbConnection: Database
    private val metadataExtension = MetaDataAwareDatabaseExtension()

    init {
        dbConnection = Database.connect("jdbc:sqlite:$dbFilePath", driver = "org.sqlite.JDBC")
        isOpen = true

        metadataExtension.initMetadata(this, build, DATABASE_SCHEMA_VERSION, "Telemetrie")
    }

    override fun saveTelemetrieReport(report: TelemetrieReport) {
        report.reporterName?.let { setReporterName(it) }
        report.reporterEmail?.let { setReporterEmail(it) }
        report.reportText?.let { setReportText(it) }
        report.logs?.forEach {
            addLogReport(it)
        }
    }

    override fun setReporterName(name: String) {
        ensureOpen()
        metadataExtension.upsertMetadata(
            dbConnection, mapOf(
                Crater.Telemetrie.Report.Author.Name to name
            )
        )
    }

    override fun setReporterEmail(email: String) {
        ensureOpen()
        metadataExtension.upsertMetadata(
            dbConnection, mapOf(
                Crater.Telemetrie.Report.Author.Email to email
            )
        )
    }

    override fun setReportText(text: String) {
        ensureOpen()
        metadataExtension.upsertMetadata(
            dbConnection, mapOf(
                Crater.Telemetrie.Report.Message to text
            )
        )
    }

    override fun addLogReport(report: LogReport) {
        ensureOpen()
        transaction(dbConnection) {
            SchemaUtils.create(LogData)

            LogData.LogDataEntry.new {
                file = report.logFileName
                domain = report.logDomain
                date = report.date
                data = report.logData
            }
        }
    }

    override fun close() {
        isOpen = false
        TransactionManager.closeAndUnregister(dbConnection)
    }
}