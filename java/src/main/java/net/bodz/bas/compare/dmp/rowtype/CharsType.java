package net.bodz.bas.compare.dmp.rowtype;

import net.bodz.bas.compare.dmp.DiffMatchPatch;
import net.bodz.bas.text.row.CharsView;
import net.bodz.bas.text.row.IRow;

class CharsType
        implements
            IDmpRowType<CharsView, IRow<? extends Character>, Character> {

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
    public CharsView newRow() {
        return new CharsView();
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
    public int cleanupSemanticScore(IRow<? extends Character> one, IRow<? extends Character> two) {
        String s1 = format(one);
        String s2 = format(two);
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

}
