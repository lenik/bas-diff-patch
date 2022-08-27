package net.bodz.bas.compare.dmp;

import net.bodz.bas.compare.dmp.rowtype.IDmpRowType;

public class LineDiffPatch
        extends DiffMatchPatch<String> {

    public LineDiffPatch() {
        super(IDmpRowType.RAW_LINES);
    }

    public static final LineDiffPatch INSTANCE = new LineDiffPatch();

}
