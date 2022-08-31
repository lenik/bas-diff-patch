package net.bodz.bas.text.row;

public class StringView
        extends AbstractRow<Character> {

    final String s;
    final int len;

    public StringView(String s) {
        if (s == null)
            throw new NullPointerException("s");
        this.s = s;
        this.len = s.length();
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
    protected int wrapIndex(int index) {
        if (index < 0)
            index += len;
        if (index < 0 || index >= len)
            throw new IndexOutOfBoundsException(String.valueOf(index));
        return index;
    }

    @Override
    protected int wrapBegin(int begin) {
        if (begin < 0)
            begin += len;
        if (begin < 0 || begin > len)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        return begin;
    }

    @Override
    protected int wrapEnd(int begin, int end) {
        if (begin < 0)
            begin += len;
        if (end < 0)
            end += len;
        if (begin < 0)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        if (end < 0 || end > len)
            throw new IndexOutOfBoundsException(String.valueOf(end));
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        return end;
    }

    @Override
    public Character cellAt(int index) {
        index = wrapIndex(index);
        return s.charAt(index);
    }

    public char _charAt(int index) {
        index = wrapIndex(index);
        return s.charAt(index);
    }

    @Override
    public StringView slice(int begin) {
        begin = wrapBegin(begin);
        return new StringView(s.substring(begin));
    }

    @Override
    public StringView slice(int begin, int end) {
        begin = wrapBegin(begin);
        end = wrapEnd(begin, end);
        return new StringView(s.substring(begin, end));
    }

    @Override
    public StringView concat(Character ch) {
        return concat(ch.charValue());
    }

    public StringView concat(char ch) {
        return new StringView(s + ch);
    }

    @Override
    public StringView concat(IRow<? extends Character> row) {
        int len2 = row.length();
        StringBuilder cat = new StringBuilder(len + len2);
        cat.append(s);

        for (int i = 0; i < len2; i++)
            cat.append(row.cellAt(i));

        return new StringView(cat.toString());
    }

    @Override
    public String toString() {
        return s;
    }

}
