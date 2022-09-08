package net.bodz.bas.compare.dmp;

import java.util.ArrayList;

import net.bodz.bas.text.row.IMutableRow;

public class PatchApplyResult<cell_t>
        extends ArrayList<PatchApplyStatus<cell_t>> {

    private static final long serialVersionUID = 1L;

    IMutableRow<cell_t> patchedRow;
    EditList<cell_t> changes;

    public PatchApplyResult() {
    }

    public IMutableRow<cell_t> getPatchedRow() {
        return patchedRow;
    }

    public EditList<cell_t> getChanges() {
        return changes;
    }

    public void setPatchedRow(IMutableRow<cell_t> patchedRow) {
        this.patchedRow = patchedRow;
    }

    public void setChanges(EditList<cell_t> changes) {
        this.changes = changes;
    }

    public boolean isError() {
        for (PatchApplyStatus<cell_t> status : this)
            if (status.isError())
                return true;
        return false;
    }

}
