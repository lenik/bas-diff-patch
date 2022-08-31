package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public class ChangeList<cell_t>
        extends AbstractDiffList<RowDifference<cell_t>, cell_t> {

    private static final long serialVersionUID = 1L;

    public ChangeList(DMPRowComparator<cell_t> dmp) {
        super(dmp);
    }

    @Override
    protected RowDifference<cell_t> createDifference(DifferenceType type, IRow<cell_t> row, boolean allocated) {
        return new RowDifference<cell_t>(type, row);
    }

    /**
     * Given the original text1, and an encoded string which describes the operations required to
     * transform text1 into text2, compute the full diff.
     *
     * @param text1
     *            Source string for the diff.
     * @param delta
     *            Delta text.
     * @throws IllegalArgumentException
     *             If invalid input.
     */
    public static <cell_t> ChangeList<cell_t> fromDelta(DMPRowComparator<cell_t> dmp, //
            IRow<cell_t> text1, String delta)
            throws IllegalArgumentException {
        ChangeList<cell_t> list = new ChangeList<cell_t>(dmp);
        list.readDelta(text1, delta);
        return list;
    }

}
