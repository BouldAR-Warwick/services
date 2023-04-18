package pbrg.webservices.models;

import com.mysql.cj.jdbc.MysqlDataSource;

public class ChainingMysqlDataSource {

    /** The mysql data source. */
    private final MysqlDataSource mysqlDataSource;

    /**
     * Constructor.
     */
    public ChainingMysqlDataSource() {
        mysqlDataSource = new MysqlDataSource();
    }

    /**
     * Set the server name.
     * @param serverName server name
     * @return this
     */
    public ChainingMysqlDataSource setServerName(final String serverName) {
        mysqlDataSource.setServerName(serverName);
        return this;
    }

    /**
     * Set the port.
     * @param port port
     * @return this
     */
    public ChainingMysqlDataSource setPort(final int port) {
        mysqlDataSource.setPort(port);
        return this;
    }

    /**
     * Set the database name.
     * @param databaseName database name
     * @return this
     */
    public final ChainingMysqlDataSource setDatabaseName(
        final String databaseName
    ) {
        mysqlDataSource.setDatabaseName(databaseName);
        return this;
    }

    /**
     * Set the user.
     * @param user user
     * @return this
     */
    public final ChainingMysqlDataSource setUser(final String user) {
        mysqlDataSource.setUser(user);
        return this;
    }

    /**
     * Set the password.
     * @param password password
     * @return this
     */
    public final ChainingMysqlDataSource setPassword(final String password) {
        mysqlDataSource.setPassword(password);
        return this;
    }

    /**
     * Get the mysql data source.
     * @return mysql data source
     */
    public final MysqlDataSource getMysqlDataSource() {
        return mysqlDataSource;
    }
}
