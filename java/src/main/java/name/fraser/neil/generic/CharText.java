package name.fraser.neil.generic;

public class CharText
        extends AbstractText<Character> {

    char[] array;
    int off;
    int len;

    public CharText(char[] array) {
        this(array, 0, array.length);
    }

    public CharText(char[] array, int start, int end) {
        if (array == null)
            throw new NullPointerException("array");
        if (start < 0 || start >= array.length)
            throw new IndexOutOfBoundsException("illegal start: " + start);
        if (end < 0 || end > array.length)
            throw new IndexOutOfBoundsException("illegal end: " + end);
        this.array = array;
        this.off = start;
        this.len = end - start;
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
        return array[index];
    }

    public char _charAt(int index) {
        checkIndex(index);
        return array[index];
    }

    @Override
    public CharText substring(int begin) {
        checkIndex(begin);
        return new CharText(array, off + begin, off + len);
    }

    @Override
    public CharText substring(int begin, int end) {
        checkIndex(begin);
        checkIndex(end);
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        return new CharText(array, off + begin, off + (end - begin));
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
