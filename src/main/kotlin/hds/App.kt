package hds

import com.vaadin.ui.UI
import com.vaadin.server.VaadinRequest
import com.vaadin.annotations.Push
import org.jooq.impl.DefaultConfiguration
import liquibase.Liquibase
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.database.jvm.JdbcConnection
import org.jooq.SQLDialect
import java.sql.DriverManager
import com.vaadin.navigator.Navigator
import hds.analysis.AnalysisView
import org.postgresql.Driver
import hds.analysis.ResultsView
import com.vaadin.annotations.Theme
import liquibase.exception.MigrationFailedException
import hds.db.connection

[Push]
[Theme("reindeer")]
public class App : UI() {

    class object {
        {
            DriverManager.registerDriver(Driver())
            val connection = connection()
            try {
                val liquibase = Liquibase("db/db-change-logs.xml", ClassLoaderResourceAccessor(), JdbcConnection(connection))
                liquibase.update("prod")
            } catch (ignore: MigrationFailedException) {
            } finally {
                connection.close()
            }
        }
    }

    override fun init(request: VaadinRequest?) {

        val navigator: Navigator = Navigator(this, this)
        navigator.addView("", AnalysisView(navigator))
        navigator.addView(ResultsView.NAME, ResultsView())
    }


}