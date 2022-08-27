package net.bodz.bas.text.row;

import java.lang.reflect.Array;

public class ArrayView<cell_t>
        extends AbstractRow<cell_t> {

    cell_t[] array;
    int off;
    int len;

    public ArrayView(cell_t[] array) {
        this(array, 0, array.length);
    }

    public ArrayView(cell_t[] array, int begin, int end) {
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

    @Override
    public int length() {
        return len;
    }

    @Override
    public boolean isEmpty() {
        return len == 0;
    }

    @Override
    public cell_t cellAt(int index) {
        index = wrapIndex(index);
        return array[off + index];
    }

    @Override
    public ArrayView<cell_t> slice(int begin) {
        begin = wrapBegin(begin);
        return new ArrayView<cell_t>(array, off + begin, off + len);
    }

    @Override
    public ArrayView<cell_t> slice(int begin, int end) {
        begin = wrapBegin(begin);
        end = wrapEnd(begin, end);
        return new ArrayView<cell_t>(array, off + begin, off + end);
    }

    cell_t[] _alloc(int n) {
        Class<?> c = array.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        cell_t[] array = (cell_t[]) Array.newInstance(c, n);
        return array;
    }

    @Override
    public ArrayView<cell_t> concat(cell_t cell) {
        cell_t[] cat = _alloc(len + 1);
        System.arraycopy(array, off, cat, 0, len);
        cat[len] = cell;
        return new ArrayView<cell_t>(cat);
    }

    @Override
    public ArrayView<cell_t> concat(IRow<? extends cell_t> row) {
        int len2 = row.length();
        cell_t[] cat = _alloc(len + len2);
        System.arraycopy(array, off, cat, 0, len);

        for (int i = 0; i < len2; i++)
            cat[len + i] = row.cellAt(i);

        return new ArrayView<cell_t>(cat);
    }

}
