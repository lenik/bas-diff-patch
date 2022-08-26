package net.bodz.bas.text.generic;

public class Nullables {

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
