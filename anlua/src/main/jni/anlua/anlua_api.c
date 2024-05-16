static int api_print(lua_State *L) {
    logI(luaL_checkstring(L, -1));
    return 0;
}

static int api_import(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);

    const char *name = luaL_checkstring(L, -1);

    jobject result = (*env)->CallStaticObjectMethod(
            env, AnluaApi, AnluaApi_import, (*env)->NewStringUTF(env, name));

    luaJ_pushResult(L, env, result);
    return 1;
}

static int api_call(lua_State *L) {
    // An example lua stack:
    //
    // ... and so on
    // 4 -1 param #3
    // 3 -2 param #2
    // 2 -3 param #1
    // 1 -4 java object
    JNIEnv *env = getJNIEnv(L);

    jobject *obj = (jobject *) luaL_checkudata(L, 1, JAVA_OBJECT);
    int top = lua_gettop(L);
    jobjectArray params;

    if (top - 1 == 0)
        params = NULL;
    else {
        params = (*env)->NewObjectArray(env, top - 1, Object, NULL);

        for (int i = 2; i <= top; i++) {
            (*env)->SetObjectArrayElement(
                    env, params, i - 2, luaJ_toObject(L, env, i));
        }
    }

    jobject result = (*env)->CallStaticObjectMethod(
            env, AnluaApi, AnluaApi_objectCall, *obj, params);

    luaJ_pushResult(L, env, result);
    return 1;
}

static int api_index(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);

    const char *key = luaL_checkstring(L, -1);

    jobject *obj = (jobject *) luaL_checkudata(L, -2, JAVA_OBJECT);

    jobject result = (*env)->CallStaticObjectMethod(
            env, AnluaApi, AnluaApi_objectIndex, *obj, (*env)->NewStringUTF(env, key));

    luaJ_pushResult(L, env, result);
    return 1;
}

static int api_gc(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);

    jobject *obj = (jobject *) luaL_checkudata(L, -1, JAVA_OBJECT);

    (*env)->DeleteGlobalRef(env, *obj);
    return 0;
}


static void luaJ_open(lua_State *L) {
    luaL_register(L, ANLUA_LIB_NAME,
                  (luaL_Reg[]) {{"import", api_import},
                                {"print",  api_print},
                                {NULL, NULL}});
    lua_pop(L, 1);

    luaL_newmetatable(L, JAVA_OBJECT);
    luaL_setfuncs(L,
                  (luaL_Reg[]) {{LUA_META_METHOD_CALL,  api_call},
                                {LUA_META_METHOD_INDEX, api_index},
                                {LUA_META_METHOD_GC,    api_gc},
                                {NULL, NULL}},
                  0);
    lua_pop(L, 1);
}