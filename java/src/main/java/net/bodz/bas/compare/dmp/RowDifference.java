package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.AbstractRow;
import net.bodz.bas.text.row.IMutableRow;
import net.bodz.bas.text.row.IRow;

/**
 * Class representing one diff operation.
 */
public class RowDifference<cell_t>
        extends AbstractRowDifference<cell_t> {

    public final DifferenceType operation;

    /**
     * The text associated with this diff operation.
     */
    public final IRow<cell_t> row;

    /**
     * Constructor. Initializes the diff with the provided values.
     *
     * @param operation
     *            One of INSERT, DELETE or EQUAL.
     * @param row
     *            The text being applied.
     */
    public RowDifference(DifferenceType operation, IRow<cell_t> row) {
        this.operation = operation;
        this.row = row;
    }

    @Override
    public DifferenceType getDifferenceType() {
        return operation;
    }

    @Override
    public IRow<cell_t> getRow() {
        return row;
    }

    public static <cell_t, T extends cell_t> RowDifference<cell_t> copy(IRowDifference<T> diff) {
        IMutableRow<cell_t> rowCopy = AbstractRow.copy(diff.getRow());
        return new RowDifference<cell_t>(diff.getDifferenceType(), rowCopy);
    }

}
