package leasses.logs;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

final class LoggerCompany {
    private static final LinkedBlockingQueue<Piece> logs = new LinkedBlockingQueue<>();
    private static final HashMap<Class<? extends Logger>, Observer> observers = new HashMap<>();

    static {
        new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    Piece log;
                    Observer o;
                    while (true) try {
                        if ((log = logs.take()) == null) continue;

                        if ((o = observers.get(BossLogger.class)) != null) try {
                            o.onReceive(log);
                        } catch (Throwable ignored) {

                        }
                        if ((o = observers.get(log.sender.getClass())) != null) try {
                            o.onReceive(log);
                        } catch (Throwable ignored) {

                        }

                        TimeUnit.MILLISECONDS.sleep(10);

                    } catch (InterruptedException ignored) {
                    }
                },
                "log dispatcher").start();
    }

    private LoggerCompany() {
        throw new UnsupportedOperationException();
    }

    static void observe(@NonNull Class<? extends Logger> target,
                        @NonNull Observer observer) {
        if (!observers.containsKey(target))
            throw new IllegalStateException("Not employ this sender yet: " + target);

        observers.put(target, observer);
    }

    static void employ(Class<? extends Logger> clz) {
        if (observers.containsKey(clz))
            throw new IllegalStateException("Already employed this sender: " + clz);

        observers.put(clz, null);
    }

    static void push(Piece log) {
        logs.offer(log);
        Log.e("BossLogger"+log.sender.tag, log.content);
    }
}
