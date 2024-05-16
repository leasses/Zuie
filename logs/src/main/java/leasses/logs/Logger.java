package leasses.logs;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public abstract class Logger {
    String tag;

    protected Logger() {
        LoggerCompany.employ(getClass());
    }

    @NonNull
    private static String toString(@Nullable Object msg, int deep) {
        if (msg instanceof Iterable<?> i) {
            var sb = new StringBuilder("{\n");

            for (var e : i)
                sb.append("  ".repeat(deep))
                        .append(toString(e, deep + 1))
                        .append(", \n");

            return sb.append("  ".repeat(deep)).append("}").toString();

        } else if (msg instanceof Object[] arr) {
            var sb = new StringBuilder(deep == -1 ? "" : "{\n");

            for (var m : arr)
                sb.append("  ".repeat(deep == -1 ? 0 : deep))
                        .append(toString(m, deep + 1))
                        .append(deep == -1 ? "  " : ", \n");

            return sb.append(deep <1 ? "" : "  ".repeat(deep-1))
                    .append(deep == -1 ? "" : "}").
                    toString();

        } else if (msg instanceof Throwable tr) {
            return Log.getStackTraceString(tr);
        } else
            return String.valueOf(msg);
    }


    @NonNull
    protected static <T extends Logger> T employ(@NonNull T logger, @NonNull String name) {
        if (logger.tag != null) throw new IllegalStateException("Already set the tag. ");
        if (name.length() >= 20) throw new IllegalArgumentException("Name too long. ");
        logger.tag = name;
        return logger;
    }

    public String getTag() {
        return tag;
    }

    private void push(@NonNull leasses.logs.Level level,
                      @NonNull String content) {
        if (tag == null) throw new IllegalStateException("Not set the tag yet. " +
                "Please use Logger::employ to employ a logger, " +
                "and give him a nice, short name. Otherwise he will get angry :(");

        var caller = new StackTraceElement(
                "UnknownClass",
                "unknownMethod",
                "unknown source",
                -1);

        var self = false;
        var clz = getClass().getName();
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().equals(clz)) {
                self = true;
                continue;
            }

            if (self) {
                caller = e;
                break;
            }
        }

        LoggerCompany.push(new Piece(level,
                content,
                caller,
                this,
                System.currentTimeMillis()));
    }

    protected void bindObserver(@Nullable Observer observer) {
        if (observer != null)
            LoggerCompany.observe(getClass(), observer);
    }

    protected void debug(@Nullable Object msg) {
        push(Level.DEBUG, toString(msg, 0));
    }

    protected void debug(@Nullable Object... msg) {
        push(Level.DEBUG, toString(msg, -1));
    }

    protected void info(@Nullable Object msg) {
        push(Level.INFO, toString(msg, 0));
    }

    protected void info(@Nullable Object... msg) {
        push(Level.INFO, toString(msg, -1));
    }

    protected void error(@Nullable Object msg) {
        push(Level.ERROR, toString(msg, 0));
    }

    protected void error(@Nullable Object... msg) {
        push(Level.ERROR, toString(msg, -1));
    }
}
