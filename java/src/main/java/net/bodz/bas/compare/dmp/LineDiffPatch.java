package net.bodz.bas.compare.dmp;

public class LineDiffPatch
        extends DiffMatchPatch<String> {

    public LineDiffPatch() {
        super(ICharDiffType.LINE_LIST);
    }

    public static final LineDiffPatch INSTANCE = new LineDiffPatch();

}
