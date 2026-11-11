import java.nio.file.Files;
import java.sql.DriverManager;

/** Executed as a Native Image executable, not as a JVM unit test. */
public final class NativeSQLiteSmoke {
    public static void main(String[] args) throws Exception {
        var folder = Files.createTempDirectory("kharchiche-native-sqlite-");
        var database = folder.resolve("test.db");
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + database;
            try (var connection = DriverManager.getConnection(url); var sql = connection.createStatement()) {
                sql.execute("CREATE TABLE smoke (name TEXT)");
                sql.execute("INSERT INTO smoke VALUES ('test natif')");
            }
            try (var connection = DriverManager.getConnection(url); var sql = connection.createStatement();
                 var rows = sql.executeQuery("SELECT name, sqlite_version() FROM smoke")) {
                if (!rows.next() || !"test natif".equals(rows.getString(1)) || !"3.46.0".equals(rows.getString(2))) {
                    throw new AssertionError("Native SQLite persistence/version mismatch");
                }
            }
            System.out.println("NATIVE_SQLITE_JNI_PERSISTENCE_OK");
        } finally {
            Files.deleteIfExists(database);
            Files.deleteIfExists(folder);
        }
    }
}
