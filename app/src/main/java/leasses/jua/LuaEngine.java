package leasses.jua;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import leasses.zuie.log.Lg;

public class LuaEngine {
//    private static final LuaEngine INSTANCE = new LuaEngine();
//
//    private AssetManager assets;
//
//    private LuaEngine() {
//    }
//
//    public static LuaEngine get() {
//        return INSTANCE;
//    }
//
//    public void setAssets(AssetManager assets) {
//        this.assets = assets;
//    }
//
//    public void runAssetFile(String name) {
//        if (assets == null) return;
//
//        StringBuilder sb = new StringBuilder();
//        String line;
//        try (BufferedReader br =
//                     new BufferedReader(new InputStreamReader(assets.open(name)))) {
//            while ((line = br.readLine()) != null)
//                sb.append(line).append('\n');
//        } catch (IOException ignored) {
//
//        }
//        Lg.i("file name:", name, "\nfile content:", sb.toString());
//        NATIVE.run(sb.toString());
//    }
}
