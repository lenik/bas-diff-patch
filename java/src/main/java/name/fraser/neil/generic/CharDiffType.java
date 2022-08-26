package name.fraser.neil.generic;

import net.bodz.bas.text.generic.CharText;
import net.bodz.bas.text.generic.Text;

class CharDiffType
        implements
            ICharDiffType<Character> {

    @Override
    public Character separator() {
        return '\n';
    }

    @Override
    public Character createPadding() {
        return '#';
    }

    @Override
    public Character createPadding(int hint) {
        return (char) hint;
    }

    @Override
    public String format(Text<? extends Character> text) {
        return text.asString();
    }

    @Override
    public Text<Character> parse(String s) {
        return new CharText(s);
    }

    @Override
    public int cleanupSemanticScore(Text<? extends Character> one, Text<? extends Character> two) {
        String s1 = one.asString();
        String s2 = two.asString();
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

}
