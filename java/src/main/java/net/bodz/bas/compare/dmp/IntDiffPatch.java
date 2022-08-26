package net.bodz.bas.compare.dmp;

public class IntDiffPatch
        extends DiffMatchPatch<Integer> {

    public IntDiffPatch() {
        super(ICharDiffType.INT_LIST);
    }

    public static final IntDiffPatch INSTANCE = new IntDiffPatch();

}
