#include <jni.h>
#include <stddef.h>

/* Gluon statically links libraries; do not rely on dlsym finding JNI_OnLoad.
 * The original Xerial initializer populates the field/method IDs used by NativeDB.
 */
extern jint JNICALL Kharchiche_SQLite_OnLoad(JavaVM *vm, void *reserved);

JNIEXPORT jint JNICALL Java_mobile_nativeimage_SQLiteBootstrap_initializeNative(JNIEnv *env, jclass type) {
    JavaVM *vm = NULL;
    (void)type;
    if ((*env)->GetJavaVM(env, &vm) != JNI_OK) return JNI_ERR;
    return Kharchiche_SQLite_OnLoad(vm, NULL);
}
