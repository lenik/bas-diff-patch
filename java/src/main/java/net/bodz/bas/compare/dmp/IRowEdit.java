package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IMutableRow;

public interface IRowEdit<cell_t>
        extends
            IRowDifference<cell_t> {

    @Override
    DifferenceType getDifferenceType();

    void setDifferenceType(DifferenceType operation);

    @Override
    IMutableRow<cell_t> getRow();

    void setRow(IMutableRow<cell_t> row);

}
