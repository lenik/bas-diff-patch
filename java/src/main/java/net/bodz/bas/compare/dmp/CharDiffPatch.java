package net.bodz.bas.compare.dmp;

public class CharDiffPatch
        extends DiffMatchPatch<Character> {

    public CharDiffPatch() {
        super(ICharDiffType.CHAR_ARRAY);
    }

    public static final CharDiffPatch INSTANCE = new CharDiffPatch();

}
