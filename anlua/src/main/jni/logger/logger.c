#include <jni.h>
#include <stdio.h>
#include <malloc.h>

#include "include/logger.h"

static JavaVM *jvm;

static jclass clz_Lg;
static jmethodID md_Lg_i;
static jmethodID md_Lg_e;


JNIEnv *getJNIEnv() {
    JNIEnv *env;
    int result = (*jvm)->GetEnv(jvm, (void **) &env, JNI_VERSION_1_4);
    if (result != JNI_OK) {
        return NULL;
    }

    return env;
}

char buff[10240];

void logI(const char *fmt, ...) {
    JNIEnv *env = getJNIEnv();

    va_list args;
    va_start(args, fmt);
    vsprintf(buff, fmt, args);
    va_end(args);

    jstring msg = (*env)->NewStringUTF(env, buff);
    (*env)->CallStaticVoidMethod(env, clz_Lg, md_Lg_i, msg);
}

void logE(const char *fmt, ...) {
    JNIEnv *env = getJNIEnv();

    va_list args;
    va_start(args, fmt);
    vsprintf(buff, fmt, args);
    va_end(args);

    jstring msg = (*env)->NewStringUTF(env, buff);
    (*env)->CallStaticVoidMethod(env, clz_Lg, md_Lg_e, msg);
}


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm=vm;

    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }

    clz_Lg = (jclass) (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "leasses/anlua/Lg"));
    md_Lg_i = (*env)->GetStaticMethodID(env, clz_Lg, "i", "(Ljava/lang/Object;)V");
    md_Lg_e = (*env)->GetStaticMethodID(env, clz_Lg, "e", "(Ljava/lang/Object;)V");

    return JNI_VERSION_1_4;
}