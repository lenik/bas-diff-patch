package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.CharsView;
import net.bodz.bas.text.row.IRow;

public class CharsComparator
        extends DMPRowComparator<Character> {

    public CharsComparator(DMPConfig config) {
        super(config);
    }

    @Override
    public CharsView parse(String s) {
        return new CharsView(s);
    }

    @Override
    public String format(IRow<? extends Character> text) {
        return text.toString();
    }

    @Override
    protected Character separator() {
        return '\n';
    }

    @Override
    protected Character createPadding() {
        return '#';
    }

    @Override
    protected Character createPadding(int hint) {
        return (char) hint;
    }

    @Override
    public int cleanupSemanticScore(IRow<? extends Character> one, IRow<? extends Character> two) {
        String s1 = format(one);
        String s2 = format(two);
        return StringStats.diff_cleanupSemanticScore(s1, s2);
    }

}
