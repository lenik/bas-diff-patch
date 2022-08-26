package net.bodz.bas.text.generic;

public class CharText
        extends AbstractText<Character> {

    char[] array;
    int off;
    int len;

    public CharText(String s) {
        this(s.toCharArray());
    }

    public CharText(char... array) {
        this(array, 0, array.length);
    }

    public CharText(char[] array, int begin, int end) {
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

    private void checkIndex(int index) {
        if (index < 0 || index >= len)
            throw new IndexOutOfBoundsException(String.valueOf(index));
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
    public Character charAt(int index) {
        checkIndex(index);
        return array[off + index];
    }

    public char _charAt(int index) {
        checkIndex(index);
        return array[off + index];
    }

    @Override
    public CharText substring(int begin) {
        if (begin < 0 || begin > len)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        return new CharText(array, off + begin, off + len);
    }

    @Override
    public CharText substring(int begin, int end) {
        if (begin < 0)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        if (end < 0 || end - begin > len)
            throw new IndexOutOfBoundsException(String.valueOf(end));
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        return new CharText(array, off + begin, off + end);
    }

    private char[] _alloc(int n) {
        return new char[n];
    }

    @Override
    public CharText concat(Character ch) {
        return concat(ch.charValue());
    }

    public CharText concat(char ch) {
        char[] cat = _alloc(len + 1);
        System.arraycopy(array, off, cat, 0, len);
        cat[len] = ch;
        return new CharText(cat);
    }

    @Override
    public CharText concat(Text<Character> o) {
        int n2 = o.length();
        char[] cat = _alloc(len + n2);
        System.arraycopy(array, off, cat, 0, len);

        for (int i = 0; i < n2; i++)
            cat[len + i] = o.charAt(i);

        return new CharText(cat);
    }

    @Override
    public String asString() {
        return new String(array, off, len);
    }

}
