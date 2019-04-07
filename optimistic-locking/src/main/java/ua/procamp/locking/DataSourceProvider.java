package ua.procamp.locking;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {

    private static final String databaseUrl = "jdbc:postgresql://localhost:5432/procamp";
    private static final String username = "postgres";
    private static final String password = "root";
    private static volatile DataSource dataSource = null;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DataSourceProvider.class) {
                if (dataSource == null) {
                    dataSource = createPostgresDataSource(databaseUrl, username, password);
                }
            }
        }
        return dataSource;
    }

    private static DataSource createPostgresDataSource(String url, String username, String pass) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(pass);
        return dataSource;
    }

}
