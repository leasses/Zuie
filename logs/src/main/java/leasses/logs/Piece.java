package leasses.logs;


import androidx.annotation.NonNull;

public class Piece {

    public final Level level;
    public final String content;
    public final StackTraceElement caller;
    public final Logger sender;
    public final long time;

    Piece(@NonNull Level level,
          @NonNull String content,
          @NonNull StackTraceElement caller,
          @NonNull Logger sender,
          long time) {
        this.level = level;
        this.content = content;
        this.caller = caller;
        this.sender = sender;
        this.time = time;
    }

    @NonNull
    @Override
    public String toString() {
        return content + "\n[" + caller + "]";
    }
}
