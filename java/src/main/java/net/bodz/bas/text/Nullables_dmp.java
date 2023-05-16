package net.bodz.bas.text;

public class Nullables_dmp {

    public static boolean equals(Object a, Object b) {
        if (a == null || b == null)
            return a == b;
        else
            return a.equals(b);
    }

    public static boolean notEquals(Object a, Object b) {
        if (a == null || b == null)
            return a != b;
        else
            return !a.equals(b);
    }

}
