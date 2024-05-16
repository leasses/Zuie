package leasses.logs;

public interface Observer {
    void onReceive(Piece msg) throws Throwable;
}
