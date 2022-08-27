package net.bodz.bas.text.row;

public class Rows {

    static ArrayView<Object> EMPTY = new ArrayView<Object>(new Object[0]);

    @SuppressWarnings("unchecked")
    public static <char_t> IRow<char_t> empty() {
        return (IRow<char_t>) EMPTY;
    }

}
