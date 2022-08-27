package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public class PatchApplyResult<char_t> {

    public IRow<char_t> text;
    public boolean[] results;

    public PatchApplyResult(IRow<char_t> text, boolean... results) {
        this.text = text;
        this.results = results;
    }

}
