package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.AbstractRow;
import net.bodz.bas.text.row.IMutableRow;

public class RowEdit<cell_t>
        extends AbstractRowDifference<cell_t>
        implements
            IRowEdit<cell_t> {

    DifferenceType type;
    IMutableRow<cell_t> row;

    public RowEdit(DifferenceType type, IMutableRow<cell_t> row) {
        this.type = type;
        this.row = row;
    }

    @Override
    public DifferenceType getDifferenceType() {
        return type;
    }

    @Override
    public void setDifferenceType(DifferenceType type) {
        this.type = type;
    }

    @Override
    public IMutableRow<cell_t> getRow() {
        return row;
    }

    @Override
    public void setRow(IMutableRow<cell_t> row) {
        this.row = row;
    }

    public static <cell_t, T extends cell_t> RowEdit<cell_t> copy(IRowDifference<T> diff) {
        IMutableRow<cell_t> rowCopy = AbstractRow.copy(diff.getRow());
        return new RowEdit<cell_t>(diff.getDifferenceType(), rowCopy);
    }

}
