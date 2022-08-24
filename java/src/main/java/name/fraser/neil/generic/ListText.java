package name.fraser.neil.generic;

import java.util.ArrayList;
import java.util.List;

public class ListText<char_t>
        extends AbstractText<char_t> {

    List<char_t> list;

    public ListText() {
        list = new ArrayList<char_t>();
    }

    public ListText(List<char_t> list) {
        this.list = list;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= list.size())
            throw new IndexOutOfBoundsException(String.valueOf(index));
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
    public char_t charAt(int index) {
        checkIndex(index);
        return list.get(index);
    }

    @Override
    public Text<char_t> substring(int begin) {
        checkIndex(begin);
        List<char_t> subList = list.subList(begin, list.size());
        return new ListText<char_t>(subList);
    }

    @Override
    public Text<char_t> substring(int begin, int end) {
        checkIndex(begin);
        checkIndex(end);
        if (end < begin)
            throw new IllegalArgumentException("end is less than begin");
        List<char_t> subList = list.subList(begin, end);
        return new ListText<char_t>(subList);
    }

    private List<char_t> _alloc(int n) {
        return new ArrayList<char_t>(n);
    }

    @Override
    public Text<char_t> concat(char_t ch) {
        List<char_t> cat = _alloc(list.size() + 1);
        cat.addAll(list);
        cat.add(ch);
        return new ListText<char_t>(cat);
    }

    @Override
    public Text<char_t> concat(Text<char_t> o) {
        int n2 = o.length();
        List<char_t> cat = _alloc(list.size() + n2);
        cat.addAll(list);

        for (int i = 0; i < n2; i++)
            cat.add(o.charAt(i));

        return new ListText<char_t>(cat);
    }

    public ListText<char_t> append(char_t ch) {
        list.add(ch);
        return this;
    }

    public ListText<char_t> append(Text<char_t> o) {
        int n = o.length();
        for (int i = 0; i < n; i++)
            list.add(o.charAt(i));
        return this;
    }

    public void clear() {
        list.clear();
    }

    public void delete(int begin) {
        delete(begin, length());
    }

    public void delete(int begin, int end) {
        checkIndex(begin);
        checkIndex(end);
        int len = end - begin;
        for (int i = 0; i < len; i++)
            list.remove(end - 1 - i);
    }

    public void preserve(int begin) {
        delete(0, begin);
    }

    public void preserve(int begin, int end) {
        delete(end);
        delete(0, begin);
    }

}
