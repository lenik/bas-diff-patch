package net.bodz.bas.compare.dmp;

import java.util.ArrayList;
import java.util.List;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;

public class PackedRows<cell_t>
        extends ArrayList<IntegerRow> {

    private static final long serialVersionUID = 1L;

    public final List<IRow<cell_t>> packArray;

    public PackedRows(List<IRow<cell_t>> packArray, IntegerRow indexRow1, IntegerRow indexRow2) {
        super(2);
        this.packArray = packArray;
        add(indexRow1);
        add(indexRow2);
    }

    public IntegerRow getIndexRow1() {
        return get(0);
    }

    public IntegerRow getIndexRow2() {
        return get(1);
    }

}
