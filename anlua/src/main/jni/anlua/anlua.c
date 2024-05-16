#include <jni.h>
#include <setjmp.h>
#include <stdbool.h>

#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "logger.h"

static JavaVM *jvm;

static jclass Object;

static jclass JLight;
static jmethodID JLight_bool;
static jmethodID JLight_num;

static jclass AnluaApi;
static jmethodID AnluaApi_import;
static jmethodID AnluaApi_objectIndex;
static jmethodID AnluaApi_objectCall;

static jfieldID CLight_num;
static jfieldID CLight_obj;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm = vm;

    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }

    Object = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "java/lang/Object"));
    if (!Object) return JNI_ERR;

    JLight = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "leasses/anlua/api/JLight"));
    if (!JLight) return JNI_ERR;
    JLight_bool = (*env)->GetStaticMethodID(
            env, JLight,
            "bool", "(Z)Lleasses/anlua/api/JLight$Z;");
    JLight_num = (*env)->GetStaticMethodID(
            env, JLight,
            "num", "(D)Ljava/lang/Object;");

    AnluaApi = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "leasses/anlua/api/AnluaApi"));
    if (!AnluaApi) return JNI_ERR;
    AnluaApi_import = (*env)->GetStaticMethodID(
            env, AnluaApi,
            "imports", "(Ljava/lang/String;)Lleasses/anlua/api/CLight;");
    AnluaApi_objectIndex = (*env)->GetStaticMethodID(
            env, AnluaApi,
            "objectIndex", "(Ljava/lang/Object;Ljava/lang/String;)Lleasses/anlua/api/CLight;");
    AnluaApi_objectCall = (*env)->GetStaticMethodID(
            env, AnluaApi,
            "objectCall", "(Ljava/lang/Object;[Ljava/lang/Object;)Lleasses/anlua/api/CLight;");

    jclass CLight = (*env)->FindClass(env, "leasses/anlua/api/CLight");
    if (!CLight) return JNI_ERR;
    CLight_num = (*env)->GetFieldID(env, CLight, "num", "D");
    CLight_obj = (*env)->GetFieldID(env, CLight, "obj", "Ljava/lang/Object;");

    if (!AnluaApi_import ||
        !AnluaApi_objectIndex ||
        !AnluaApi_objectCall ||
        !CLight_num ||
        !CLight_obj ||
        !JLight_bool ||
        !JLight_num)
        return JNI_ERR;

    return JNI_VERSION_1_4;
}

JNIEnv *getJNIEnv(lua_State *L) {
    JNIEnv *env;
    int result = (*jvm)->GetEnv(jvm, (void **) &env, JNI_VERSION_1_4);
    if (result != JNI_OK) {
        luaL_error(L, "cannot get the JNIEnv");
        return NULL;
    }

    return env;
}

// Lua error handler
static jmp_buf jmpBuf;

#define try(L, env, exp) ({if (setjmp(jmpBuf) == 0) exp else throwFromLua(L,env);})

static void throw(JNIEnv *env, const char *msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "leasses/anlua/LuaException"), msg);
}

static void throwFromLua(lua_State *L, JNIEnv *env) {
    if (setjmp(jmpBuf) == 0)
        throw(env, luaL_checkstring(L, -1));
    else
        throw(env, "error without message or cannot get the error message");
}

static int panic(lua_State *L) {
    longjmp(jmpBuf, 1);
}

// Anlua interfaces
#include "anlua_const.h"

static void luaJ_dump(lua_State *L) {
    for (int i = 1; i <= lua_gettop(L); i++)
        logE("INDEX: %d \tTYPE:%s ", i, luaL_typename(L, i));

}

static void luaJ_pushResult(lua_State *L, JNIEnv *env, jobjectArray result) {
    if (!result) {
        luaL_error(L, "result from java is null");
        return;
    }

    jdouble num = (*env)->GetDoubleField(env, result, CLight_num);
    jobject obj = (*env)->GetObjectField(env, result, CLight_obj);

    if (obj == NULL) {  // A number
        lua_pushnumber(L, num);
        return;
    }

    switch ((jbyte) num) {
        case LIGHT_TYPE_ERR:
            luaL_error(L, (*env)->GetStringUTFChars(env, obj, NULL));
            break;

        case LIGHT_TYPE_NULL:
            lua_pushnil(L);
            break;

        case LIGHT_TYPE_TRUE:
            lua_pushboolean(L, 1);
            break;

        case LIGHT_TYPE_FALSE:
            lua_pushboolean(L, 0);
            break;

        case LIGHT_TYPE_STR:
            lua_pushstring(L, (*env)->GetStringUTFChars(env, obj, NULL));
            break;

        case LIGHT_TYPE_OBJ: {
            jobject *data = (jobject *) lua_newuserdata(L, sizeof(jobject));
            *data = (jobject *) (*env)->NewGlobalRef(env, obj);
            luaL_setmetatable(L, JAVA_OBJECT);
            break;
        }

        default:
            luaL_error(L, "unknown result type");
    }
}


jobject luaJ_toObject(lua_State *L, JNIEnv *env, jint idx) {
    switch (lua_type(L, idx)) {
        case LUA_TBOOLEAN:
            return (*env)->CallStaticObjectMethod(
                    env, JLight, JLight_bool, lua_toboolean(L, idx));

        case LUA_TUSERDATA:
            return *((jobject *) lua_touserdata(L, idx));

        case LUA_TNUMBER:
            return (*env)->CallStaticObjectMethod(
                    env, JLight, JLight_num, lua_tonumber(L, idx));

        case LUA_TSTRING:
            return (*env)->NewStringUTF(env, lua_tostring(L, idx));

        default:
            return NULL;
    }
}

#include "anlua_api.c"
#include "anlua_native.c"

