package leasses.logs;

public class BossLogger extends Logger{
    private static final BossLogger I = employ(new BossLogger(),"zuie");

    public static void observe(Observer observer) {
        I.bindObserver(observer);
    }
}
