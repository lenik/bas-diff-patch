package net.bodz.bas.compare.dmp;

import java.util.List;

import net.bodz.bas.text.row.IRow;

/**
 * Internal class for returning results from diff_linesToChars(). Other less paranoid languages just
 * use a three-element array.
 */
public class LinesToCharsResult<char_t> {

    protected IRow<Integer> chars1;
    protected IRow<Integer> chars2;
    protected List<IRow<char_t>> lineArray;

    protected LinesToCharsResult(IRow<Integer> chars1, IRow<Integer> chars2, List<IRow<char_t>> lineArray) {
        this.chars1 = chars1;
        this.chars2 = chars2;
        this.lineArray = lineArray;
    }

}
