package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;

public interface IPackBuilder<cell_t> {

    int pack(IRow<cell_t> row);

    IRow<cell_t> unpack(int index);

    int addIndexRow(IntegerRow indexRow);

    IntegerRow getIndexRow(int indexIndex);

    void setIndexRow(int indexIndex, IntegerRow indexRow);

    PackedRows<cell_t> build();

}
