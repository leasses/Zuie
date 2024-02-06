package leasses.jua.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import leasses.zuie.log.Lg;

public class ReflectFactory {

    private ReflectFactory() {
    }

    @Nullable
    public static Field getField(@NonNull Object obj,
                                 @NonNull String name) {
        for (Field f :
                ((Class<?>) (obj instanceof Class ? obj : obj.getClass()))
                        .getFields()) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    @Nullable
    public static Class<?> getInnerClass(@NonNull Object obj,
                                         @NonNull String name) {
        Class<?> clz;
        if (obj instanceof Class)
            clz = (Class<?>) obj;
        else
            clz = obj.getClass();

        for (Class<?> c : clz.getClasses()) {
            if (c.getSimpleName().equals(name))
                return c;
        }
        return null;
    }

    @Nullable
    public static Object[] getMethods(@NonNull Object obj,
                                      @NonNull String name) {
        Class<?> clz;
        if (obj instanceof Class)
            clz = (Class<?>) obj;
        else
            clz = obj.getClass();

        Method[] methods = clz.getMethods();

        int pop = 0;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(name))
                methods[pop++] = methods[i];

            if (i != pop) methods[i] = null;
        }

        if (pop != 0) {
            Object[] result = new Object[pop + 1];
            result[0] = obj instanceof Class ? null : obj;
            System.arraycopy(methods, 0, result, 1, pop);
            Lg.e("hahasasa",result);
            return result;
        }
        return null;
    }

    @Nullable
    public static Object accessField(@NonNull Object obj,
                                     @NonNull Field field)
            throws IllegalAccessException {
        return field.get(Modifier.isStatic(field.getModifiers()) ? null : obj);
    }

    @Nullable
    public static Object invokeMethod(@NonNull Object[] methods,
                                      @Nullable Object[] params)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        int paramNum = params == null ? 0 : params.length;

        Class<?>[] exceptedTypes;

        for (int i = 1; i < methods.length; i++) {
            Method m = (Method) methods[i];
            exceptedTypes = m.getParameterTypes();

            if (canMatchType(exceptedTypes, params, paramNum))
                return m.invoke(methods[0], params);
        }
        throw new NoSuchMethodException("no method can match params: " + Arrays.toString(params));
    }

    @NonNull
    public static Object newInstance(@NonNull Class<?> clz,
                                     @Nullable Object[] params)
            throws InvocationTargetException, IllegalAccessException,
            InstantiationException, NoSuchMethodException {
        int paramNum = params == null ? 0 : params.length;

        Class<?>[] exceptedTypes;

        for (Constructor<?> ctr : clz.getConstructors()) {
            exceptedTypes = ctr.getParameterTypes();

            if (canMatchType(exceptedTypes, params, paramNum))
                return ctr.newInstance(params);
        }
        throw new NoSuchMethodException("no constructor can match params: " + Arrays.toString(params));
    }

    private static boolean canMatchType(@NonNull Class<?>[] exceptedTypes,
                                        @Nullable Object[] params,
                                        int paramNum) {
        if (exceptedTypes.length != paramNum) return false;

        if (params == null) return true;

        for (int i = 0; i < paramNum; i++) {
            if (!canCorrectType(exceptedTypes[i], params, i)) return false;
        }

        return true;
    }

    private static boolean canCorrectType(@NonNull Class<?> excepted,
                                          @NonNull Object[] params,
                                          int index) {
        Object param = params[index];

        if (param == null) {
            return !excepted.isPrimitive();
        }

        if (excepted.isAssignableFrom(param.getClass())) return true;

        if (param instanceof Result.Num) {
            double v = ((Result.Num) param).value;
            params[index] = v;
            boolean isInt = v % 1 == 0;
            if ((excepted == Integer.TYPE || excepted == Integer.class) && isInt) {
                params[index] = (int) v;
                return true;
            } else if (excepted == Float.TYPE || excepted == Float.class) {
                params[index] = (float) v;
                return true;
            } else if ((excepted == Long.TYPE || excepted == Long.class) && isInt) {
                params[index] = (long) v;
                return true;
            } else if (excepted == Double.TYPE || excepted == Double.class) {
                return true;
            } else if ((excepted == Byte.TYPE || excepted == Byte.class) && isInt) {
                params[index] = (byte) v;
                return true;
            } else if ((excepted == Short.TYPE || excepted == Short.class) && isInt) {
                params[index] = (short) v;
                return true;
            } else if ((excepted == Character.TYPE || excepted == Character.class) && isInt) {
                params[index] = (char) (int) v;
                return true;
            }
            return false;

        } else if (param instanceof String) {
            String v = (String) params[index];
            if (excepted == Character.TYPE || excepted == Character.class) {
                if (v.length() != 1) return false;
                params[index] = v.charAt(0);
                return true;
            }
            return false;
        } else if (param instanceof Result.Byte) {
            params[index] = ((Result.Byte) param).value == 1;
            return excepted == Boolean.TYPE || excepted == Boolean.class;
        }
        return false;
    }
}
