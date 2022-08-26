package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.generic.Text;

public interface ICharDiffType<char_t>
        extends
            ICharType<char_t> {

    char_t separator();

    char_t createPadding();

    char_t createPadding(int hint);

    int cleanupSemanticScore(Text<? extends char_t> one, Text<? extends char_t> two);

    CharDiffType CHAR_ARRAY = new CharDiffType();
    IntDiffType INT_LIST = new IntDiffType();
    LineDiffType LINE_LIST = new LineDiffType();
    LineLnDiffType LINE_LN_LIST = new LineLnDiffType();

}
