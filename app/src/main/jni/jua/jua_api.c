static int api_print(lua_State *L) {
    logI(lua_tostring(L, -1));
    return 0;
}

static int api_import(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);

    const char *name = lua_tostring(L, -1);

    jclass *globalRef = (jclass *) (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, name));

    jclass *userData = (jclass *) lua_newuserdata(L, sizeof(jclass));
    *userData = globalRef;
    luaL_setmetatable(L, JAVA_OBJECT);

    return 1;
}

static int api_call(lua_State *L) {
    // An example lua stack:
    // 4 -1 param #3
    // 3 -2 param #2
    // 2 -3 param #1
    // 1 -4 java object
    JNIEnv *env = getJNIEnv(L);

    jobject *obj = (jobject *) luaL_checkudata(L, 1, JAVA_OBJECT);
    int top = lua_gettop(L);
    jobjectArray params;

    if (top - 1 == 0) {
        params = NULL;
    } else {
        params = (*env)->NewObjectArray(env, top - 1, Object, NULL);

        for (int i = 2; i <= top; i++) {
            (*env)->SetObjectArrayElement(
                    env, params, i - 2, luaJ_toObject(L, env, i));
        }
    }

    jobjectArray result = (*env)->CallStaticObjectMethod(
            env, JuaApi, JuaApi_objectCall, *obj, params);

    luaJ_pushResult(L, env, result);
    return 1;
}

static int api_index(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);

    const char *key = luaL_checkstring(L, -1);
    jobject *obj = (jobject *) luaL_checkudata(L, -2, JAVA_OBJECT);

    jobjectArray result = (*env)->CallStaticObjectMethod(
            env, JuaApi, JuaApi_objectIndex, *obj, (*env)->NewStringUTF(env, key));

    luaJ_pushResult(L, env, result);
    return 1;
}

static int api_gc(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);

    jobject *obj = (jobject *) luaL_checkudata(L, -1, JAVA_OBJECT);

    (*env)->DeleteGlobalRef(env, *obj);
    return 0;
}

const static luaL_Reg jua_lib[] = {{"import", api_import},
                                   {"print",  api_print},
                                   {NULL, NULL}};


const static luaL_Reg jua_javaObjectMT[] = {{LUA_METAMETHOD_CALL,  api_call},
                                            {LUA_METAMETHOD_INDEX, api_index},
                                            {LUA_METAMETHOD_GC,    api_gc},
                                            {NULL, NULL}};

static void luaJ_open(lua_State *L) {
    luaL_register(L, JUA_LIB_NAME, jua_lib);
    lua_pop(L, 1);

    luaL_newmetatable(L, JAVA_OBJECT);
    luaL_setfuncs(L, jua_javaObjectMT, 0);
    lua_pop(L, 1);
}