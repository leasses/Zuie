package leasses.zuie;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import leasses.jua.Jua;
import leasses.jua.api.Result;
import leasses.zuie.log.Lg;

public class MainActivity extends AppCompatActivity {
    public String readAssetFile(String name) {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(getAssets().open(name)))) {
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
        } catch (IOException ignored) {

        }
        Lg.i("file name:", name, "\nfile content:", sb.toString().subSequence(0, 10) + "...");
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        TextView tv = new TextView(this);

        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        setContentView(tv);

        Lg.register((msg) -> runOnUiThread(
                () -> {
                    tv.setText(String.format(
                            "%s\n%s\n", msg,
                            tv.getText()));
                    tv.scrollTo(0, tv.getLineHeight() * tv.getLineCount());
                }));

        Jua jua = Jua.create((Lg::e));
        if (jua != null) {
            Lg.i("doString result ... ", jua.doString(readAssetFile("test.lua")));
        }

//        Double obj = Double.valueOf(234532443243242343243244.32432423423434234234);
//        long t;

//         t= System.currentTimeMillis();
//        for (int i = 0; i < 10000000; i++) {
//            Object[] r = new Object[]{null,obj};
//        }
//        Lg.e(System.currentTimeMillis() - t);
//        Class<?> clz = obj.getClass();

//        t = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; i++) {
//            aa(null, Result.Num.of(0));
//        }
//        Lg.e("o", System.currentTimeMillis() - t);
    }

//    private native void aa(Object o, Result.Num o2);
}