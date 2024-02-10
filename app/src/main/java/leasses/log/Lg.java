package leasses.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class Lg /* Logger */ {
    private static final String CLASS_NAME = Lg.class.getName();
    private static Observer observer = msg -> {

    };

    private Lg() {
    }

    public static void register(Observer observer) {
        Lg.observer = observer;
    }

    public static void i(@Nullable Object msg) {
        newLog(Level.INFO, String.valueOf(msg));
    }

    public static void i(@Nullable Object... msg) {
        newLog(Level.INFO, toString(msg));
    }

    public static void e(@Nullable Object msg) {
        newLog(Level.ERROR, String.valueOf(msg));
    }

    public static void e(@Nullable Object... msg) {
        newLog(Level.ERROR, toString(msg));
    }

    @NonNull
    private static String toString(@Nullable Object... msg) {
        if (msg == null) return "null";

        StringBuilder sb = new StringBuilder();
        for (Object obj : msg) {
            if (obj == null) {
                sb.append("null");
            } else if (obj.getClass().isArray()) {
                sb.append(Arrays.toString((Object[]) obj));
            } else if (obj instanceof Throwable) {
                sb.append(Log.getStackTraceString((Throwable) obj));
            } else {
                sb.append(obj);
            }

            sb.append("  ");
        }
        return sb.toString();
    }

    private static void newLog(@NonNull Level level, @NonNull String msg) {
        StackTraceElement caller = new StackTraceElement(
                "UnknownClass",
                "unknownMethod",
                "unknown source",
                -1);

        boolean isMyself = false;
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().equals(CLASS_NAME)) {
                isMyself = true;
                continue;
            }

            if (isMyself) {
                caller = e;
                break;
            }
        }

        observer.onNewLog(msg + "\n[Caller: " +
                caller.getClassName() + "::" + caller.getMethodName() +
                " (" + caller.getFileName() + ":" + caller.getLineNumber() + ")]");

        switch (level) {
            case INFO:
                Log.i(caller.getClassName(), msg);
                break;
            case ERROR:
                Log.e(caller.getClassName(), msg);
        }
    }

    private enum Level {
        INFO, ERROR
    }

    public interface Observer {
        void onNewLog(String msg);
    }
}
