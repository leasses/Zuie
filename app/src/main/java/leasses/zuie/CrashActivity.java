package leasses.zuie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class CrashActivity extends Activity {
    private static final String EXTRA_THREAD = "thread";
    private static final String EXTRA_THROWABLE = "throwable";

    static void start(@NonNull Context ctx, @Nullable Thread thread, @Nullable Throwable e) {
        try {
            ctx.startActivity(new Intent()
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setClass(ctx, CrashActivity.class)
                    .putExtra(EXTRA_THREAD, Objects.toString(thread))
                    .putExtra(EXTRA_THROWABLE, e));
        } catch (Throwable wtf) {
            Log.wtf("APP CRASHED", wtf);
            Toast.makeText(ctx, wtf.toString(), Toast.LENGTH_LONG).show();
            Process.killProcess(Process.myPid());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Throwable e) {
            giveUp(e);
        }

        var thread = "unknown thread";
        var throwable = "unknown exception";

        try {
            thread = getIntent().getStringExtra(EXTRA_THREAD);
        } catch (Throwable ignored) {
        }

        try {
            var t = (Throwable) getIntent().getSerializableExtra(EXTRA_THROWABLE);
            throwable = String.valueOf(t);
            throwable = Log.getStackTraceString(t);
        } catch (Throwable ignored) {
        }

        try {
            var content = new TextView(this);
            content.setText(String.format(
                    "App crashed unexpectedly.\nOn thread: %s\nStackTrace\n%s",
                    thread,
                    throwable));

            setContentView(content);

            content.setMovementMethod(ScrollingMovementMethod.getInstance());
            content.setBackgroundColor(0xff202020);
            content.setTextColor(0xfff0f0f0);
            content.setTextSize(16);
        } catch (Throwable e) {
            giveUp(e);
        }
    }

    private void giveUp(Throwable e) {
        Log.wtf("APP CRASHED", e);
        Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show();
        finish();
    }
}