package net.bodz.bas.text.row;

public class CharsView
        extends AbstractRow<Character> {

    char[] array;
    int off;
    int len;

    public CharsView(String s) {
        this(s.toCharArray());
    }

    public CharsView(char... array) {
        this(array, 0, array.length);
    }

    public CharsView(char[] array, int begin, int end) {
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
    public Character cellAt(int index) {
        index = wrapIndex(index);
        return array[off + index];
    }

    public char _charAt(int index) {
        index = wrapIndex(index);
        return array[off + index];
    }

    @Override
    public CharsView slice(int begin) {
        begin = wrapBegin(begin);
        return new CharsView(array, off + begin, off + len);
    }

    @Override
    public CharsView slice(int begin, int end) {
        begin = wrapBegin(begin);
        end = wrapEnd(begin, end);
        return new CharsView(array, off + begin, off + end);
    }

    private char[] _alloc(int n) {
        return new char[n];
    }

    @Override
    public CharsView concat(Character ch) {
        return concat(ch.charValue());
    }

    public CharsView concat(char ch) {
        char[] cat = _alloc(len + 1);
        System.arraycopy(array, off, cat, 0, len);
        cat[len] = ch;
        return new CharsView(cat);
    }

    @Override
    public CharsView concat(IRow<? extends Character> row) {
        int len2 = row.length();
        char[] cat = _alloc(len + len2);
        System.arraycopy(array, off, cat, 0, len);

        for (int i = 0; i < len2; i++)
            cat[len + i] = row.cellAt(i);

        return new CharsView(cat);
    }

    @Override
    public String toString() {
        return new String(array, off, len);
    }

}
