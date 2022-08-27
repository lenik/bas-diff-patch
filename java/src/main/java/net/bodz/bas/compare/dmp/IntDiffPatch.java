package net.bodz.bas.compare.dmp;

import net.bodz.bas.compare.dmp.rowtype.IDmpRowType;

public class IntDiffPatch
        extends DiffMatchPatch<Integer> {

    public IntDiffPatch() {
        super(IDmpRowType.CSV_INTEGERS);
    }

    public static final IntDiffPatch INSTANCE = new IntDiffPatch();

}
