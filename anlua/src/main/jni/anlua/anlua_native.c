#pragma clang diagnostic push
#pragma ide diagnostic ignored "UnusedParameter"

JNIEXPORT jbyte JNICALL
Java_leasses_anlua_Native_getLightTypeErr(JNIEnv *env, jclass clazz) { return LIGHT_TYPE_ERR; }

JNIEXPORT jbyte JNICALL
Java_leasses_anlua_Native_getLightTypeNull(JNIEnv *env, jclass clazz) { return LIGHT_TYPE_NULL; }

JNIEXPORT jbyte JNICALL
Java_leasses_anlua_Native_getLightTypeTrue(JNIEnv *env, jclass clazz) { return LIGHT_TYPE_TRUE; }

JNIEXPORT jbyte JNICALL
Java_leasses_anlua_Native_getLightTypeFalse(JNIEnv *env, jclass clazz) { return LIGHT_TYPE_FALSE; }

JNIEXPORT jbyte JNICALL
Java_leasses_anlua_Native_getLightTypeStr(JNIEnv *env, jclass clazz) { return LIGHT_TYPE_STR; }

JNIEXPORT jbyte JNICALL
Java_leasses_anlua_Native_getLightTypeObj(JNIEnv *env, jclass clazz) { return LIGHT_TYPE_OBJ; }


JNIEXPORT jlong JNICALL
Java_leasses_anlua_Native_newState
        (JNIEnv *env, jclass clazz) {
    lua_State *L = luaL_newstate();
    if (L) {
        try(L, env, {
            luaL_openlibs(L);
            luaJ_open(L);
            lua_atpanic(L, &panic);
        });
        return (jlong) L;
    }
    throw(env, "cannot register lua state");
    return (jlong) NULL;
}

JNIEXPORT jobjectArray JNICALL
Java_leasses_anlua_Native_doString
        (JNIEnv *env, jclass clazz, jlong ptr, jstring s) {
    lua_State *L = (lua_State *) ptr;

    lua_getglobal(L, "debug");
    lua_getfield(L, -1, "traceback");
    luaL_loadstring(L, (*env)->GetStringUTFChars(env, s, NULL));

    if (lua_pcall(L, 0, LUA_MULTRET, -2) == LUA_OK) {
        int top = lua_gettop(L) - 2;
        jobjectArray result = (*env)->NewObjectArray(env, top, Object, NULL);

        for (int i = 0; i < top; i++) {
            try(L, env,
                (*env)->SetObjectArrayElement(
                        env, result, i, luaJ_toObject(L, env, i + 3));
            );
        }
        lua_pop(L, 2);
        return result;
    }

    throwFromLua(L, env);
    lua_pop(L, 2);
    luaJ_dump(L);
    return NULL;
}

#pragma clang diagnostic pop