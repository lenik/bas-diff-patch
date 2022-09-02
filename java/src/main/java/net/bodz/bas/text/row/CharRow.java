package net.bodz.bas.text.row;

import java.util.List;

public class CharRow
        extends MutableRow<Character> {

    public CharRow() {
        super();
    }

    public CharRow(int capacity) {
        super(capacity);
    }

    public CharRow(List<Character> list) {
        super(list);
    }

    @SuppressWarnings("unchecked")
    public CharRow(List<? extends Character> list, boolean allocated) {
        super((List<Character>) list, allocated);
    }

}
