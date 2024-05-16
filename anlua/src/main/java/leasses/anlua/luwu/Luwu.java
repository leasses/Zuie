package leasses.anlua.luwu;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import dalvik.system.BaseDexClassLoader;
import leasses.dexcake.DexCake;
import leasses.dexcake.Type;
import leasses.anlua.api.CLight;
import leasses.anlua.api.JLight;
import leasses.util.ArrayShortList;

//  陆吾
// 《山海经·西次三经》：“昆仑之丘，是实惟帝之下都。神陆吾司之。”
public abstract class Luwu {
    public static final short NOT_FOUND = -1;
    private static final HashMap<Class<?>, Luwu> CACHED = new HashMap<>();
    private static ClassLoader classLoader;
    private static File dexDir;
    private final short[] ids;
    private final short classOff;
    private final short fieldOff;
    private final short exeOff;
    private final short tailOff;
    private final short tableLen;
    private final ArrayList<Collision> collisions;
    private final ArrayList<Executable> executables;

    protected Luwu(@NonNull String[] classes,
                   @NonNull String[] fields,
                   @NonNull String[] exes,
                   @NonNull Object[] types) {
        if (exes.length != types.length) throw new IllegalArgumentException();

        classOff = 1;
        fieldOff = (short) (classOff + classes.length);
        exeOff = (short) (fieldOff + fields.length);
        tailOff = (short) (exeOff + exes.length);

        tableLen = (short) ((-1 >>> Integer.numberOfLeadingZeros(tailOff - 1)) + 1);
        ids = new short[tableLen];

        collisions = new ArrayList<>(tailOff >> 2); // tailOff / 4
        executables = new ArrayList<>(exes.length << 2); // exes.length * 2

        // Introduction to array ids:
        // ---------------------------------------------------------------------------------------
        //      INDEX       |         VALUE(the id)
        //    hash(name)    |     =0 -> Available.
        //                  |     >0 -> Already taken. For this cause, we will replace it with
        //                  |           an index in collisions list, and use the contrary to it
        //                  |           in order to recognise.
        //                  |     <0 -> Already taken like the cause above.
        // ----------------------------------------------------------------------------------------
        String name;
        short id = 1;

        for (var i = classOff; i < tailOff; i++) {
            if (i < fieldOff) name = classes[i - classOff];
            else if (i < exeOff) name = fields[i - fieldOff];
            else name = exes[i - exeOff];

            var index = (tableLen - 1) & hash(name);
            var v = ids[index];

            if (v == 0) {
                ids[index] = id;

            } else if (v < 0) {
                collisions.get(-v - 1).put(name, id);

            } else {
                ids[index] = (short) (-collisions.size() - 1);
                collisions.add(new Collision(v, name, id));
            }

            if (i >= exeOff) {
                var e = Executable.obtain(types[i - exeOff], id);
                executables.add(e);
                for (var j = 1; j < e.count(); j++) executables.add(null);
                id += e.count();
            } else id++;
        }

//        Lg.d(executables);
    }

    public static void init(@NonNull ClassLoader loader, @NonNull File dir) {
        if (classLoader != null)
            throw new IllegalStateException("Already initialized. ");

        dexDir = dir;
        classLoader = loader;
    }

