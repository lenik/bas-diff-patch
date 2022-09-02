package net.bodz.bas.text.row;

import java.util.List;

public class StringRow
        extends MutableRow<String> {

    public StringRow() {
        super();
    }

    @SuppressWarnings("unchecked")
    public StringRow(List<? extends String> list) {
        super((List<String>) list);
    }

    public StringRow(List<String> list, boolean copyOnWrite) {
        super(list, copyOnWrite);
    }

}
