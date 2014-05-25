package hds;

import hds.db.DB;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.wicket.protocol.http.WebApplication;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class WicketApplication extends WebApplication
{
    /**
     * Constructor
     */
    public WicketApplication() throws SQLException, LiquibaseException {
        DriverManager.registerDriver(new Driver());
        Connection connection = new DB().getConnection();
        try {
            Liquibase liquibase = new Liquibase("db/db-change-logs.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            liquibase.update("prod");
        } catch (MigrationFailedException e) {
        }
        finally {
            connection.close();
        }
    }

    public Class<HomePage> getHomePage()
    {
        return HomePage.class;
    }
}