package com.softwareverde.database.mysql;

import com.softwareverde.database.DatabaseConnectionFactory;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.database.mysql.properties.Credentials;
import com.softwareverde.database.mysql.properties.DatabaseProperties;
import com.softwareverde.util.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class MysqlDatabaseConnectionFactory implements DatabaseConnectionFactory<Connection> {
    public static String createConnectionString(final String hostname, final Integer port, final String schema) {
        final StringBuilder stringBuilder = new StringBuilder("jdbc:mysql://");
        stringBuilder.append(Util.coalesce(hostname, "localhost"));
        stringBuilder.append(":");
        stringBuilder.append(Util.coalesce(port, MysqlDatabase.DEFAULT_PORT));
        stringBuilder.append("/");
        stringBuilder.append(Util.coalesce(schema, ""));

        return stringBuilder.toString();
    }

    protected static void _setDefaultConnectionProperties(final Properties properties) {
        if (! properties.containsKey("allowPublicKeyRetrieval")) {
            properties.setProperty("allowPublicKeyRetrieval", "true");
        }

        if (! properties.containsKey("useSSL")) {
            properties.setProperty("useSSL", "false");
        }

        if (! properties.containsKey("serverTimezone")) {
            properties.setProperty("serverTimezone", "UTC");
        }
    }

    /**
     * Require dependencies be packaged at compile-time.
     */
    private static final Class[] UNUSED = {
        com.mysql.jdbc.Driver.class
    };

    protected final String _hostname;
    protected final Integer _port;
    protected final Properties _connectionProperties;

    protected String _username;
    protected String _password;
    protected String _schema;

    public MysqlDatabaseConnectionFactory(final DatabaseProperties databaseProperties) {
        this(databaseProperties, databaseProperties.getCredentials());
    }

    public MysqlDatabaseConnectionFactory(final DatabaseProperties databaseProperties, final Credentials credentials) {
        this(databaseProperties, credentials, new Properties());
    }

    public MysqlDatabaseConnectionFactory(final DatabaseProperties databaseProperties, final Credentials credentials, final Properties connectionProperties) {
        this(databaseProperties.getHostname(), databaseProperties.getPort(), databaseProperties.getSchema(), credentials.username, credentials.password, connectionProperties);
    }

    public MysqlDatabaseConnectionFactory(final String hostname, final Integer port, final String schema, final String username, final String password) {
        this(hostname, port, schema, username, password, new Properties());
    }

    public MysqlDatabaseConnectionFactory(final String hostname, final Integer port, final String schema, final String username, final String password, final Properties properties) {
        _hostname = hostname;
        _port = port;
        _schema = schema;
        _connectionProperties = new Properties(properties);

        _username = username;
        _password = password;

        MysqlDatabaseConnectionFactory._setDefaultConnectionProperties(_connectionProperties);
    }

    @Override
    public MysqlDatabaseConnection newConnection() throws DatabaseException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            final String connectionString = MysqlDatabaseConnectionFactory.createConnectionString(_hostname, _port, _schema);

            final Properties connectionProperties = new Properties(_connectionProperties);
            connectionProperties.put("user", _username);
            connectionProperties.put("password", _password);

            final Connection connection = DriverManager.getConnection(connectionString, connectionProperties);
            return new MysqlDatabaseConnection(connection);
        }
        catch (final Exception exception) {
            throw new DatabaseException(exception);
        }
    }
}
