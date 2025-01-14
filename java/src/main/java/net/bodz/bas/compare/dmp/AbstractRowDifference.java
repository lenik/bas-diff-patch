package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.Function;
import net.bodz.bas.text.row.IRow;

public abstract class AbstractRowDifference<cell_t>
        implements
            IRowDifference<cell_t> {

    @Override
    public RowEdit<cell_t> copy() {
        return new RowEdit<cell_t>(getDifferenceType(), getDelta().copy());
    }

    @Override
    public <T> RowEdit<T> copy(Function<cell_t, T> function) {
        return new RowEdit<T>(getDifferenceType(), getDelta().copy(function));
    }

    public String getTextAsString() {
        IRow<cell_t> row = getDelta();
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
        DifferenceType type = getDifferenceType();
        int result = (type == null) ? 0 : type.hashCode();
        IRow<cell_t> row = getDelta();
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
        DifferenceType type1 = getDifferenceType();
        DifferenceType type2 = other.getDifferenceType();
        if (type1 != type2) {
            return false;
        }
        IRow<cell_t> row1 = getDelta();
        IRow<?> row2 = other.getDelta();
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
