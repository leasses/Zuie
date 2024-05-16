package leasses.anlua.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import leasses.anlua.Native;

// Hope it as fast as light,
// and as light as light.

// A value wrapper used to transport simple values between Java and Lua through C.
// It was designed for the internal data transportation of AnluaApi.

// For C side.
// We set two fields to recognise its type.
// The num field used to store the number value while the obj field is null,
// or to store other types of values while the obj field is not null.

public class CLight {
    private static final byte TYPE_ERR = Native.getLightTypeErr();
    private static final byte TYPE_NULL = Native.getLightTypeNull();
    private static final byte TYPE_TRUE = Native.getLightTypeTrue();
    private static final byte TYPE_FALSE = Native.getLightTypeFalse();
    private static final byte TYPE_STR = Native.getLightTypeStr();
    private static final byte TYPE_OBJ = Native.getLightTypeObj();

    private static final Object SPACE = new Object();
    private static final CLight TRUE = new CLight(TYPE_TRUE, SPACE);
    private static final CLight FALSE = new CLight(TYPE_FALSE, SPACE);
    // We cached 1000 integers.
    // I think it's enough :)
    private static final short NUMBERS_OFFSET = 500;
    private static final CLight[] NUMBERS = new CLight[NUMBERS_OFFSET * 2];

    static {
        for (var i = 0; i < NUMBERS.length; i++)
            NUMBERS[i] = new CLight(-NUMBERS_OFFSET + i, null);
    }

    private final double num;
    @Nullable
    private final Object obj;

    private CLight(double num, @Nullable Object obj) {
        this.num = num;
        this.obj = obj;
    }

    @NonNull
    public static CLight err(String err) {
        return new CLight(TYPE_ERR, err);
    }

    @NonNull
    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    public static CLight of(Object v) {
        if (v == null)
            return new CLight(TYPE_NULL, SPACE);

        if (v instanceof Number) {
            return new CLight(((Number) v).doubleValue(), null);

        } else if (v instanceof Boolean b) {
            return new CLight(b ? TYPE_TRUE : TYPE_FALSE, SPACE);

        } else if (v instanceof String // For better performance
                || v instanceof Character
                || v instanceof CharSequence) {
            return new CLight(TYPE_STR, String.valueOf(v));
        }

        return new CLight(TYPE_OBJ, v);
    }

    @NonNull
    public static CLight of(boolean v) {
        return v ? TRUE : FALSE;
    }

    @NonNull
    public static CLight of(byte v) {
        return NUMBERS[NUMBERS_OFFSET + v];
    }

    @NonNull
    public static CLight of(char v) {
        return new CLight(TYPE_STR, String.valueOf(v));
    }

    @NonNull
    public static CLight of(short v) {
        if (v < NUMBERS_OFFSET && v > -NUMBERS_OFFSET) return NUMBERS[NUMBERS_OFFSET + v];
        return new CLight(v, null);
    }

    @NonNull
    public static CLight of(int v) {
        if (v < NUMBERS_OFFSET && v > -NUMBERS_OFFSET) return NUMBERS[NUMBERS_OFFSET + v];
        return new CLight(v, null);
    }

    @NonNull
    public static CLight of(long v) {
        if (v < NUMBERS_OFFSET && v > -NUMBERS_OFFSET)
            return NUMBERS[(int) (NUMBERS_OFFSET + v)];
        return new CLight(v, null);
    }

    @NonNull
    public static CLight of(float v) {
        if ((short) v == v && v < NUMBERS_OFFSET && v > -NUMBERS_OFFSET)
            return NUMBERS[(int) (NUMBERS_OFFSET + v)];
        return new CLight(v, null);
    }

    @NonNull
    public static CLight of(double v) {
        if ((short) v == v && v < NUMBERS_OFFSET && v > -NUMBERS_OFFSET)
            return NUMBERS[(int) (NUMBERS_OFFSET + v)];
        return new CLight(v, null);
    }

    @NonNull
    @Override
    public String toString() {
        if (obj == null) return "TYPE_NUM:" + num;

        if ((int) num == TYPE_ERR) {
            return "TYPE_ERR";
        } else if ((int) num == TYPE_NULL) {
            return "TYPE_NULL";
        } else if ((int) num == TYPE_TRUE) {
            return "TYPE_TRUE";
        } else if ((int) num == TYPE_FALSE) {
            return "TYPE_FALSE";
        } else if ((int) num == TYPE_STR) {
            return "TYPE_STR: " + obj;
        } else if ((int) num == TYPE_OBJ) {
            return "TYPE_OBJ: " + obj;
        }
        return "Unknown";
    }
}
