package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.Function;
import net.bodz.bas.text.row.IRow;

public abstract class AbstractRowDifference<cell_t>
        implements
            IRowDifference<cell_t> {

    @Override
    public RowEdit<cell_t> copy() {
        return new RowEdit<cell_t>(getDifferenceType(), getRow().copy());
    }

    @Override
    public <T> RowEdit<T> copy(Function<cell_t, T> function) {
        return new RowEdit<T>(getDifferenceType(), getRow().copy(function));
    }

    public String getTextAsString() {
        IRow<cell_t> row = getRow();
        int n = row.length();
        StringBuilder buf = new StringBuilder(n * 100);
        for (int i = 0; i < n; i++) {
            cell_t cell = row.cellAt(i);
            buf.append(cell);
        }
        return buf.toString();
    }

    /**
     * Display a human-readable version of this Diff.
     *
     * @return text version.
     */
    @Override
    public String toString() {
        String prettyText = getTextAsString().replace('\n', '\u00b6');
        return "Diff(" + this.getDifferenceType() + ",\"" + prettyText + "\")";
    }

    /**
     * Create a numeric hash value for a Diff. This function is not used by DMP.
     *
     * @return Hash value.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        DifferenceType operation = getDifferenceType();
        int result = (operation == null) ? 0 : operation.hashCode();
        IRow<cell_t> row = getRow();
        result += prime * ((row == null) ? 0 : row.hashCode());
        return result;
    }

    /**
     * Is this Diff equivalent to another Diff?
     *
     * @param obj
     *            Another Diff to compare against.
     * @return true or false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractRowDifference<?> other = (AbstractRowDifference<?>) obj;
        DifferenceType operation1 = getDifferenceType();
        DifferenceType operation2 = other.getDifferenceType();
        if (operation1 != operation2) {
            return false;
        }
        IRow<cell_t> row1 = getRow();
        IRow<?> row2 = other.getRow();
        if (row1 == null) {
            if (row2 != null) {
                return false;
            }
        } else if (!row1.equals(row2)) {
            return false;
        }
        return true;
    }

}
