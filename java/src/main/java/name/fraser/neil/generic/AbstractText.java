package name.fraser.neil.generic;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractText<char_t>
        implements
            Text<char_t> {

    @Override
    public Iterator<char_t> iterator() {
        return new Iterator<char_t>() {

            int n = length();
            int next = 0;

            @Override
            public boolean hasNext() {
                return next < n;
            }

            @Override
            public char_t next() {
                if (next >= n)
                    throw new NoSuchElementException();
                return charAt(next++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public int indexOf(Text<char_t> pattern) {
        return indexOf(pattern, 0);
    }

    @Override
    public int indexOf(Text<char_t> pattern, int fromIndex) {
        return indexOf(this, 0, this.length(), pattern, 0, pattern.length(), fromIndex);
    }

    @Override
    public int lastIndexOf(Text<char_t> pattern) {
        return lastIndexOf(pattern, 0);
    }

    @Override
    public int lastIndexOf(Text<char_t> pattern, int fromIndex) {
        return lastIndexOf(this, 0, this.length(), pattern, 0, pattern.length(), fromIndex);
    }

    private static int indexOf(Text<?> source, int sourceOffset, int sourceCount, //
            Text<?> target, int targetOffset, int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount)
            return (targetCount == 0 ? sourceCount : -1);
        if (fromIndex < 0)
            fromIndex = 0;
        if (targetCount == 0)
            return fromIndex;

        Object first = target.charAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            if (notEquals(source.charAt(i), first))
                while (++i <= max)
                    if (equals(source.charAt(i), first))
                        break;

            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;

                int k = targetOffset + 1;
                while (j < end) {
                    if (notEquals(source.charAt(j), target.charAt(k)))
                        break;
                    j++;
                    k++;
                }
                if (j == end)
                    return i - sourceOffset;
            }
        }
        return -1;
    }

    private static int lastIndexOf(Text<?> source, int sourceOffset, int sourceCount, Text<?> target, int targetOffset,
            int targetCount, int fromIndex) {
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0)
            return -1;
        if (fromIndex > rightIndex)
            fromIndex = rightIndex;
        if (targetCount == 0)
            return fromIndex;

        int strLastIndex = targetOffset + targetCount - 1;
        Object strLastChar = target.charAt(strLastIndex);
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        L: while (true) {
            while (i >= min) {
                if (equals(source.charAt(i), strLastChar))
                    break;
                i--;
            }

            if (i < min)
                return -1;
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start)
                if (notEquals(source.charAt(j--), target.charAt(k--))) {
                    i--;
                    continue L;
                }
            return start - sourceOffset + 1;
        }
    }

    @Override
    public boolean startsWith(Text<char_t> pattern) {
        int nl = length();
        int np = pattern.length();
        if (nl < np)
            return false;
        for (int i = 0; i < np; i++)
            if (notEquals(charAt(i), pattern.charAt(i)))
                return false;
        return true;
    }

    @Override
    public boolean endsWith(Text<char_t> pattern) {
        int nl = length();
        int np = pattern.length();
        if (nl < np)
            return false;
        int tail = nl - np;
        for (int i = 0; i < np; i++)
            if (notEquals(charAt(tail + i), pattern.charAt(i)))
                return false;
        return true;
    }

    @Override
    public String asString() {
        int n = length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char_t ch = charAt(i);
            sb.append(ch.toString());
        }
        return sb.toString();
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

}
