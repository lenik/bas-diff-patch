package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IRowPatcher<cell_t> {

    void applyPatch(IRow<cell_t> row)
            throws PatcherException;

    PatchApplyResult<cell_t> apply(IRow<cell_t> row)
            throws PatcherException;

}
