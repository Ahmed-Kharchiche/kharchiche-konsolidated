package mobile.nativeimage;

/** Explicitly initializes Xerial's cached JNI IDs after static linkage. */
final class SQLiteBootstrap {
    private static boolean initialized;
    static synchronized void initialize() {
        if (initialized) return;
        if (initializeNative() < 0) throw new UnsatisfiedLinkError("Initialisation JNI SQLite impossible");
        initialized = true;
    }
    private static native int initializeNative();
}