    @NonNull
    public static Luwu proxy(@NonNull Class<?> clz) throws Exception {
        if (clz.isArray() || clz.isPrimitive())
            throw new UnsupportedOperationException("Cannot index for an array or " +
                    "a primitive type. ");

        Luwu cache;
        if ((cache = CACHED.get(clz)) != null)
            return cache;

        if (classLoader == null)
            throw new IllegalStateException("classLoader is null. " +
                    "Have you called init(...) first? ");

        ///////////////////////////////// SHARED VARIABLES //////////////////////////////////
        var access = new File(dexDir, "leasses.anlua." + clz.getName() + "Access");

        // Inner classes
        Class<?>[] cs;
        // Fields
        Field[] fs;
        // Introduction to variable es
        // It maps the method names to their types, including parameter types and return type.
        // The name of constructor is specified as null.
        // |------------------------------ STRUCTURE --------------------------------|
        // |        KEY           |                    VALUE                         |
        // |  <the method name>   | ArrayList [ Class[<two for static method,        |
        // |                      |                   otherwise one>]                |
        // |                      |                      {<return type, or the null  |
        // |                      |                       for constructor>,          |
        // |                      |                       <null for static method>}, |
        // |                      |             Class[ ] {<one parameter types of    |
        // |                      |                       overloaded method>},       |
        // |                      |             Class[ ] {<one parameter types of    |
        // |                      |                      overloaded method>},        |
        // |                      |             <so on> ... ]                        |
        // | <so on> ----------------------------------------------------------------|
        // Executables
        HashMap<String, ArrayList<Class<?>[]>> es;
        final var metadataIdx = 0;
        final var staticLen = 2;
        final var returnTypeIdx = 0;

        var dex = new DexCake(
                Modifier.PUBLIC | Modifier.FINAL,
                Type.of(access.getName()), Type.of(Luwu.class));

        // Useful variables
        String name;
        // Used to create loop indexing variables
        short i;
        short j;
        short k;
        {/////////////////////////////////// SORT MEMBERS ////////////////////////////////////
            // Public only
            cs = clz.getClasses();
            // Public only
            fs = clz.getFields();

            var exes = new HashMap<String, ArrayList<Class<?>[]>>(50);

            ArrayList<Class<?>[]> e;

            // Public only
            for (var c : clz.getConstructors()) {

                if ((e = exes.get(null)) == null) {
                    e = new ArrayList<>();
                    e.add(new Class[Modifier.isStatic(c.getModifiers()) ? 2 : 1]);
                    exes.put(null, e);
                }

                e.add(c.getParameterTypes());
            }

            // Public only
            for (var m : clz.getMethods()) {
                name = m.getName();

                if ((e = exes.get(name)) == null) {
                    e = new ArrayList<>();
                    e.add(new Class[Modifier.isStatic(m.getModifiers()) ? 2 : 1]);
                    e.get(metadataIdx)[returnTypeIdx] = m.getReturnType();
                    exes.put(name, e);
                }

                e.add(m.getParameterTypes());
            }

            es = exes;
        }
        {////////////////////////////// GENERATE CONSTRUCTOR ///////////////////////////////
            dex.constructor(Modifier.PUBLIC);
            // Used to pass arguments when calling super's constructor
            final var /* register */ self = 0;
            final var /* register */ classNames = 1;
            final var /* register */ fieldNames = 2;
            final var /* register */ exeNames = 3;
            final var /* register */ exeTypes = 4;

            // Used to operate arrays
            final var /* register */ index = 5;
            final var /* register */ length = 5;
            final var /* register */ element = 6;
            final var maxElements = 0xff - element;

            // Inner class names and field names

            // Now we have a class C, it declares two inner classes.
            // One is static named A, and another one is non-static named B.
            // To new an instance of A, we use new C.A();
            // To new an instance of B, we use new C().new B();
            // So here, we filter those static but doesn't declared in this class.
            i = 0;
            for (var c : cs) {
                if (Modifier.isStatic(c.getModifiers()) && c.getDeclaringClass() != clz) continue;

                if (i == maxElements + 1) {
                    dex.lateConst16(length)
                            .newArray(Type.STRING_ARR, classNames, length);
                    for (j = 0; j < maxElements + 1; j++)
                        dex.const16(index, j)
                                .aPutObject(element + j, classNames, index);
                }

                if (i > maxElements) dex.constString(element, c.getSimpleName())
                        .const16(index, i)
                        .aPutObject(element, classNames, index);
                else dex.constString(element + i, c.getSimpleName());

                cs[i++] = c;
            }

            if (i == 0) // Zero eligible inner class.
                dex.const16(length, 0).newArray(Type.STRING_ARR, classNames, length);
            else {
                // To filter ineligible  inner classes.
                if (i < cs.length) cs[i] = null;

                if (i <= maxElements)
                    dex.filledNewArrayRange(Type.STRING_ARR, element, element - 1 + i)
                            .moveResultObject(classNames);
                else dex.const16late(i);
            }

            // Same as above.
            i = 0;
            for (var f : fs) {
                if (Modifier.isStatic(f.getModifiers()) && f.getDeclaringClass() != clz) continue;

                if (i == maxElements + 1) {
                    dex.lateConst16(length)
                            .newArray(Type.STRING_ARR, fieldNames, length);
                    for (j = 0; j < maxElements + 1; j++)
                        dex.const16(index, j)
                                .aPutObject(element + j, fieldNames, index);
                }

                if (i > maxElements) dex.constString(element, fs[i].getName())
                        .const16(index, i)
                        .aPutObject(element, fieldNames, index);

                else dex.constString(element + i, fs[i].getName());

                fs[i++] = f;
            }

            if (i == 0) // Zero eligible fields.
                dex.const16(length, 0).newArray(Type.STRING_ARR, fieldNames, length);
            else {
                // To filter ineligible fields.
                if (i < fs.length) fs[i] = null;

                if (i <= maxElements)
                    dex.filledNewArrayRange(Type.STRING_ARR, element, element - 1 + i)
                            .moveResultObject(fieldNames);
                else dex.const16late(i);
            }

            // Executable names and parameter types
            dex.lateConst16(length)
                    .newArray(Type.OBJECT_ARR, exeTypes, length)
                    .newArray(Type.STRING_ARR, exeNames, length);

            i = 0;
            for (var e : es.entrySet()) {
                name = e.getKey();
                dex.constString(element, name);

                dex.const16(index, i++).aPutObject(element, exeNames, index);

                var value = e.getValue();

                // Meta data takes one position.
                // The case which the method isn't overloaded.
                if (value.size() == 2) {
                    Class<?>[] tp = value.get(1);

                    if (tp.length == 0)
                        dex.const4(element, 0);

                    else if (tp.length == 1)
                        dex.constClass(element, Type.of(tp[0]));

                    else {
                        for (j = 0; j < tp.length; j++)
                            dex.constClass(element + j, Type.of(tp[j]));

                        dex.filledNewArrayRange(Type.CLASS_ARR,
                                        element, element + --j)
                                .moveResultObject(element);
                    }

                    dex.aPutObject(element, exeTypes, index);
                    continue;
                }

                for (k = 0; k < value.size() - 1; k++) {
                    var tp = value.get(k + 1);

                    if (tp.length == 0) {
                        dex.const4(element + k, 0);
                        continue;
                    }

                    // <element+k> = <type>
                    if (tp.length == 1) {
                        dex.constClass(element + k, Type.of(tp[0]));
                        continue;
                    }

                    for (j = 0; j < tp.length; j++)
                        dex.constClass(element + k + j, Type.of(tp[j]));

                    // <element+k> = new Class[]{ from <element+k> to <element+k+j-1> }
                    dex.filledNewArrayRange(Type.CLASS_ARR,
                                    element + k, element + k + --j)
                            .moveResultObject(element + k);
                }

                // <element> = new Object[]{ from <element> to <element+k-1> }
                dex.filledNewArrayRange(Type.OBJECT_ARR, element, element + --k)
                        .moveResultObject(element)
                        .aPutObject(element, exeTypes, index);
            }
            dex.const16late(i);

            // Call super
            dex.moveObjectFrom16(-1, self)
                    .invokeDirect(Type.of(Luwu.class), "<init>", Type.VOID,
                            new int[]{self, classNames, fieldNames, exeNames, exeTypes},
                            Type.STRING_ARR, Type.STRING_ARR, Type.STRING_ARR, Type.OBJECT_ARR)
                    .returnVoid()
                    .endMethod();
        }
        {////////////////////////////////// GENERATE ACCESSOR //////////////////////////////////
            dex.method(Modifier.PUBLIC,
                    "access",
                    Type.of(CLight.class),
                    Type.OBJECT, Type.SHORT, Type.OBJECT_ARR);

            final var /* register v0 */ obj = 0;
            final var /* register v1 */ id = 1;
            final var /* register v2 */ args = 2;
            final var /* register v3 */ ret = 3;
            final var /* register v3 */ exception = 3;
            final var /* label */ switchData = "d";
            final var /* label */ switchBranch = "b";

            dex.moveObjectFrom16(-2, obj)
                    .moveFrom16(-3, id)
                    .moveObjectFrom16(-4, args)

                    .checkCast(obj, Type.of(clz))
                    .packedSwitch(id, switchData);

            dex.newInstance(exception, Type.ILLEGAL_ARGUMENT_EXCEPTION)
                    .invokeDirect(Type.ILLEGAL_ARGUMENT_EXCEPTION, "<init>", Type.VOID,
                            new int[]{exception})
                    .throw0(exception);

            i = 1;

            for (var c : cs) {
                if (c == null) break;

                dex.label(switchBranch, i++)
                        .constClass(ret, Type.of(c))
                        .wrap(ret, Type.CLASS)
                        .moveResultObject(ret)
                        .returnObject(ret);
            }

            for (var f : fs) {
                if (f == null) break;

                dex.label(switchBranch, i++);

                var tp = Type.of(f.getType());
                var dc = Type.of(f.getDeclaringClass());
                if (Modifier.isStatic(f.getModifiers())) {
                    dex.sGet(ret, dc, f.getName(), tp);
                } else {
                    dex.iGet(obj, ret, dc, f.getName(), tp);
                }

                dex.wrap(ret, tp).moveResultObject(ret).returnObject(ret);
            }

            final var index = 4;
            final var argFrom = 5;
            for (var e : es.entrySet()) {
                var overloadTypes = e.getValue();
                var isStatic = overloadTypes.get(metadataIdx).length == staticLen;
                var returnType = overloadTypes.get(metadataIdx)[returnTypeIdx];

                for (k = 1; k < overloadTypes.size(); k++) {
                    var types = overloadTypes.get(k);
                    dex.label(switchBranch, i++);
//                            .checkCast(args, Type.OBJECT_ARR);

                    var argEnd = argFrom;
                    for (j = 0; j < types.length; j++) {
                        dex.const16(index, j)
                                .aGetObject(argEnd, args, index);

                        var t = types[j];

                        if (t == byte.class)
                            dex.checkCast(argEnd, Type.of(JLight.B.class)).iGet(argEnd, argEnd, Type.of(JLight.B.class), "v", Type.BYTE);
                        else if (t == short.class)
                            dex.checkCast(argEnd, Type.of(JLight.S.class)).iGet(argEnd, argEnd, Type.of(JLight.S.class), "v", Type.SHORT);
                        else if (t == int.class)
                            dex.checkCast(argEnd, Type.of(JLight.I.class)).iGet(argEnd, argEnd, Type.of(JLight.I.class), "v", Type.INT);
                        else if (t == long.class)
                            dex.checkCast(argEnd, Type.of(JLight.J.class)).iGet(argEnd, argEnd++, Type.of(JLight.J.class), "v", Type.LONG);
                        else if (t == float.class)
                            dex.checkCast(argEnd, Type.of(JLight.F.class)).iGet(argEnd, argEnd, Type.of(JLight.F.class), "v", Type.FLOAT);
                        else if (t == double.class)
                            dex.checkCast(argEnd, Type.of(JLight.D.class)).iGet(argEnd, argEnd++, Type.of(JLight.D.class), "v", Type.DOUBLE);
                        else if (t == boolean.class)
                            dex.checkCast(argEnd, Type.of(JLight.Z.class)).iGet(argEnd, argEnd, Type.of(JLight.Z.class), "v", Type.BOOL);
                        else dex.checkCast(argEnd, Type.of(t));

                        argEnd++;
                    }

                    var t = new Type[types.length];
                    for (j = 0; j < types.length; j++) t[j] = Type.of(types[j]);

                    argEnd--;
                    if (e.getKey() == null) // Constructor
                        dex.newInstance(argFrom - 1, Type.of(clz))
                                .invokeDirectRange(Type.of(clz), "<init>", Type.VOID,
                                        argFrom - 1, argEnd,
                                        t)
                                .wrap(argFrom - 1, Type.of(clz))
                                .moveResultObject(ret)
                                .returnObject(ret);
                    else {
                        if (isStatic)
                            if (argEnd == argFrom - 1) // Zero parameter
                                dex.invokeStatic(Type.of(clz), e.getKey(), Type.of(returnType),
                                        new int[0]);
                            else
                                dex.invokeStaticRange(Type.of(clz), e.getKey(), Type.of(returnType),
                                        argFrom, argEnd,
                                        t);
                        else
                            dex.moveObject(obj, argFrom - 1)
                                    .invokeVirtualRange(Type.of(clz), e.getKey(), Type.of(returnType),
                                            argFrom - 1, argEnd,
                                            t);

                        if (returnType == void.class)
                            dex.const4(ret, 0)
                                    .returnObject(ret);

                        else {
                            if (returnType == int.class ||
                                    returnType == float.class ||
                                    returnType == byte.class ||
                                    returnType == short.class ||
                                    returnType == boolean.class ||
                                    returnType == char.class) {
                                dex.moveResult(ret);

                            } else if (returnType == long.class ||
                                    returnType == double.class) {
                                dex.moveResultWide(ret);

                            } else
                                dex.moveResultObject(ret);

                            dex.wrap(ret, Type.of(returnType))
                                    .moveResultObject(ret)
                                    .returnObject(ret);
                        }
                    }
                }
            }

            dex.label(switchData)
                    .packedSwitchData(1);
            for (j = 1; j < i; j++) dex.label(switchBranch, j);
            dex.endPackedSwitchData().endMethod();
        }

//        var s = dex.build();
//        Lg.d(s);

        dex.strawberry(access);

        CACHED.put(clz,
                (cache = (Luwu) new BaseDexClassLoader(
                        access.getPath(),
                        dexDir,
                        null,
                        classLoader)
                        .loadClass(access.getName())
                        .getDeclaredConstructor()
                        .newInstance()
                )
        );
        return cache;
    }

