package leasses.anlua;

public final class Native {
    static {
        System.loadLibrary("logger");
        System.loadLibrary("anlua");
    }

    public static native byte getLightTypeErr();

    public static native byte getLightTypeNull();

    public static native byte getLightTypeTrue();

    public static native byte getLightTypeFalse();

    public static native byte getLightTypeStr();

    public static native byte getLightTypeObj();

    static native long newState();

    static native Object[] doString(long ptr, String s);
}
