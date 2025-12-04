package database;

import java.sql.Connection;
import java.sql.SQLException;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;

public class DatabaseServiceHandler {
    private final static String DB_USER = System.getenv("DB_USER");
    private final static String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private final static String CONNECT_STRING = System.getenv("CONNECT_STRING");
    private final static String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.replay.OracleConnectionPoolDataSourceImpl";
    private final PoolDataSource poolDataSource;

    public DatabaseServiceHandler() throws SQLException {
        this.poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
        poolDataSource.setURL("jdbc:oracle:thin:@" + CONNECT_STRING);
        poolDataSource.setUser(DB_USER);
        poolDataSource.setPassword(DB_PASSWORD);
        poolDataSource.setConnectionPoolName("JDBC_UCP_POOL");
    }
    public void testConnection() {
        try {
            try (Connection conn = poolDataSource.getConnection()) {
                System.out.println(conn.getSchema());
            }
        } catch (SQLException e) {
            System.out.println("Could not connect to the database - SQLException occurred: " + e.getMessage());
        }
    }

    // Currently in WIP
    public static void main(String[] args) {
        try {
            DatabaseServiceHandler uds = new DatabaseServiceHandler();
            uds.testConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}