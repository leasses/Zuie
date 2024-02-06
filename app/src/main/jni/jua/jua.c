
#include <jni.h>
#include <setjmp.h>
#include <stdbool.h>

#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "logger.h"

static JavaVM *jvm = NULL;

static jclass Object;

static jclass Boolean;
static jmethodID Boolean_valueOf;
static jobject Boolean_TRUE;

static jclass JuaApi;
static jmethodID JuaApi_objectIndex;
static jmethodID JuaApi_objectCall;

static jobject Result_TYPE_NONE;
static jobject Result_TYPE_NIL;
static jobject Result_TYPE_NUM;
static jobject Result_TYPE_BOOL;
static jobject Result_TYPE_STR;

static jclass Result_Num;
static jmethodID Result_Num_of;
static jfieldID Result_Num_value;

static jclass Result_Byte;
static jmethodID Result_Byte_of;
static jfieldID Result_Byte_value;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm = vm;

    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }

    Object = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "java/lang/Object"));
    if (!Object) return JNI_ERR;

    Boolean = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "java/lang/Boolean"));
    if (!Boolean) return JNI_ERR;
    Boolean_valueOf = (*env)->GetStaticMethodID(
            env, Boolean, "valueOf", "(Z)Ljava/lang/Boolean;");
    Boolean_TRUE = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(
            env, Boolean, (*env)->GetStaticFieldID(
                    env, Boolean, "TRUE", "Ljava/lang/Boolean;")));

    JuaApi = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "leasses/jua/api/JuaApi"));
    if (!JuaApi) return JNI_ERR;
    JuaApi_objectIndex = (*env)->GetStaticMethodID(
            env, JuaApi,
            "objectIndex", "(Ljava/lang/Object;Ljava/lang/String;)[Ljava/lang/Object;");
    JuaApi_objectCall = (*env)->GetStaticMethodID(
            env, JuaApi,
            "objectCall", "(Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;");

    jclass Result = (*env)->FindClass(env, "leasses/jua/api/Result");
    if (!Result) return JNI_ERR;
    Result_TYPE_NONE = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(
            env, Result, (*env)->GetStaticFieldID(
                    env, Result, "TYPE_NONE", "Lleasses/jua/api/Result$Byte;")));
    Result_TYPE_NIL = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(
            env, Result, (*env)->GetStaticFieldID(
                    env, Result, "TYPE_NIL", "Lleasses/jua/api/Result$Byte;")));
    Result_TYPE_NUM = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(
            env, Result, (*env)->GetStaticFieldID(
                    env, Result, "TYPE_NUM", "Lleasses/jua/api/Result$Byte;")));
    Result_TYPE_BOOL = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(
            env, Result, (*env)->GetStaticFieldID(
                    env, Result, "TYPE_BOOL", "Lleasses/jua/api/Result$Byte;")));
    Result_TYPE_STR = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(
            env, Result, (*env)->GetStaticFieldID(
                    env, Result, "TYPE_STR", "Lleasses/jua/api/Result$Byte;")));

    Result_Num = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "leasses/jua/api/Result$Num"));
    if (!Result_Num) return JNI_ERR;
    Result_Num_of = (*env)->GetStaticMethodID(
            env, Result_Num,
            "of", "(D)Lleasses/jua/api/Result$Num;");
    Result_Num_value = (*env)->GetFieldID(
            env, Result_Num, "value", "D");

    Result_Byte = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "leasses/jua/api/Result$Byte"));
    if (!Result_Byte) return JNI_ERR;
    Result_Byte_of = (*env)->GetStaticMethodID(
            env, Result_Byte,
            "of", "(B)Lleasses/jua/api/Result$Byte;");
    Result_Byte_value = (*env)->GetFieldID(
            env, Result_Byte, "value", "B");

    if (!Boolean_valueOf ||
        !Boolean_TRUE ||
        !JuaApi_objectIndex ||
        !JuaApi_objectCall ||
        !Result_TYPE_NONE ||
        !Result_TYPE_NIL ||
        !Result_TYPE_NUM ||
        !Result_TYPE_BOOL ||
        !Result_TYPE_STR ||
        !Result_Num_of ||
        !Result_Num_value ||
        !Result_Byte_of ||
        !Result_Byte_value)
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
    (*env)->ThrowNew(env, (*env)->FindClass(env, "leasses/jua/LuaException"), msg);
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

// Jua interfaces
#include "jua_const.h"

static void luaJ_dump(lua_State *L) {
    for (int i = 1; i <= lua_gettop(L); i++) {
        logE("INDEX: %d \tTYPE:%s ", i, luaL_typename(L, i));
    }
}

static void luaJ_pushResult(lua_State *L, JNIEnv *env, jobjectArray result) {
    if (!result) {
        luaL_error(L, "result from java is null");
        return;
    }

    jobject type = (*env)->GetObjectArrayElement(env, result, 0);

    if (type == NULL) {
        jobject obj = (*env)->GetObjectArrayElement(env, result, 1);
        jobject *data = (jobject *) lua_newuserdata(L, sizeof(jobject));
        *data = (jobject *) (*env)->NewGlobalRef(env, obj);
        luaL_setmetatable(L, JAVA_OBJECT);
        return;
    }

    switch ((*env)->GetByteField(env, type, Result_Byte_value)) {
        break;
        case LUA_TNIL:
            lua_pushnil(L);
            break;
        case LUA_TBOOLEAN:
            lua_pushboolean(L, (*env)->GetByteField(
                    env, (*env)->GetObjectArrayElement(env, result, 1), Result_Byte_value));
            break;
        case LUA_TNUMBER:
            lua_pushnumber(L, (*env)->GetDoubleField(
                    env, (*env)->GetObjectArrayElement(env, result, 1), Result_Num_value));
            break;
        case LUA_TSTRING:
            lua_pushstring(L, (*env)->GetStringUTFChars(
                    env, (*env)->GetObjectArrayElement(env, result, 1), NULL));
            break;
        case LUA_TNONE:
            luaL_error(L, (*env)->GetStringUTFChars(
                    env, (*env)->GetObjectArrayElement(env, result, 1), NULL));
            break;
        default:
            luaL_error(L, "unknown result type");
    }
}


jobject luaJ_toObject(lua_State *L, JNIEnv *env, jint idx) {
    switch (lua_type(L, idx)) {
        case LUA_TBOOLEAN:
            return (*env)->CallStaticObjectMethod(
                    env, Result_Byte, Result_Byte_of, lua_toboolean(L, idx));
        case LUA_TUSERDATA:
            return *((jobject *) lua_touserdata(L, idx));
        case LUA_TNUMBER:
            return (*env)->CallStaticObjectMethod(
                    env, Result_Num, Result_Num_of, lua_tonumber(L, idx));
        case LUA_TSTRING:
            return (*env)->NewStringUTF(env, lua_tostring(L, idx));
    }
    return NULL;
}

#include "jua_api.c"
#include "jua_native.c"

