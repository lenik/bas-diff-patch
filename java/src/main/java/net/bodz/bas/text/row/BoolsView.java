package net.bodz.bas.text.row;

public class BoolsView
        extends AbstractRow<Boolean> {

    boolean[] array;
    int off;
    int len;

    public BoolsView(String s) {
        this(toBooleanArray(s));
    }

    public BoolsView(boolean... array) {
        this(array, 0, array.length);
    }

    public BoolsView(boolean[] array, int begin, int end) {
        if (array == null)
            throw new NullPointerException("array");
        if (begin < 0)
            throw new IndexOutOfBoundsException("illegal begin: " + begin);
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        this.array = array;
        this.off = begin;
        this.len = end - begin;
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public boolean isEmpty() {
        return len == 0;
    }

    @Override
    public Boolean cellAt(int index) {
        index = wrapIndex(index);
        return array[off + index];
    }

    public boolean _booleanAt(int index) {
        index = wrapIndex(index);
        return array[off + index];
    }

    @Override
    public BoolsView slice(int begin) {
        begin = wrapBegin(begin);
        return new BoolsView(array, off + begin, off + len);
    }

    @Override
    public BoolsView slice(int begin, int end) {
        begin = wrapBegin(begin);
        end = wrapEnd(begin, end);
        return new BoolsView(array, off + begin, off + end);
    }

    private boolean[] _alloc(int n) {
        return new boolean[n];
    }

    @Override
    public BoolsView concat(Boolean ch) {
        return concat(ch.booleanValue());
    }

    public BoolsView concat(boolean ch) {
        boolean[] cat = _alloc(len + 1);
        System.arraycopy(array, off, cat, 0, len);
        cat[len] = ch;
        return new BoolsView(cat);
    }

    @Override
    public BoolsView concat(IRow<? extends Boolean> row) {
        int len2 = row.length();
        boolean[] cat = _alloc(len + len2);
        System.arraycopy(array, off, cat, 0, len);

        for (int i = 0; i < len2; i++)
            cat[len + i] = row.cellAt(i);

        return new BoolsView(cat);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(array[off + i] ? '1' : '0');
        return sb.toString();
    }

    public boolean[] toBooleanArray() {
        int n = length();
        boolean[] copy = new boolean[n];
        System.arraycopy(array, off, copy, 0, len);
        return copy;
    }

    static boolean[] toBooleanArray(String s) {
        int n = s.length();
        boolean[] array = new boolean[n];
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            array[i] = ch == '1' || ch == '-'; // 10 or -.
        }
        return array;
    }

}
