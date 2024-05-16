package leasses.zuie.dev;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.SchemeVS2019;
import leasses.anlua.Anlua;
import leasses.anlua.luwu.Luwu;
import leasses.logs.BossLogger;
import leasses.zuie.Lg;

public class DevActivity extends AppCompatActivity {
    public String readAssetFile(String name) {
        var sb = new StringBuilder();
        String line;
        try (var br =
                     new BufferedReader(new InputStreamReader(getAssets().open(name)))) {
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
        } catch (IOException ignored) {

        }
        Lg.d("file name:", name, "\nfile content:", sb.toString().subSequence(0, 66) + " ...");
        return sb.toString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        var editor = new CodeEditor(this);
        editor.setColorScheme(new SchemeVS2019());
        editor.setText("Hello, world! ");
        editor.setTypefaceText(Typeface.MONOSPACE);
        editor.setNonPrintablePaintingFlags(
                CodeEditor.FLAG_DRAW_WHITESPACE_LEADING |
                        CodeEditor.FLAG_DRAW_LINE_SEPARATOR |
                        CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION);

//        var spec=new TsLanguageSpec(
//                 TSLanguage.register()
//        )

        setContentView(editor);

        BossLogger.observe(msg -> runOnUiThread(() -> editor.setText(
                String.format("%s\n[from %s, %s]\n\n%s",
                        msg.content,
                        msg.sender.getTag(),
                        msg.caller,
                        editor.getText())
        )));

        leasses.anlua.Lg.observe(msg -> runOnUiThread(() -> {
            if (!msg.content.startsWith(".class")) return;

            var c = new CodeEditor(this);
            c.setColorScheme(new SchemeVS2019());
            c.setText(msg.content);

            new AlertDialog.Builder(this)
                    .setView(c)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        throw new RuntimeException("aha aha");
                    })
                    .setNeutralButton("Share", (d, w) -> {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, msg.content);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "分享到"));
                    });
//                    .show();
        }));

        var anlua = Anlua.create((Lg::d));
        if (anlua != null) {
            Lg.d("doString result ... ", anlua.doString(readAssetFile("test.lua")));
        }
//        obtainStyledAttributes()
//Lg.d(Intent[].class.equals(Intent.class));

        try {
            Lg.d(Luwu.proxy(AnluaTest.class).indexField("staticString"));
            Lg.d(Luwu.proxy(AnluaTest.LargeClass.class).indexField("FIELD_0"));
//            Lg.d(Test.d);
        } catch (Throwable e) {
            Lg.d(e);
        }

//        var h = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                editor.setText(
//                        String.format("%d\n[%s]\n\n%s",
//                                msg.what,
//                                "((Piece)msg.obj).caller,",
//                                editor.getText())
//                );
//            }
//        };
        editor.postDelayed(() -> {
            for (var i = 0; i <= 10; i++) {
//                Lg.d(i);
            }
        }, 1000L);
//            h.sendEmptyMessage(i);


        var ll = new LinearLayout(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        {
            var h = new HorizontalScrollView(this);
            h.addView(ll);
            Objects.requireNonNull(getSupportActionBar()).setCustomView(h);
        }
        {
            var b1 = new AppCompatButton(this);
            b1.setOnClickListener(v -> {
                long t = System.currentTimeMillis();
                for (int i = 0; i < 10000000; i++) {
                    if (PerformanceTest.obj() instanceof Class<?>) {
//            if (Test.obj().getClass()== String.class) {
                        PerformanceTest.nothing();
                    }
                }
                Lg.d("Time used ", System.currentTimeMillis() - t, "ms\n");
            });
            b1.setText("INSTANCE OF");
            ll.addView(b1);
        }
        {
            var b2 = new AppCompatButton(this);
            b2.setOnClickListener(v -> {
                long t = System.currentTimeMillis();
                for (int i = 0; i < 10000000; i++) {
//                if (Test.obj() instanceof String) {
                    if (PerformanceTest.obj().getClass() == String.class) {
                        PerformanceTest.nothing();
                    }
                }
                Lg.d("Time used ", System.currentTimeMillis() - t, "ms\n");
            });
            b2.setText("GET CLASS");
            ll.addView(b2);
        }
    }

}