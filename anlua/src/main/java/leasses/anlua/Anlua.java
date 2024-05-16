package leasses.anlua;

import androidx.annotation.Nullable;

//    public static short LUA_TYPE_THREAD = 8;
//        private static final short LUA_OK = 0;
//        private static final short LUA_YIELD = 1;
//        private static final short LUA_ERR_RUN = 2;
//        private static final short LUA_ERR_SYNTAX = 3;
//        private static final short LUA_ERR_MEM = 4;
//        private static final short LUA_ERR_ERR = 5;
//    public static final byte LUA_TYPE_USERDATA = 7;
//    public static final byte LUA_TYPE_TABLE = 5;
public class Anlua {
    public static final byte LUA_TYPE_NONE = -1;
    public static final byte LUA_TYPE_NIL = 0;
    public static final byte LUA_TYPE_BOOLEAN = 1;
    public static final byte LUA_TYPE_NUMBER = 3;
    public static final byte LUA_TYPE_STRING = 4;
    private final long ptr;
    private final ErrorHandler handler;

    private Anlua(long ptr, ErrorHandler handler) {
        this.ptr = ptr;
        this.handler = handler;
    }

    @Nullable
    public static Anlua create(ErrorHandler handler) {
        try {
            return new Anlua(Native.newState(), handler);
        } catch (LuaException e) {
            handler.onError(e);
        }
        return null;
    }

    public Object[] doString(String s) {
        try {
            return Native.doString(ptr, s);
        } catch (LuaException e) {
            handler.onError(e);
        }
        return null;
    }

    public interface ErrorHandler {
        void onError(LuaException e);
    }

}

//        private static native void pushNil(long ptr);
//
//        private static native void pushString(long ptr, String s);
//
//        private static native void pushJavaClass(long ptr, Class<?> clz);
//
//        private static native void pushJavaObject(long ptr, Object obj);
//
//        private static native int type(long ptr, int idx);
//
//        private static native Object toObject(long ptr, int idx);