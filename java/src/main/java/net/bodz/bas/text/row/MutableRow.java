package net.bodz.bas.text.row;

import java.util.ArrayList;
import java.util.List;

public class MutableRow<cell_t>
        extends AbstractMutableRow<cell_t> {

    static final int CAPACITY_DEFAULT = 10;

    List<cell_t> list;
    boolean allocated;

    public MutableRow() {
        this(CAPACITY_DEFAULT);
    }

    public MutableRow(int capacity) {
        list = _alloc(capacity);
        allocated = true;
    }

    public MutableRow(List<cell_t> list) {
        this(list, false);
    }

    public MutableRow(List<cell_t> list, boolean allocated) {
        this.list = list;
        this.allocated = allocated;
    }

    private void beforeWrite() {
        if (!allocated) {
            List<cell_t> newList = _alloc(length());
            newList.addAll(list);
            list = newList;
            allocated = true;
        }
    }

    @Override
    public int length() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public cell_t cellAt(int index) {
        index = wrapIndex(index);
        return list.get(index);
    }

    @Override
    public void set(int index, cell_t cell) {
        index = wrapIndex(index);
        list.set(index, cell);
    }

    @Override
    public MutableRow<cell_t> slice(int begin) {
        begin = wrapBegin(begin);
        List<cell_t> subList = list.subList(begin, list.size());
        subList = new ArrayList<cell_t>(subList);
        return new MutableRow<cell_t>(subList, true);
    }

    @Override
    public MutableRow<cell_t> slice(int begin, int end) {
        if (begin < 0)
            throw new IndexOutOfBoundsException(String.valueOf(begin));
        if (end < 0 || end > list.size())
            throw new IndexOutOfBoundsException(String.valueOf(end));
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        List<cell_t> subList = list.subList(begin, end);
        subList = new ArrayList<cell_t>(subList);
        return new MutableRow<cell_t>(subList, true);
    }

    private List<cell_t> _alloc(int n) {
        return new ArrayList<cell_t>(n);
    }

    @Override
    public MutableRow<cell_t> concat(cell_t ch) {
        List<cell_t> cat = _alloc(list.size() + 1);
        cat.addAll(list);
        cat.add(ch);
        return new MutableRow<cell_t>(cat, true);
    }

    @Override
    public MutableRow<cell_t> concat(IRow<? extends cell_t> o) {
        int n2 = o.length();
        List<cell_t> cat = _alloc(list.size() + n2);
        cat.addAll(list);

        for (int i = 0; i < n2; i++)
            cat.add(o.cellAt(i));

        return new MutableRow<cell_t>(cat, true);
    }

    @Override
    public synchronized MutableRow<cell_t> append(cell_t ch) {
        beforeWrite();
        list.add(ch);
        return this;
    }

    @Override
    public MutableRow<cell_t> append(IRow<? extends cell_t> o) {
        beforeWrite();
        int n = o.length();
        for (int i = 0; i < n; i++)
            list.add(o.cellAt(i));
        return this;
    }

    @Override
    public void clear() {
        beforeWrite();
        list.clear();
    }

    @Override
    public void delete(int begin) {
        begin = wrapBegin(begin);
        delete(begin, length());
    }

    @Override
    public synchronized void delete(int begin, int end) {
        begin = wrapBegin(begin);
        end = wrapEnd(begin, end);
        beforeWrite();
        int len = end - begin;
        for (int i = 0; i < len; i++)
            list.remove(end - 1 - i);
    }

    @Override
    public void preserve(int begin) {
        delete(0, begin);
    }

    @Override
    public synchronized void preserve(int begin, int end) {
        delete(end);
        delete(0, begin);
    }

    @Override
    public synchronized MutableRow<cell_t> splice(int begin, int end) {
        MutableRow<cell_t> slice = slice(begin, end);
        delete(begin, end);
        return slice;
    }

    public MutableRow<cell_t> copy() {
        List<cell_t> list = new ArrayList<cell_t>(this.list);
        return new MutableRow<cell_t>(list, true);
    }

}
