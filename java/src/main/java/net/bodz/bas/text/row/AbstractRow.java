package net.bodz.bas.text.row;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.bodz.bas.text.Nullables;

public abstract class AbstractRow<cell_t>
        implements
            IRow<cell_t> {

    @Override
    public Iterator<cell_t> iterator() {
        return new Iterator<cell_t>() {

            int n = length();
            int next = 0;

            @Override
            public boolean hasNext() {
                return next < n;
            }

            @Override
            public cell_t next() {
                if (next >= n)
                    throw new NoSuchElementException();
                return cellAt(next++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public boolean isEmpty() {
        return length() == 0;
    }

    protected int wrapIndex(int index) {
        if (index < 0)
            index += length();
        if (index < 0 || index >= length())
            throw new IndexOutOfBoundsException(String.valueOf(index));
        return index;
    }

    protected int wrapBegin(int begin) {
        if (begin < 0)
            begin += length();
        if (begin < 0 || begin > length())
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        return begin;
    }

    protected int wrapEnd(int begin, int end) {
        if (begin < 0)
            begin += length();
        if (end < 0)
            end += length();
        if (begin < 0)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        if (end < 0 || end > length())
            throw new IndexOutOfBoundsException(String.valueOf(end));
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        return end;
    }

    @Override
    public IRow<cell_t> slice(int begin) {
        begin = wrapBegin(begin);
        return slice(begin, length());
    }

    @Override
    public int indexOf(Object pattern) {
        return indexOf(pattern, 0);
    }

    @Override
    public int indexOf(Object pattern, int from) {
        from = wrapBegin(from);
        int n = length();
        for (int i = from; i < n; i++)
            if (pattern.equals(cellAt(i)))
                return i;
        return -1;
    }

    @Override
    public int lastIndexOf(Object pattern) {
        return lastIndexOf(pattern, length() - 1);
    }

    @Override
    public int lastIndexOf(Object pattern, int from) {
        from = wrapBegin(from);
        for (int i = from; i >= 0; i--)
            if (pattern.equals(cellAt(i)))
                return i;
        return -1;
    }

    @Override
    public int indexOf(IRow<? extends cell_t> pattern) {
        return indexOf(pattern, 0);
    }

    @Override
    public int indexOf(IRow<? extends cell_t> pattern, int from) {
        from = wrapBegin(from);
        return indexOf(this, 0, this.length(), pattern, 0, pattern.length(), from);
    }

    @Override
    public int lastIndexOf(IRow<? extends cell_t> pattern) {
        return lastIndexOf(pattern, this.length());
    }

    @Override
    public int lastIndexOf(IRow<? extends cell_t> pattern, int from) {
        from = wrapBegin(from);
        return lastIndexOf(this, 0, this.length(), pattern, 0, pattern.length(), from);
    }

    private static int indexOf(IRow<?> source, int sourceOffset, int sourceCount, //
            IRow<?> target, int targetOffset, int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount)
            return (targetCount == 0 ? sourceCount : -1);
        if (fromIndex < 0)
            fromIndex = 0;
        if (targetCount == 0)
            return fromIndex;

        Object first = target.cellAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            if (Nullables.notEquals(source.cellAt(i), first))
                while (++i <= max)
                    if (Nullables.equals(source.cellAt(i), first))
                        break;

            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;

                int k = targetOffset + 1;
                while (j < end) {
                    if (Nullables.notEquals(source.cellAt(j), target.cellAt(k)))
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

    private static int lastIndexOf(IRow<?> source, int sourceOffset, int sourceCount, IRow<?> target, int targetOffset,
            int targetCount, int fromIndex) {
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0)
            return -1;
        if (fromIndex > rightIndex)
            fromIndex = rightIndex;
        if (targetCount == 0)
            return fromIndex;

        int strLastIndex = targetOffset + targetCount - 1;
        Object strLastChar = target.cellAt(strLastIndex);
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        L: while (true) {
            while (i >= min) {
                if (Nullables.equals(source.cellAt(i), strLastChar))
                    break;
                i--;
            }

            if (i < min)
                return -1;
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start)
                if (Nullables.notEquals(source.cellAt(j--), target.cellAt(k--))) {
                    i--;
                    continue L;
                }
            return start - sourceOffset + 1;
        }
    }

    @Override
    public boolean startsWith(IRow<? extends cell_t> pattern) {
        int nl = length();
        int np = pattern.length();
        if (nl < np)
            return false;
        for (int i = 0; i < np; i++)
            if (Nullables.notEquals(cellAt(i), pattern.cellAt(i)))
                return false;
        return true;
    }

    @Override
    public boolean endsWith(IRow<? extends cell_t> pattern) {
        int nl = length();
        int np = pattern.length();
        if (nl < np)
            return false;
        int tail = nl - np;
        for (int i = 0; i < np; i++)
            if (Nullables.notEquals(cellAt(tail + i), pattern.cellAt(i)))
                return false;
        return true;
    }

    @Override
    public IRow<cell_t> lock() {
        return this;
    }

    @Override
    public IMutableRow<cell_t> unlock() {
        return copy();
    }

    @Override
    public IMutableRow<cell_t> copy() {
        MutableRow<cell_t> copy = new MutableRow<cell_t>(length());
        copy.append(this);
        return copy;
    }

    @Override
    public <T> IMutableRow<T> copy(Function<cell_t, T> function) {
        MutableRow<T> copy = new MutableRow<T>(length());
        for (cell_t cell : this)
            copy.append(function.apply(cell));
        return copy;
    }

    public static <cell_t, T extends cell_t> IMutableRow<cell_t> copy(IRow<T> row) {
        MutableRow<cell_t> copy = new MutableRow<cell_t>(row.length());
        copy.append(row);
        return copy;
    }

    @Override
    public String toString() {
        int n = length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            cell_t cell = cellAt(i);
            sb.append(cell.toString());
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int len = length();
        for (int i = 0; i < len; i++) {
            cell_t c = cellAt(i);
            result = prime * result + c.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
//        if (getClass() != obj.getClass())
//            return false;

        @SuppressWarnings("unchecked")
        AbstractRow<cell_t> other = (AbstractRow<cell_t>) obj;

        int len = length();
        if (len != other.length())
            return false;

        for (int i = 0; i < len; i++) {
            cell_t c1 = cellAt(i);
            cell_t c2 = other.cellAt(i);
            if (!c1.equals(c2))
                return false;
        }
        return true;
    }

}
