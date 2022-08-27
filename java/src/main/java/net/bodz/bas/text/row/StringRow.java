package net.bodz.bas.text.row;

import java.util.List;

public class StringRow
        extends MutableRow<String> {

    public StringRow() {
        super();
    }

    public StringRow(List<String> list) {
        super(list);
    }

    public StringRow(List<String> list, boolean copyOnWrite) {
        super(list, copyOnWrite);
    }

}
