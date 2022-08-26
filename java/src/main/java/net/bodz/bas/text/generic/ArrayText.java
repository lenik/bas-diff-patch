package net.bodz.bas.text.generic;

import java.lang.reflect.Array;

public class ArrayText<char_t>
        extends AbstractText<char_t> {

    char_t[] array;
    int off;
    int len;

    public ArrayText(char_t[] array) {
        this(array, 0, array.length);
    }

    public ArrayText(char_t[] array, int begin, int end) {
        if (array == null)
            throw new NullPointerException("array");
        if (begin < 0)
            throw new IndexOutOfBoundsException("illegal begin: " + begin);
        if (end < 0 || end > array.length)
            throw new IndexOutOfBoundsException("illegal end: " + end);
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
    public char_t charAt(int index) {
        checkIndex(index);
        return array[off + index];
    }

    @Override
    public Text<char_t> substring(int begin) {
        if (begin < 0 || begin > len)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        return new ArrayText<char_t>(array, off + begin, off + len);
    }

    @Override
    public Text<char_t> substring(int begin, int end) {
        if (begin < 0)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        if (end < 0 || end - begin > len)
            throw new IndexOutOfBoundsException(String.valueOf(end));
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        return new ArrayText<char_t>(array, off + begin, off + end);
    }

    private char_t[] _alloc(int n) {
        Class<?> c = array.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        char_t[] array = (char_t[]) Array.newInstance(c, n);
        return array;
    }

    @Override
    public Text<char_t> concat(char_t ch) {
        char_t[] cat = _alloc(len + 1);
        System.arraycopy(array, off, cat, 0, len);
        cat[len] = ch;
        return new ArrayText<char_t>(cat);
    }

    @Override
    public Text<char_t> concat(Text<char_t> o) {
        int n2 = o.length();
        char_t[] cat = _alloc(len + n2);
        System.arraycopy(array, off, cat, 0, len);

        for (int i = 0; i < n2; i++)
            cat[len + i] = o.charAt(i);

        return new ArrayText<char_t>(cat);
    }

}