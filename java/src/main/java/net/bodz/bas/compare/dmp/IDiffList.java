package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IDiffList<diff_t extends IRowDifference<cell_t>, cell_t> {

    /**
     * loc is a location in text1, compute and return the equivalent location in text2. e.g. "The
     * cat" vs "The big cat", 1->1, 5->8
     *
     * @param diffs
     *            List of Diff objects.
     * @param loc
     *            Location within text1.
     * @return Location within text2.
     */
    int xIndex(int loc);

    /**
     * Convert a Diff list into a pretty HTML report.
     *
     * @param diffs
     *            List of Diff objects.
     * @return HTML representation.
     */
    String prettyHtml();

    /**
     * Compute and return the source text (all equalities and deletions).
     *
     * @param diffs
     *            List of Diff objects.
     * @return Source text.
     */
    IRow<cell_t> restoreRow1();

    /**
     * Compute and return the destination text (all equalities and insertions).
     *
     * @param diffs
     *            List of Diff objects.
     * @return Destination text.
     */
    IRow<cell_t> restoreRow2();

    /**
     * Compute the Levenshtein distance; the number of inserted, deleted or substituted characters.
     *
     * @param diffs
     *            List of Diff objects.
     * @return Number of changes.
     */
    int levenshtein();

    /**
     * Crush the diff into an encoded string which describes the operations required to transform
     * text1 into text2. E.g. =3\t-2\t+ing -> Keep 3 chars, delete 2 chars, insert 'ing'. Operations
     * are tab-separated. Inserted text is escaped using %xx notation.
     *
     * @param diffs
     *            List of Diff objects.
     * @return Delta text.
     */
    String toDelta();

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
    void readDelta(IRow<cell_t> text1, String delta)
            throws IllegalArgumentException;
}
