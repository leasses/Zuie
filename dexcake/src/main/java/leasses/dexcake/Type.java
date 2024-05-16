package leasses.dexcake;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public final class Type {
    public static final Type DOUBLE = new Type(0, 'D', null);
    public static final Type FLOAT = new Type(0, 'F', null);
    public static final Type LONG = new Type(0, 'J', null);
    public static final Type INT = new Type(0, 'I', null);
    public static final Type SHORT = new Type(0, 'S', null);
    public static final Type CHAR = new Type(0, 'C', null);
    public static final Type BYTE = new Type(0, 'B', null);
    public static final Type BOOL = new Type(0, 'Z', null);
    public static final Type VOID = new Type(0, 'V', null);
    private static final String ARRAY_PREFIX = "[";
    private static final char OBJECT_PREFIX = 'L';
    private static final char OBJECT_SUFFIX = ';';
    private static final HashMap<Class<?>, Type> CACHED = new HashMap<>();
    public static final Type CLASS = of(Class.class);
    public static final Type CLASS_ARR = CLASS.arr();
    public static final Type CLASS_ARR2 = CLASS.arr(2);
    public static final Type CLASS_ARR3 = CLASS.arr(3);
    public static final Type STRING = of(String.class);
    public static final Type STRING_ARR = STRING.arr();
    public static final Type OBJECT = of(Object.class);
    public static final Type OBJECT_ARR = OBJECT.arr();
    public static final Type NUMBER = of(Number.class);
    public static final Type ILLEGAL_ARGUMENT_EXCEPTION = of(IllegalArgumentException.class);
    private final byte arr;
    private final char t;
    @Nullable
    private final String clz;

    Type(int arr, char t, @Nullable String clz) {
        this.arr = (byte) arr;
        this.t = t;
        this.clz = clz;
    }

    @NonNull
    public static Type of(@NonNull Class<?> clz) {
        var r=clz;
        Type t;
        if ((t = CACHED.get(clz)) != null) return t;

        var arr = 0;
        var ct = clz;

        while ((ct = ct.getComponentType()) != null) {
            clz = ct;
            arr++;
        }

        if (clz.isPrimitive()) {
            if (clz == boolean.class) return BOOL.arr(arr);
            if (clz == byte.class) return BYTE.arr(arr);
            if (clz == char.class) return CHAR.arr(arr);
            if (clz == short.class) return SHORT.arr(arr);
            if (clz == int.class) return INT.arr(arr);
            if (clz == long.class) return LONG.arr(arr);
            if (clz == float.class) return FLOAT.arr(arr);
            if (clz == double.class) return DOUBLE.arr(arr);
            return VOID.arr(arr);
        }

        CACHED.put(clz, (t = new Type(
                arr, OBJECT_PREFIX, clz.getName().replace('.', '/')
        )));

        return t;
    }

    @NonNull
    public static Type of(@NonNull String clz) {
        return new Type(
                0, OBJECT_PREFIX, clz.replace('.', '/')
        );
    }

    @NonNull
    public Type arr() {
        return arr(1);
    }

    @NonNull
    Type arr(int deep) {
        if (deep == 0) return this;
        return new Type(arr + deep, t, clz);
    }

    @NonNull
    String fitInstruction(
            @NonNull String forNormal,
            @NonNull String forBoolean,
            @NonNull String forByte,
            @NonNull String forChar,
            @NonNull String forObject,
            @NonNull String forShort,
            @NonNull String forWide
    ) {
        if (t == 'F' || t == 'I') return forNormal;
        if (t == 'Z') return forBoolean;
        if (t == 'B') return forByte;
        if (t == 'C') return forChar;
        if (t == 'L') return forObject;
        if (t == 'S') return forShort;
        if (t == 'D' || t == 'J') return forWide;
        throw new RuntimeException();
    }

    boolean isWide() {
        return t == 'D' || t == 'J';
    }

    boolean isPrimitive() {
        return (t != 'L') && arr == 0;
    }

    @NonNull
    @Override
    public String toString() {
        var sb = new StringBuilder(arr + (clz == null ? 0 : clz.length()) + 5);

        sb.append(ARRAY_PREFIX.repeat(arr)).append(t);

        if (t == OBJECT_PREFIX)
            sb.append(clz).append(OBJECT_SUFFIX);

        return sb.toString();
    }
}
