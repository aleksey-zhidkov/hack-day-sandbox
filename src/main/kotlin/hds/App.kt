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

[Push]
public class App : UI() {

    override fun init(request: VaadinRequest?) {
        DriverManager.registerDriver(Driver())
        val connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hds", "hds", "hds")

        val liquibase = Liquibase("db/db-change-logs.xml", ClassLoaderResourceAccessor(), JdbcConnection(connection))
        liquibase.update("prod")

        val conf = DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES)

        val navigator: Navigator = Navigator(this, this)
        navigator.addView("", AnalysisView())
    }


}