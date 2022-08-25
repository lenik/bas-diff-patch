package name.fraser.neil.generic;

public class CharDiffPatch
        extends DiffMatchPatch<Character> {

    public CharDiffPatch() {
        super(ICharDiffType.CHAR_ARRAY);
    }

    public static final CharDiffPatch INSTANCE = new CharDiffPatch();

}
