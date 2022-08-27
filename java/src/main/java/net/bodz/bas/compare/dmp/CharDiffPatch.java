package net.bodz.bas.compare.dmp;

import net.bodz.bas.compare.dmp.rowtype.IDmpRowType;

public class CharDiffPatch
        extends DiffMatchPatch<Character> {

    public CharDiffPatch() {
        super(IDmpRowType.CHARS);
    }

    public static final CharDiffPatch INSTANCE = new CharDiffPatch();

}
