package hds.db

import java.sql.DriverManager
import java.sql.Connection

fun connection(): Connection {
    val url = if ("true" == System.getProperty("production")) {
        "jdbc:postgresql://postgres-atmoresume.jelasticloud.com/hds"
    } else {
        "jdbc:postgresql://localhost:5432/hds"
    }
    return DriverManager.getConnection(url, System.getProperty("db.user", "hds")!!, System.getProperty("db.pass", "hds")!!)
}