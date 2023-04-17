package pbrg.webservices.models;

import com.mysql.cj.jdbc.MysqlDataSource;

public class ChainingMysqlDataSource {

    private final MysqlDataSource mysqlDataSource;

    public ChainingMysqlDataSource() {
        mysqlDataSource = new MysqlDataSource();
    }

    public ChainingMysqlDataSource setServerName(String serverName) {
        mysqlDataSource.setServerName(serverName);
        return this;
    }

    public ChainingMysqlDataSource setPort(int port) {
        mysqlDataSource.setPort(port);
        return this;
    }

    public ChainingMysqlDataSource setDatabaseName(String databaseName) {
        mysqlDataSource.setDatabaseName(databaseName);
        return this;
    }

    public ChainingMysqlDataSource setUser(String user) {
        mysqlDataSource.setUser(user);
        return this;
    }

    public ChainingMysqlDataSource setPassword(String password) {
        mysqlDataSource.setPassword(password);
        return this;
    }

    public MysqlDataSource getMysqlDataSource() {
        return mysqlDataSource;
    }
}