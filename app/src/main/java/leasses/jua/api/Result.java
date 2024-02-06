package leasses.jua.api;

import androidx.annotation.NonNull;

import leasses.jua.Jua;

public class Result {
    private static final Byte TYPE_NONE = Byte.of(Jua.LUA_TYPE_NONE);
    private static final Byte TYPE_NIL = Byte.of(Jua.LUA_TYPE_NIL);
    private static final Byte TYPE_NUM = Byte.of(Jua.LUA_TYPE_NUMBER);
    private static final Byte TYPE_BOOL = Byte.of(Jua.LUA_TYPE_BOOLEAN);
    private static final Byte TYPE_STR = Byte.of(Jua.LUA_TYPE_STRING);
    private static final Byte TYPE_UD = null;

    private Result() {
    }

    @NonNull
    public static Object[] err(String err) {
        return new Object[]{TYPE_NONE, err};
    }

    @NonNull
    public static Object[] wrap(Object v) {
        if (v == null)
            return new Object[]{TYPE_NIL};

        if (v instanceof Number) {
            return new Object[]{TYPE_NUM, Num.of(((Number) v).doubleValue())};
        } else if (v instanceof Boolean) {
            return new Object[]{TYPE_BOOL, Byte.of((byte) ((boolean) v ? 1 : 0))};
        } else if (v instanceof String || v instanceof Character) {
            return new Object[]{TYPE_STR, v};
        }
        return new Object[]{TYPE_UD, v};
    }


    public static class Num {
        public final double value;

        private Num(double value) {
            this.value = value;
        }

        @NonNull
        private static Num of(double value) {
            return new Num(value);
        }
    }

    public static class Byte {
        public final byte value;

        private Byte(byte value) {
            this.value = value;
        }

        @NonNull
        private static Byte of(byte value) {
            return new Byte(value);
        }
    }
}
