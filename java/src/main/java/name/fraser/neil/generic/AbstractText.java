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

    public int indexOf(Text<char_t> pattern) {
        return indexOf(pattern, 0);
    }

    public int indexOf(Text<char_t> pattern, int fromIndex) {
        return indexOf(this, 0, this.length(), pattern, 0, pattern.length(), fromIndex);
    }

    private static int indexOf(Text<?> source, int sourceOffset, int sourceCount, //
            Text<?> target, int targetOffset, int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        Object first = target.charAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (notEquals(source.charAt(i), first)) {
                while (++i <= max && notEquals(source.charAt(i), first))
                    ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && equals(source.charAt(j), target.charAt(k)); j++, k++)
                    ;

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
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
