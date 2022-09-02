package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.BoolsView;
import net.bodz.bas.text.row.IMutableRow;
import net.bodz.bas.text.row.IRow;

public class PatchApplyResult<cell_t> {

    public IMutableRow<cell_t> row;
    public EditList<cell_t> changes;
    public IRow<Boolean> results;

    public PatchApplyResult(IMutableRow<cell_t> row, IRow<Boolean> results) {
        this.row = row;
        this.results = results;
    }

    public PatchApplyResult(IMutableRow<cell_t> row, boolean... results) {
        this(row, new BoolsView(results));
    }

}
