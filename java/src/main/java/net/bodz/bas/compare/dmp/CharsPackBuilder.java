package net.bodz.bas.compare.dmp;

import java.util.List;

import net.bodz.bas.text.row.IntegerRow;
import net.bodz.bas.text.row.StringView;

public class CharsPackBuilder
        extends PackBuilder<Character> {

    public CharsPackBuilder(int capacityInRows) {
        super(capacityInRows);
    }

    public IndexedRef<IntegerRow> pack(List<? extends String> strings) {
        IntegerRow indexRow = new IntegerRow();
        for (String s : strings) {
            int index = pack(new StringView(s));
            indexRow.append(index);
        }
        int indexIndex = addIndexRow(indexRow);
        return new IndexedRef<IntegerRow>(indexIndex, indexRow);
    }

    public CharsPackBuilder pack(List<? extends String> list1, List<? extends String> list2) {
        pack(list1);
        pack(list2);
        return this;
    }

}
