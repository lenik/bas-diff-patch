package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.AbstractRow;
import net.bodz.bas.text.row.IMutableRow;

public class RowEdit<cell_t>
        extends AbstractRowDifference<cell_t>
        implements
            IRowEdit<cell_t> {

    DifferenceType type;
    IMutableRow<cell_t> delta;

    public RowEdit(DifferenceType type, IMutableRow<cell_t> delta) {
        this.type = type;
        this.delta = delta;
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
    public IMutableRow<cell_t> getDelta() {
        return delta;
    }

    @Override
    public void setDelta(IMutableRow<cell_t> delta) {
        this.delta = delta;
    }

    public static <cell_t, T extends cell_t> RowEdit<cell_t> copy(IRowDifference<T> o) {
        IMutableRow<cell_t> deltaCopy = AbstractRow.copy(o.getDelta());
        return new RowEdit<cell_t>(o.getDifferenceType(), deltaCopy);
    }

}
