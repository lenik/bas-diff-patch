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

    public void add(Patch<cell_t> patch, MatchStatus status) {
        add(new PatchApplyStatus<cell_t>(patch, status));
    }

    public boolean isError() {
        for (PatchApplyStatus<cell_t> item : this)
            if (item.isError())
                return true;
        return false;
    }

}
