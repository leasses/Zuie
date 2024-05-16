package leasses.anlua.api;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import leasses.anlua.luwu.Executable;
import leasses.anlua.luwu.Luwu;

class AnluaApi {
    private AnluaApi() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    private static CLight imports(@NonNull String name) {
        try {
            return CLight.of(Class.forName(name));
        } catch (ClassNotFoundException e) {
            return CLight.err("cannot import the class " + name + '\n' + e);
        }
    }

    @NonNull
    private static CLight objectIndex(@NonNull Object obj, @NonNull String key) {
        var first = key.charAt(0);
        var last = key.charAt(key.length() - 1);
        var err = "not implement yet";

        if (first >= '0' && first <= '9') {
            try {
                var index = Integer.parseInt(key);
                if (obj instanceof Object[] o) {
                    return CLight.of(o[index - 1]);
                } else if (obj instanceof List<?> l) {
                    return CLight.of(l.get(index - 1));
                } else if (obj instanceof SparseArray<?> a) {
                    return CLight.of(a.get(index));
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                err = e.toString();
            }
        }

        var clz = obj instanceof Class<?> c ? c : obj.getClass();
        obj = obj instanceof Class<?> ? null : obj;

        if (clz.isArray()) return CLight.err("cannot index an array with a string");

        Luwu luwu;
        try {
            luwu = Luwu.proxy(clz);
        } catch (Exception e) {
            return CLight.err("cannot get Luwu access\n" + Log.getStackTraceString(e));
        }

        var preferField = (first >= 'A' && first <= 'Z') &&
                ((last >= 'A' && last <= 'Z') || (last >= '0' && last <= '9'));

        var id = preferField ? luwu.indexField(key) : luwu.indexClass(key);

        if (id == Luwu.NOT_FOUND)
            id = preferField ? luwu.indexClass(key) : luwu.indexField(key);

        if (id != Luwu.NOT_FOUND)
            try {
                return luwu.access(obj, id, null);
            } catch (Throwable e) {
                return CLight.err("cannot get class or field\n" + Log.getStackTraceString(e));
            }

        Executable exe;
        if ((exe = luwu.indexExecutable(key)) != null)
            return CLight.of(new Object[]{obj, luwu, exe});

        return CLight.err(err);
    }

    @NonNull
    private static CLight objectCall(@NonNull Object obj, @Nullable Object[] params) {
        if (obj instanceof Class<?> clz) {
            Luwu access;
            try {
                access = Luwu.proxy(clz);
            } catch (Exception e) {
                return CLight.err("cannot get Luwu access\n" + Log.getStackTraceString(e));
            }

            Executable exe;
            if ((exe = access.indexExecutable(null)) == null)
                return CLight.err("no constructor found");

            try {
                return access.access(obj, exe.matchType(params), params);
            } catch (Throwable e) {
                return CLight.err("cannot new an instance\n" + Log.getStackTraceString(e));
            }

        } else if (obj instanceof Object[] o) {

            if (o.length == 3 &&
                    o[1] instanceof Luwu access &&
                    o[2] instanceof Executable exe) {
                try {
                    return access.access(o[0], exe.matchType(params), params);
                } catch (Throwable e) {
                    return CLight.err("cannot call the method\n" + e);
                }

            }
        }
        return CLight.err("bad object to call (excepted java class or method set)");
    }

}
