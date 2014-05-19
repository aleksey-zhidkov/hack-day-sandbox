package hds

import com.vaadin.ui.UI
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.Alignment
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.annotations.Push
import org.jooq.impl.DefaultConfiguration
import liquibase.Liquibase
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.database.jvm.JdbcConnection
import org.jooq.SQLDialect
import hds.db.tables.daos.UserDao
import java.sql.DriverManager
import org.h2.Driver

[Push]
public class App : UI() {

    val push = Label("no content")

    override fun init(request: VaadinRequest?) {
        DriverManager.registerDriver(Driver())
        val connection = DriverManager.getConnection("jdbc:h2:build/db/hds;FILE_LOCK=NO;MODE=PostgreSQL", "sa", "")

        val liquibase = Liquibase("db/db-change-logs.xml", ClassLoaderResourceAccessor(), JdbcConnection(connection))
        liquibase.update("prod")

        val conf = DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES)
        val usersDao = UserDao(conf)

        val view = VerticalLayout()
        view.setSizeFull()

        val label = Label("<div style='text-align: center'>Hello Vaadin!</div>", ContentMode.HTML)
        view.addComponent(label)
        view.setComponentAlignment(label, Alignment.MIDDLE_CENTER)

        view.addComponent(push)
        view.setComponentAlignment(push, Alignment.MIDDLE_CENTER)

        setContent(view)

        Pusher(usersDao).start()
    }

    inner class Pusher(val userDao: UserDao) : Thread() {

        override fun run() {
            for (i in 1..10) {
                Thread.sleep(1000)
                access {
                    push.setValue("Push $i")
                }
            }

            access {
                push.setValue("Users: " + userDao.findAll())
            }
        }
    }

}