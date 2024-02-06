
JNIEXPORT jlong JNICALL
Java_leasses_jua_Jua_00024Native_newState
        (JNIEnv *env, jobject clazz) {
    lua_State *L = luaL_newstate();
    if (L) {
        try(L, env, {
            luaL_openlibs(L);
            luaJ_open(L);
//            lua_atpanic(L, &panic);
        });
        return (jlong) L;
    }
    throw(env, "cannot create lua state");
    return (jlong) NULL;
}

JNIEXPORT jobjectArray JNICALL
Java_leasses_jua_Jua_00024Native_doString
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