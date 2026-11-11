package mobile.nativeimage;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/** Mobile links SQLite into the app; never extracts a desktop library into writable storage. */
@TargetClass(className = "org.sqlite.SQLiteJDBCLoader")
final class SQLiteLoader {
    @Substitute
    public static synchronized boolean initialize() {
        System.loadLibrary("sqlitejdbc");
        SQLiteBootstrap.initialize();
        SQLiteBootstrap.initialize();
        return true;
    }
}
