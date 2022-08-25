package name.fraser.neil.generic;

public interface ICharDiffType<char_t>
        extends
            ICharType<char_t> {

    char_t createJunk();

    char_t createPadding();

    char_t createPadding(int hint);

    int cleanupSemanticScore(Text<? extends char_t> one, Text<? extends char_t> two);

    CharDiffType CHAR_ARRAY = new CharDiffType();
    IntDiffType INT_LIST = new IntDiffType();

}
