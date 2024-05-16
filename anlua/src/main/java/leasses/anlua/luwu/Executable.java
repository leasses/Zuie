package leasses.anlua.luwu;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import leasses.anlua.api.JLight;

public final class Executable {
    // We cached those having zero param and not overloaded.
    private static final SparseArray<Executable> CACHED = new SparseArray<>(100);
    @Nullable
    private final Object[] types;
    private final short headId;
    private final boolean overloaded;

    // When the parameter types is
    // null:                  Class:                  Class[]:                 Class[][]:
    // {                      {                       {                        {
    //   {},                    { <type> },             {...},                   {...},
    // }                      }                       }                          {...},
    // overloaded = false     overloaded = false      overloaded = false         ...
    //                                                                         }
    //                                                                         overloaded = true
    // To have a happier life, we combined the second and third causes.

    // null Class Class[] Object[]
    private Executable(@Nullable Object type, short headId) {
        this.headId = headId;

        if (type != null && type.getClass() == Object[].class) {
            Object[] t = (Object[]) type;
            this.types = new Class<?>[t.length][];
            for (byte i = 0; i < t.length; i++)
                this.types[i] = applyType(t[i]);

            this.overloaded = true;
            return;
        }

        this.types = applyType(type);
        this.overloaded = false;
    }

    @Nullable
    private static Class<?>[] applyType(@Nullable Object type) {
        if (type == null)
            return null;

        if (type instanceof Class<?>[] t)
            return t;

        if (type instanceof Class<?> t)
            return new Class[]{t};

        throw new IllegalArgumentException();
    }

    private static boolean incompatible(@Nullable Class<?>[] params,
                                        @Nullable Object[] args) {
        if (params == null || args == null) return params != args;

        if (params.length != args.length) return true;

        for (var i = 0; i < params.length; i++) {
            var expected = params[i];
            var arg = args[i];

            if (arg == null) {
                if (expected.isPrimitive()) return true;
                else continue;
            }

            var actual = arg.getClass();
            if (!expected.isAssignableFrom(actual)) return true;

            // Numbers have only two classes, both JLight.D and JLight.S.
            // Conversion details in JLight.num(...)
            if (!switch (JLight.TYPES.get(expected.hashCode(), -1)) {

                case JLight.TYPE_B, JLight.TYPE_L_B ->
                        (arg instanceof JLight.D a && (byte) a.v == a.v) ||
                                (arg instanceof JLight.S b && (byte) b.v == b.v);

                case JLight.TYPE_S, JLight.TYPE_L_S ->
                        (arg instanceof JLight.D a && (short) a.v == a.v) ||
                                arg instanceof JLight.S;

                case JLight.TYPE_I, JLight.TYPE_L_I ->
                        arg instanceof JLight.D a && (int) a.v == a.v ||
                                arg instanceof JLight.S;

                case JLight.TYPE_J, JLight.TYPE_L_J ->
                        (arg instanceof JLight.D a && (long) a.v == a.v) ||
                                arg instanceof JLight.S;

                case JLight.TYPE_F, JLight.TYPE_L_F ->
                        (arg instanceof JLight.D a && (float) a.v == a.v) ||
                                arg instanceof JLight.S;

                case JLight.TYPE_D, JLight.TYPE_L_D ->
                        arg instanceof JLight.D || arg instanceof JLight.S;

                case JLight.TYPE_Z, JLight.TYPE_L_Z -> arg instanceof JLight.Z;

                case JLight.TYPE_C, JLight.TYPE_L_C -> arg instanceof JLight.C ||
                        (arg instanceof CharSequence s && s.length() == 1);

                default -> false;

            }) return true;
        }

        return false;
    }

