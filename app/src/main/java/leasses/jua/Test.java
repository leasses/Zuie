package leasses.jua;

import leasses.zuie.log.Lg;

public class Test {
    public static final String pb_f_s_f = "Hey! I am the value of pb_f_s_f";
    private static final String pr_f_s_f = "Hey! I am the value of pr_f_s_f";
    public static String pb_s_f = "Hey! I am the value of pb_s_f";
    public static String[] strArray = new String[]{
            "first",
            "this is 2",
            "this is 3",
            "this is 4",
    };
    public static boolean public_static_boolean_true = true;
    private static String pr_s_f = "Hey! I am the value of pr_s_f";
    private static Test test = new Test((int) 1.0f, false, 'a');
    public final double number;
    public final String pb_f_f = "Hey! I am the value of pb_f_f";
    private final String pr_f_f = "Hey! I am the value of pr_f_f";
    public String pb_f = "Hey! I am the value of pb_f";
    private String pr_f = "Hey! I am the value of pr_f";

    public Test(Integer d, boolean b, Character a) {
        this.number = (double) d;
    }

    public Test(Double dr, boolean b, Lg.Observer a) {
        this.number = dr + 12;
    }

    public static int ohh(int a, Float b) {
        Boolean.valueOf(true).booleanValue();
        return (int) (Math.max(a, b) + 3);
    }

    public static int ohh(boolean a) {
        return 2132313213;
    }

    public int len(String[] arr) {
        return arr.length;
    }

    public int getNum() {
        return 2333;
    }
}