    private static int hash(@Nullable String str) {
        int h;
        return str == null ? 0 : (h = str.hashCode()) ^ (h >>> 16);
    }

    public abstract CLight access(@Nullable Object obj,
                                  short id,
                                  @Nullable Object[] args)
            throws Throwable;

    private short index(@Nullable String name, short min, short max) {
        var i = (tableLen - 1) & hash(name);
        var v = ids[i];

        if (v > 0) return (v >= min && v < max) ? v : NOT_FOUND;
        if (v == 0) return NOT_FOUND;
        return collisions.get(-v - 1).get(name, min, max);
    }

    public short indexClass(@NonNull String name) {
        return index(name, classOff, fieldOff);
    }

    public short indexField(@NonNull String name) {
        return index(name, fieldOff, exeOff);
    }

    @Nullable
    public Executable indexExecutable(@Nullable String name) {
        var index = index(name, exeOff, tailOff);
        if (index == NOT_FOUND) return null;
        return executables.get(index - exeOff);
    }

    private static class Collision {
        private final ArrayList<String> names = new ArrayList<>(4);
        private final ArrayShortList ids = new ArrayShortList(4);

        private Collision(short id1, @Nullable String name, short id2) {
            ids.add(id1);
            ids.add(id2);
            names.add(name);
        }

        private void put(@Nullable String name, short id) {
            names.add(name);
            ids.add(id);
        }

        private short get(@Nullable String name, short min, short max) {
            short id;

            for (var i = 0; i < names.size(); i++)
                if (Objects.equals(names.get(i), name) &&
                        ((id = ids.get(i + 1)) >= min && id < max))
                    return id;

            if ((id = ids.get(0)) >= min && id < max)
                return id;
            else
                return NOT_FOUND;
        }
    }
}
