package leasses.anlua.api;

import android.util.SparseIntArray;

import androidx.annotation.NonNull;

// Hope it as fast as light,
// and as light as light.

// For Java side.
// Different class means different types.

@SuppressWarnings("ClassCanBeRecord") // for performance.
// Remember it's light after all :P
public class JLight {
    public static final SparseIntArray TYPES;
    // Primitive types
    public static final byte TYPE_B = 0;
    public static final byte TYPE_S = 1;
    public static final byte TYPE_I = 2;
    public static final byte TYPE_J = 3;
    public static final byte TYPE_F = 4;
    public static final byte TYPE_D = 5;
    public static final byte TYPE_Z = 6;
    public static final byte TYPE_C = 7;
    // Wrapping objects in  java.lang
    public static final byte TYPE_L_B = 8;
    public static final byte TYPE_L_S = 9;
    public static final byte TYPE_L_I = 10;
    public static final byte TYPE_L_J = 11;
    public static final byte TYPE_L_F = 12;
    public static final byte TYPE_L_D = 13;
    public static final byte TYPE_L_Z = 14;
    public static final byte TYPE_L_C = 15;
    private static final Z TRUE = new Z(true);
    private static final Z FALSE = new Z(false);
    // We cached 1000 integers.
    // I think it's enough :)
    private static final short NUMBERS_OFFSET = 500;
    private static final S[] NUMBERS = new S[NUMBERS_OFFSET * 2];

    static {
        for (var i = 0; i < NUMBERS.length; i++)
            NUMBERS[i] = new S((short) (-NUMBERS_OFFSET + i));

        TYPES = new SparseIntArray(JLight.TYPE_L_Z + 1);
        TYPES.put(byte.class.hashCode(), JLight.TYPE_B);
        TYPES.put(short.class.hashCode(), JLight.TYPE_S);
        TYPES.put(int.class.hashCode(), JLight.TYPE_I);
        TYPES.put(long.class.hashCode(), JLight.TYPE_J);
        TYPES.put(float.class.hashCode(), JLight.TYPE_F);
        TYPES.put(double.class.hashCode(), JLight.TYPE_D);
        TYPES.put(boolean.class.hashCode(), JLight.TYPE_Z);
        TYPES.put(Byte.class.hashCode(), JLight.TYPE_L_B);
        TYPES.put(Short.class.hashCode(), JLight.TYPE_L_S);
        TYPES.put(Integer.class.hashCode(), JLight.TYPE_L_I);
        TYPES.put(Long.class.hashCode(), JLight.TYPE_L_J);
        TYPES.put(Float.class.hashCode(), JLight.TYPE_L_F);
        TYPES.put(Double.class.hashCode(), JLight.TYPE_L_D);
        TYPES.put(Boolean.class.hashCode(), JLight.TYPE_L_Z);
    }

    private JLight() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static Z bool(boolean v) {
        return v ? TRUE : FALSE;
    }

    @NonNull
    public static Object /* JLight.S | JLight.D */ num(double v) {
        if ((short) v == v && v < NUMBERS_OFFSET && v > -NUMBERS_OFFSET)
            return NUMBERS[(int) (NUMBERS_OFFSET + v)];
        return new D(v);
    }
    
    /*
            B               byte
            S               short
            I               int
            J               long
            F               float
            D               double
            Z               boolean
            C               char
            L               Object
            Byte
            Short
            Integer
            Long
            Float
            Double
            Boolean
            Character
     */

    public static final class B {
        public final byte v;

         public B(byte v) {
            this.v = v;
        }
    }

    public static final class S {
        public final short v;

        public S(short v) {
            this.v = v;
        }
    }

    public static final class I {
        public final int v;

        public I(int v) {
            this.v = v;
        }
    }

    public static final class J {
        public final long v;

        public J(long v) {
            this.v = v;
        }
    }

    public static final class F {
        public final float v;

        public F(float v) {
            this.v = v;
        }
    }

    public static final class D {
        public final double v;

        public D(double v) {
            this.v = v;
        }
    }

    public static final class Z {
        public final boolean v;

        public Z(boolean v) {
            this.v = v;
        }
    }

    public static final class C {
        public final char v;

        public C(char v) {
            this.v = v;
        }
    }
}
