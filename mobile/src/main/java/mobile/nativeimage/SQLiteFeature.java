package mobile.nativeimage;

import com.oracle.svm.core.jdk.NativeLibrarySupport;
import com.oracle.svm.core.jdk.PlatformNativeLibrarySupport;
import org.graalvm.nativeimage.hosted.Feature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;

/** Static JNI registration for the original Xerial driver, shared by Android and iOS. */
public final class SQLiteFeature implements Feature {
    @Override public void beforeAnalysis(BeforeAnalysisAccess access) {
        NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("sqlitejdbc");
        PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("org_sqlite_");
        PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("mobile_nativeimage_SQLiteBootstrap_");
        PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("mobile_nativeimage_SQLiteBootstrap_");
        for (String name : new String[]{"org.sqlite.core.NativeDB", "org.sqlite.core.DB",
                "org.sqlite.Function", "org.sqlite.Function$Aggregate", "org.sqlite.Function$Window",
                "org.sqlite.Collation", "org.sqlite.ProgressHandler", "org.sqlite.BusyHandler",
                "org.sqlite.core.DB$ProgressObserver", "java.lang.Throwable"}) {
            Class<?> type = access.findClassByName(name);
            if (type == null) throw new IllegalStateException("SQLite JNI class missing: " + name);
            JNIRuntimeAccess.register(type);
            JNIRuntimeAccess.register(type.getDeclaredFields());
            JNIRuntimeAccess.register(type.getDeclaredMethods());
            JNIRuntimeAccess.register(type.getDeclaredConstructors());
        }
        JNIRuntimeAccess.register(boolean[].class);
    }
}
