package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.AbstractRow;
import net.bodz.bas.text.row.Function;
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

    public RowEdit<cell_t> copy() {
        return new RowEdit<cell_t>(operation, row.copy());
    }

    public <T> RowEdit<T> copy(Function<cell_t, T> function) {
        return new RowEdit<T>(operation, row.copy(function));
    }

    public static <cell_t, T extends cell_t> RowEdit<cell_t> copy(RowDifference<T> change) {
        IMutableRow<cell_t> rowCopy = AbstractRow.copy(change.row);
        return new RowEdit<cell_t>(change.operation, rowCopy);
    }

}
