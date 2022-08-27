package net.bodz.bas.compare.dmp.rowtype;

import net.bodz.bas.compare.dmp.DiffMatchPatch;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;

public class IntCharsType
        implements
            IDmpRowType<IntegerRow, IRow<? extends Integer>, Integer> {

    public static final int SEP = -1;

    @Override
    public Integer separator() {
        return (int) '\0';
    }

    @Override
    public IntegerRow newRow() {
        return new IntegerRow();
    }

    @Override
    public IntegerRow parse(String s) {
        IntegerRow row = newRow();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            // surrogate pair -> 32-bit int...
            row.append((int) ch);
        }
        return row;
    }

    @Override
    public String format(IRow<? extends Integer> row) {
        StringBuilder sb = new StringBuilder(row.length());
        int n = row.length();
        for (int i = 0; i < n; i++) {
            Integer cell = row.cellAt(i);
            char ch = (char) cell.intValue();
            sb.append(ch);
        }
        return sb.toString();
    }

    @Override
    public Integer createPadding() {
        return (int) '#';
    }

    @Override
    public Integer createPadding(int hint) {
        char ch = (char) hint;
        return (int) ch;
    }

    @Override
    public int cleanupSemanticScore(IRow<? extends Integer> one, IRow<? extends Integer> two) {
        String s1 = format(one);
        String s2 = format(two);
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

}
