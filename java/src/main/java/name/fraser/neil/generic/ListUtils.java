package name.fraser.neil.generic;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    public static <T> List<T> create(T item) {
        List<T> list = new ArrayList<T>();
        list.add(item);
        return list;
    }

    public static <T> List<T> concat(List<T> list1, List<T> list2) {
        List<T> cat = new ArrayList<T>(list1.size() + list2.size());
        cat.addAll(list1);
        cat.addAll(list2);
        return cat;
    }

    public static void removeFrom(List<?> list, int start) {
        int n = list.size();
        for (int i = n - 1; i >= start; i--)
            list.remove(i);
    }

    public static void removeUntil(List<?> list, int end) {
        for (int i = end - 1; i >= 0; i++)
            list.remove(i);
    }

    public static boolean startsWithList(List<?> list, List<?> pattern) {
        int nl = list.size();
        int np = pattern.size();
        if (nl < np)
            return false;
        for (int i = 0; i < np; i++)
            if (notEquals(list.get(i), pattern.get(i)))
                return false;
        return true;
    }

    public static boolean endsWithList(List<?> list, List<?> pattern) {
        int nl = list.size();
        int np = pattern.size();
        if (nl < np)
            return false;
        int tail = nl - np;
        for (int i = 0; i < np; i++)
            if (notEquals(list.get(tail + i), pattern.get(i)))
                return false;
        return true;
    }

    public static int indexOfList(List<?> list, List<?> pattern) {
        return indexOfList(list, pattern, 0);
    }

    public static int indexOfList(List<?> list, List<?> pattern, int fromIndex) {
        return indexOf(list, 0, list.size(), pattern, 0, pattern.size(), fromIndex);
    }

    static int indexOf(List<?> source, int sourceOffset, int sourceCount, //
            List<?> target, int targetOffset, int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        Object first = target.get(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (notEquals(source.get(i), first)) {
                while (++i <= max && notEquals(source.get(i), first))
                    ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && equals(source.get(j), target.get(k)); j++, k++)
                    ;

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    private static boolean equals(Object a, Object b) {
        if (a == null || b == null)
            return a == b;
        else
            return a.equals(b);
    }

    private static boolean notEquals(Object a, Object b) {
        if (a == null || b == null)
            return a != b;
        else
            return !a.equals(b);
    }

    static List<String> toList(char[] v) {
        List<String> list = new ArrayList<String>(v.length);
        for (char ch : v)
            list.add("" + ch);
        return list;
    }

    public static void main(String[] args) {
        String a = "hello, world. here's foo there's bar.";
        String b = "world";
        List<String> av = toList(a.toCharArray());
        List<String> bv = toList(b.toCharArray());
        int index = indexOfList(av, bv);
        System.out.println(index);
    }

}
