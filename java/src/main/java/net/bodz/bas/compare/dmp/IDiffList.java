package net.bodz.bas.compare.dmp;

import java.util.Collection;
import java.util.ListIterator;

import net.bodz.bas.text.row.IRow;

public interface IDiffList<diff_t extends IRowDifference<cell_t>, cell_t>
        extends
            Iterable<diff_t> {

    int size();

    boolean isEmpty();

    ListIterator<diff_t> listIterator();

    diff_t getFirst();

    diff_t getLast();

    diff_t removeFirst();

    diff_t removeLast();

    void addAll(Collection<? extends diff_t> diffs);

    void addAll(IDiffList<? extends diff_t, ? extends cell_t> diffs);

    void add(diff_t diff);

    <T extends cell_t> void prepend(IRowDifference<T> diff);

    <T extends cell_t> void append(IRowDifference<T> diff);

    /**
     * loc is a location in row1, compute and return the equivalent location in row2. e.g. "The
     * cat" vs "The big cat", 1->1, 5->8
     *
     * @param diffs
     *            List of Diff objects.
     * @param loc
     *            Location within row1.
     * @return Location within row2.
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
     * Crush the diff into an encoded string which describes the types required to transform
     * row1 into row2. E.g. =3\t-2\t+ing -> Keep 3 chars, delete 2 chars, insert 'ing'. Operations
     * are tab-separated. Inserted text is escaped using %xx notation.
     *
     * @param diffs
     *            List of Diff objects.
     * @return Delta text.
     */
    String toDelta();

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
    void readDelta(IRow<cell_t> row1, String delta)
            throws IllegalArgumentException;
}
