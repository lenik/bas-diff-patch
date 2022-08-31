package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.AbstractRow;
import net.bodz.bas.text.row.IMutableRow;
import net.bodz.bas.text.row.IRow;

/**
 * Class representing one diff type.
 */
public class RowDifference<cell_t>
        extends AbstractRowDifference<cell_t> {

    public final DifferenceType type;

    /**
     * The text associated with this diff type.
     */
    public final IRow<cell_t> delta;

    /**
     * Constructor. Initializes the diff with the provided values.
     *
     * @param type
     *            One of INSERT, DELETE or EQUAL.
     * @param delta
     *            The text being applied.
     */
    public RowDifference(DifferenceType type, IRow<cell_t> delta) {
        this.type = type;
        this.delta = delta;
    }

    @Override
    public DifferenceType getDifferenceType() {
        return type;
    }

    @Override
    public IRow<cell_t> getDelta() {
        return delta;
    }

    public static <cell_t, T extends cell_t> RowDifference<cell_t> copy(IRowDifference<T> o) {
        IMutableRow<cell_t> deltaCopy = AbstractRow.copy(o.getDelta());
        return new RowDifference<cell_t>(o.getDifferenceType(), deltaCopy);
    }

}
