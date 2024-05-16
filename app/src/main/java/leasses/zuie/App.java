package leasses.zuie;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.Objects;

import leasses.anlua.luwu.Luwu;

public final class App extends Application {
    private static final String DEX_DIR_NAME = "luwu";

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void onCreate() {
        new Handler(Looper.getMainLooper()).post(() -> {
            while (true)
                try {
                    Looper.loop();
                } catch (Throwable e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            CrashActivity.start(App.this, Thread.currentThread(), e));
                }
        });

        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
                CrashActivity.start(App.this, t, e));

        super.onCreate();

        Luwu.init(getClassLoader(),
                Objects.requireNonNull(getExternalFilesDir(DEX_DIR_NAME)));
    }
}
