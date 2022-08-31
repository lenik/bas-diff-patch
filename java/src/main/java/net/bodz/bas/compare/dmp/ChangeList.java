package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public class ChangeList<cell_t>
        extends AbstractDiffList<RowDifference<cell_t>, cell_t> {

    public ChangeList(DMPRowComparator<cell_t> dmp) {
        super(dmp);
    }

    @Override
    public <T extends cell_t> void prepend(IRowDifference<T> o) {
        list.addFirst(RowDifference.<cell_t, T> copy(o));
    }

    @Override
    public <T extends cell_t> void append(IRowDifference<T> o) {
        list.addLast(RowDifference.<cell_t, T> copy(o));
    }

    @Override
    protected RowDifference<cell_t> createDifference(DifferenceType type, IRow<cell_t> row,
            boolean allocated) {
        return new RowDifference<cell_t>(type, row);
    }

    /**
     * Given the original row1, and an encoded string which describes the types required to
     * transform row1 into row2, compute the full diff.
     *
     * @param row1
     *            Source string for the diff.
     * @param delta
     *            Delta text.
     * @throws IllegalArgumentException
     *             If invalid input.
     */
    public static <cell_t> ChangeList<cell_t> fromDelta(DMPRowComparator<cell_t> dmp, //
            IRow<cell_t> row1, String delta)
            throws IllegalArgumentException {
        ChangeList<cell_t> list = new ChangeList<cell_t>(dmp);
        list.readDelta(row1, delta);
        return list;
    }

}
