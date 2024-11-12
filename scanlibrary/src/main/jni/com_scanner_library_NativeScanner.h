/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_scanner_library_NativeScanner */

#ifndef _Included_com_scanner_library_NativeScanner
#define _Included_com_scanner_library_NativeScanner
#ifdef __cplusplus
extern "C" {
#endif
#undef com_scanner_library_NativeScanner_BIND_ABOVE_CLIENT
#define com_scanner_library_NativeScanner_BIND_ABOVE_CLIENT 8L
#undef com_scanner_library_NativeScanner_BIND_ADJUST_WITH_ACTIVITY
#define com_scanner_library_NativeScanner_BIND_ADJUST_WITH_ACTIVITY 128L
#undef com_scanner_library_NativeScanner_BIND_ALLOW_OOM_MANAGEMENT
#define com_scanner_library_NativeScanner_BIND_ALLOW_OOM_MANAGEMENT 16L
#undef com_scanner_library_NativeScanner_BIND_AUTO_CREATE
#define com_scanner_library_NativeScanner_BIND_AUTO_CREATE 1L
#undef com_scanner_library_NativeScanner_BIND_DEBUG_UNBIND
#define com_scanner_library_NativeScanner_BIND_DEBUG_UNBIND 2L
#undef com_scanner_library_NativeScanner_BIND_IMPORTANT
#define com_scanner_library_NativeScanner_BIND_IMPORTANT 64L
#undef com_scanner_library_NativeScanner_BIND_NOT_FOREGROUND
#define com_scanner_library_NativeScanner_BIND_NOT_FOREGROUND 4L
#undef com_scanner_library_NativeScanner_BIND_WAIVE_PRIORITY
#define com_scanner_library_NativeScanner_BIND_WAIVE_PRIORITY 32L
#undef com_scanner_library_NativeScanner_CONTEXT_IGNORE_SECURITY
#define com_scanner_library_NativeScanner_CONTEXT_IGNORE_SECURITY 2L
#undef com_scanner_library_NativeScanner_CONTEXT_INCLUDE_CODE
#define com_scanner_library_NativeScanner_CONTEXT_INCLUDE_CODE 1L
#undef com_scanner_library_NativeScanner_CONTEXT_RESTRICTED
#define com_scanner_library_NativeScanner_CONTEXT_RESTRICTED 4L
#undef com_scanner_library_NativeScanner_MODE_APPEND
#define com_scanner_library_NativeScanner_MODE_APPEND 32768L
#undef com_scanner_library_NativeScanner_MODE_ENABLE_WRITE_AHEAD_LOGGING
#define com_scanner_library_NativeScanner_MODE_ENABLE_WRITE_AHEAD_LOGGING 8L
#undef com_scanner_library_NativeScanner_MODE_MULTI_PROCESS
#define com_scanner_library_NativeScanner_MODE_MULTI_PROCESS 4L
#undef com_scanner_library_NativeScanner_MODE_PRIVATE
#define com_scanner_library_NativeScanner_MODE_PRIVATE 0L
#undef com_scanner_library_NativeScanner_MODE_WORLD_READABLE
#define com_scanner_library_NativeScanner_MODE_WORLD_READABLE 1L
#undef com_scanner_library_NativeScanner_MODE_WORLD_WRITEABLE
#define com_scanner_library_NativeScanner_MODE_WORLD_WRITEABLE 2L
#undef com_scanner_library_NativeScanner_DEFAULT_KEYS_DIALER
#define com_scanner_library_NativeScanner_DEFAULT_KEYS_DIALER 1L
#undef com_scanner_library_NativeScanner_DEFAULT_KEYS_DISABLE
#define com_scanner_library_NativeScanner_DEFAULT_KEYS_DISABLE 0L
#undef com_scanner_library_NativeScanner_DEFAULT_KEYS_SEARCH_GLOBAL
#define com_scanner_library_NativeScanner_DEFAULT_KEYS_SEARCH_GLOBAL 4L
#undef com_scanner_library_NativeScanner_DEFAULT_KEYS_SEARCH_LOCAL
#define com_scanner_library_NativeScanner_DEFAULT_KEYS_SEARCH_LOCAL 3L
#undef com_scanner_library_NativeScanner_DEFAULT_KEYS_SHORTCUT
#define com_scanner_library_NativeScanner_DEFAULT_KEYS_SHORTCUT 2L
#undef com_scanner_library_NativeScanner_RESULT_CANCELED
#define com_scanner_library_NativeScanner_RESULT_CANCELED 0L
#undef com_scanner_library_NativeScanner_RESULT_FIRST_USER
#define com_scanner_library_NativeScanner_RESULT_FIRST_USER 1L
#undef com_scanner_library_NativeScanner_RESULT_OK
#define com_scanner_library_NativeScanner_RESULT_OK -1L
/*
 * Class:     com_scanner_library_NativeScanner
 * Method:    getScannedBitmap
 * Signature: (IILandroid/graphics/Bitmap;FFFFFFFF)Landroid/graphics/Bitmap;
 */
JNIEXPORT void
JNICALL Java_com_scanner_library_NativeScanner_configureScanner
        (JNIEnv* env, jobject obj,
        jboolean filterEnabled, jboolean applyCLAHE,
        jdouble scaleFactor, jdouble contrastValue,
        jdouble contrastLimitThreshold);

JNIEXPORT jobject

JNICALL Java_com_scanner_library_NativeScanner_getScannedBitmap
        (JNIEnv *, jobject, jobject,
         jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat);


JNIEXPORT jfloatArray

JNICALL Java_com_scanner_library_NativeScanner_getPoints
        (JNIEnv *, jobject, jobject);

JNIEXPORT jobject

JNICALL Java_com_scanner_library_NativeScanner_getBWBitmap
        (JNIEnv *, jobject, jobject);

JNIEXPORT jobject

JNICALL Java_com_scanner_library_NativeScanner_getMagicColorBitmap
        (JNIEnv *, jobject, jobject);

JNIEXPORT jobject

JNICALL Java_com_scanner_library_NativeScanner_getGrayBitmap
        (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
