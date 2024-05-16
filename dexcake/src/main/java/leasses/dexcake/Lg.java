package leasses.dexcake;

import leasses.logs.Logger;
import leasses.logs.Observer;

public final class Lg extends Logger {
    private static final Lg I = employ(new Lg(),"dex cake");

    public static void d(Object msg) {
        I.debug(msg);
    }

    public static void d(Object... msg) {
        I.debug(msg);
    }

}
