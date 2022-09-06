package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IRowMatcher<cell_t> {

    /**
     * Locate the best instance of 'pattern' in 'text' near 'loc'. Returns -1 if no match found.
     *
     * @param row
     *            The text to search.
     * @param pattern
     *            The pattern to search for.
     * @param loc
     *            The location to search around.
     * @return Best match index or -1.
     */
    <T extends cell_t> int search(IRow<T> row, IRow<T> pattern, int loc);

}
