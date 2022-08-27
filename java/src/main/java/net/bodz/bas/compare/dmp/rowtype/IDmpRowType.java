package net.bodz.bas.compare.dmp.rowtype;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IRowType;

public interface IDmpRowType<mutable_t extends IRow<?>, view_t extends IRow<?>, cell_t>
        extends
            IRowType<mutable_t, view_t> {

    cell_t separator();

    cell_t createPadding();

    cell_t createPadding(int hint);

    int cleanupSemanticScore(view_t one, view_t two);

    CharsType CHARS = new CharsType();
    IntCharsType INT_CHARS = new IntCharsType();
    CsvIntegersType CSV_INTEGERS = new CsvIntegersType();
    LinesType RAW_LINES = new LinesType();
    ChoppedLinesType CHOPPED_LINES = new ChoppedLinesType();

}
