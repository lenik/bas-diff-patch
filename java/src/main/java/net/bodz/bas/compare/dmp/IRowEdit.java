package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IMutableRow;

public interface IRowEdit<cell_t>
        extends
            IRowDifference<cell_t> {

    @Override
    DifferenceType getDifferenceType();

    void setDifferenceType(DifferenceType type);

    @Override
    IMutableRow<cell_t> getDelta();

    void setDelta(IMutableRow<cell_t> row);

}
