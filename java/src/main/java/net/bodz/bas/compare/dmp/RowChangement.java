package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

/**
 * Class representing one diff operation.
 */
public class RowChangement<cell_t> {

    /**
     * One of: INSERT, DELETE or EQUAL.
     */
    public Operation operation;

    /**
     * The text associated with this diff operation.
     */
    public IRow<cell_t> text;

    public boolean atom;

    /**
     * Constructor. Initializes the diff with the provided values.
     *
     * @param operation
     *            One of INSERT, DELETE or EQUAL.
     * @param text
     *            The text being applied.
     */
    public RowChangement(Operation operation, IRow<cell_t> text) {
        this(operation, text, false);
    }

    public RowChangement(Operation operation, IRow<cell_t> text, boolean atom) {
        // Construct a diff with the specified operation and text.
        this.operation = operation;
        this.text = text;
        this.atom = atom;
    }

    public String getTextAsString() {
        int n = text.length();
        StringBuilder buf = new StringBuilder(n * 100);
        for (int i = 0; i < n; i++) {
            cell_t line = text.cellAt(i);
            if (atom)
                buf.append((char) ((Integer) line).intValue());
            else
                buf.append(line);
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
        return "Diff(" + this.operation + ",\"" + prettyText + "\")";
    }

    /**
     * Create a numeric hash value for a Diff. This function is not used by DMP.
     *
     * @return Hash value.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (operation == null) ? 0 : operation.hashCode();
        result += prime * ((text == null) ? 0 : text.hashCode());
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
        RowChangement<?> other = (RowChangement<?>) obj;
        if (operation != other.operation) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }
}
