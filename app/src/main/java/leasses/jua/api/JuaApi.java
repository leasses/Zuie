package leasses.jua.api;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("unused")
public class JuaApi {

    private JuaApi() {
    }

    @NonNull
    private static Object[] objectIndex(@NonNull Object obj, @NonNull String key) {
        char first = key.charAt(0);
        char last = key.charAt(key.length() - 1);
        String err = "not implement yet";

        if (first >= '0' && first <= '9') {
            try {
                int index = Integer.parseInt(key);
                if (obj instanceof Object[]) {
                    return Result.wrap(((Object[]) obj)[index - 1]);
                } else if (obj instanceof List) {
                    return Result.wrap(((List<?>) obj).get(index - 1));
                } else if (obj instanceof SparseArray) {
                    return Result.wrap(((SparseArray<?>) obj).get(index));
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                err = e.toString();
            }
        }

        if (obj instanceof Object[]) return Result.err("cannot index an array with a string");

        Object v;
        if (first >= 'A' && first <= 'Z') {
            if (last >= 'A' && last <= 'Z') {

                if ((v = ReflectFactory.getField(obj, key)) != null)
                    try {
                        return Result.wrap(ReflectFactory.accessField(obj, (Field) v));
                    } catch (IllegalAccessException e) {
                        err = e.toString();
                    }
                else if ((v = ReflectFactory.getInnerClass(obj, key)) != null)
                    return Result.wrap(v);
                else err = "no matched field or inner class found";

            } else {

                if ((v = ReflectFactory.getInnerClass(obj, key)) != null)
                    return Result.wrap(v);
                else if ((v = ReflectFactory.getField(obj, key)) != null)
                    try {
                        return Result.wrap(ReflectFactory.accessField(obj, (Field) v));
                    } catch (IllegalAccessException e) {
                        err = e.toString();
                    }
                else err = "no matched field or inner class found";

            }
        }

        if ((v = ReflectFactory.getMethods(obj, key)) != null)
            return Result.wrap(v);
        else if ((v = ReflectFactory.getField(obj, key)) != null)
            try {
                return Result.wrap(ReflectFactory.accessField(obj, (Field) v));
            } catch (IllegalAccessException e) {
                err = e.toString();
            }
        else if ((v = ReflectFactory.getInnerClass(obj, key)) != null)
            return Result.wrap(v);

        return Result.err(err);
    }

    @NonNull
    private static Object[] objectCall(@NonNull Object obj, @Nullable Object[] params) {
        if (obj instanceof Class)

            try {
                return Result.wrap(ReflectFactory.newInstance((Class<?>) obj, params));
            } catch (InvocationTargetException |
                     IllegalAccessException |
                     InstantiationException |
                     NoSuchMethodException e) {
                return Result.err("cannot new an instance\n" + e);
            }

        else if (obj instanceof Object[]) {

            Object[] o = (Object[]) obj;
            if (o.length >= 2 && o[1] instanceof Method) {
                try {
                    return Result.wrap(ReflectFactory.invokeMethod(o, params));
                } catch (InvocationTargetException |
                         IllegalAccessException |
                         NoSuchMethodException e) {
                    return Result.err("cannot call the method\n" + e);
                }catch (NullPointerException e){
                    return Result.err("cannot call an instance method on a java class");
                }
            }

        }
        return Result.err("bad argument to call (excepted java class or method set)");
    }
}
