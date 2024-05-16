package leasses.anlua;

import leasses.logs.Logger;
import leasses.logs.Observer;

public final class Lg extends Logger {
    private static final Lg I = employ(new Lg(), "anlua");

    public static void observe(Observer observer) {
        I.bindObserver(observer);
    }

    public static void d(Object msg) {
        I.debug(msg);
    }

    public static void d(Object... msg) {
        I.debug(msg);
    }

    public static void i(Object msg) {
        I.info(msg);
    }

    public static void e(Object msg) {
        I.error(msg);
    }

}
