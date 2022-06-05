package de.menkalian.crater.server.database

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

object DatabaseHelper {
    fun createDatabase(dbName: String, host: String, port: String, uname: String, pword: String) {
        val url = "jdbc:postgresql://$host:$port/"
        val props = Properties()
        props.setProperty("user", uname)
        props.setProperty("password", pword)
        val conn: Connection = DriverManager.getConnection(url, props)

        val existsQuery = conn
            .prepareStatement("SELECT COUNT(*) AS count FROM pg_database WHERE datname='$dbName'")
            .executeQuery()

        if (existsQuery.next() && existsQuery.getInt("count") >= 0) {
            conn.close()
            return
        }

        conn.prepareCall("CREATE DATABASE $dbName;").execute()
        conn.close()
    }
}