    private static void correct(@Nullable Class<?>[] params,
                                @Nullable Object[] args) {
        if (params == null || args == null) return;

        for (var i = 0; i < params.length; i++) {
            var expected = params[i];
            var arg = args[i];

            if (arg instanceof JLight.D a)
                args[i] = switch (JLight.TYPES.get(expected.hashCode(), -1)) {

                    case JLight.TYPE_B -> new JLight.B((byte) a.v);
                    case JLight.TYPE_L_B -> (byte) a.v;

                    case JLight.TYPE_S -> new JLight.S((short) a.v);
                    case JLight.TYPE_L_S -> (short) a.v;

                    case JLight.TYPE_I -> new JLight.I((int) a.v);
                    case JLight.TYPE_L_I -> (int) a.v;

                    case JLight.TYPE_J -> new JLight.J((long) a.v);
                    case JLight.TYPE_L_J -> (long) a.v;

                    case JLight.TYPE_F -> new JLight.F((float) a.v);
                    case JLight.TYPE_L_F -> (float) a.v;

                    case JLight.TYPE_D -> new JLight.D(a.v);
                    case JLight.TYPE_L_D -> a.v;

                    case JLight.TYPE_C -> new JLight.C((char) a.v);
                    case JLight.TYPE_L_C -> (char) a.v;

                    default -> arg;
                };

            if (arg instanceof JLight.S a)
                args[i] = switch (JLight.TYPES.get(expected.hashCode(), -1)) {

                    case JLight.TYPE_B -> new JLight.B((byte) a.v);
                    case JLight.TYPE_L_B -> (byte) a.v;

                    case JLight.TYPE_S -> new JLight.S(a.v);
                    case JLight.TYPE_L_S -> (short) a.v;

                    case JLight.TYPE_I -> new JLight.I(a.v);
                    case JLight.TYPE_L_I -> (int) a.v;

                    case JLight.TYPE_J -> new JLight.J(a.v);
                    case JLight.TYPE_L_J -> (long) a.v;

                    case JLight.TYPE_F -> new JLight.F(a.v);
                    case JLight.TYPE_L_F -> (float) a.v;

                    case JLight.TYPE_D -> new JLight.D(a.v);
                    case JLight.TYPE_L_D -> a.v;

                    case JLight.TYPE_C -> new JLight.C((char) a.v);
                    case JLight.TYPE_L_C -> (char) a.v;

                    default -> arg;
                };

            if (arg instanceof JLight.Z a)
                args[i] = switch (JLight.TYPES.get(expected.hashCode(), -1)) {

                    case JLight.TYPE_Z -> new JLight.Z(a.v);
                    case JLight.TYPE_L_Z -> a.v;

                    default -> arg;
                };

            if (arg instanceof CharSequence a)
                args[i] = switch (JLight.TYPES.get(expected.hashCode(), -1)) {

                    case JLight.TYPE_C -> new JLight.C(a.charAt(0));
                    case JLight.TYPE_L_C -> a.charAt(0);

                    default -> arg;
                };
        }
    }

    @NonNull
    static Executable obtain(@Nullable Object type, short headId) {
        if (type != null) return new Executable(type, headId);

        Executable cache;
        if ((cache = CACHED.get(headId)) != null) return cache;
        CACHED.put(headId, (cache = new Executable(null, headId)));
        return cache;
    }

    public short matchType(@Nullable Object[] args) throws NoSuchMethodException {
        if (types == null) { // Case of null
            if (args != null)
                throw new NoSuchMethodException(notFound(args));

            return headId;
        }

        if (!overloaded) { // Case of Class[]
            var singleTypes = (Class<?>[]) types;
            if (incompatible(singleTypes, args))
                throw new NoSuchMethodException(notFound(args));

            correct(singleTypes, args);
            return headId;
        }

        // Case of Class[][]
        var overloadedTypes = (Class<?>[][]) types;
        for (byte i = 0; i < overloadedTypes.length; i++) {
            if (incompatible(overloadedTypes[i], args)) continue;

            correct(overloadedTypes[i], args);
            return (short) (headId + i);
        }

        throw new NoSuchMethodException(notFound(args));
    }

    @NonNull
    private String notFound(@Nullable Object[] args) {
        var sb = new StringBuilder();

        sb.append("cannot match types for arguments {\n");
        if (args != null) for (var a : args)
            sb.append(a)
                    .append(" : ")
                    .append((a == null ? Void.TYPE : a.getClass()).getName())
                    .append(", \n");

        sb.append("}\navailable types: {\n");

        if (types == null) // Case of null
            sb.append("[],\n");

        else if (!overloaded) { // Case of Class[]
            sb.append("[ ");
            for (var t : (Class<?>[]) types)
                sb.append(t.getSimpleName()).append(" ");
            sb.append("],\n");

        } else { // Case of Class[][]
            for (var type : (Class<?>[][]) types) {
                sb.append("[ ");
                if (type != null) for (var t : type)
                    sb.append(t.getSimpleName()).append(" ");
                else sb.append("null(zero param) ");
                sb.append("],\n");
            }
        }

        return sb.append('}').toString();
    }

    @NonNull
    @Override
    public String toString() {
        var sb = new StringBuilder(overloaded ? 666 : 123)
                .append(getClass().getName())
                .append('\n');

        if (types == null) // Case of null
            sb.append(headId).append("\t\t--> null(zero param & not overloaded)").append('\n');

        else if (!overloaded) { // Case of Class[]
            var singleTypes = (Class<?>[]) types;

            sb.append(headId).append("\t\t--> [ ");
            for (var t : singleTypes)
                sb.append(t.getSimpleName()).append(" ");
            sb.append("]\n");

        } else { // Case of Class[][]
            var overloadedTypes = (Class<?>[][]) types;

            for (var i = 0; i < overloadedTypes.length; i++) {
                sb.append(headId + i).append("\t\t--> [ ");
                if (overloadedTypes[i] != null) for (var t : overloadedTypes[i])
                    sb.append(t.getSimpleName()).append(" ");
                else sb.append("null(zero param) ");
                sb.append("]\n");
            }
        }

        return sb.toString();
    }

    byte count() {
        if (types == null || !overloaded) return 1;

        return (byte) ((Class<?>[][]) types).length;
    }
}
