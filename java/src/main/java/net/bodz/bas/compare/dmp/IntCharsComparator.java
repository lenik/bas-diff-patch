package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;

public class IntCharsComparator
        extends DMPRowComparator<Integer> {

    public static final int SEP = -1;

    public IntCharsComparator(DMPConfig config) {
        super(config);
    }

    @Override
    public IntegerRow parse(String s) {
        IntegerRow row = new IntegerRow();
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
    protected Integer separator() {
        return (int) '\0';
    }

    @Override
    protected Integer createPadding() {
        return (int) '#';
    }

    @Override
    protected Integer createPadding(int hint) {
        char ch = (char) hint;
        return (int) ch;
    }

    @Override
    public int cleanupSemanticScore(IRow<? extends Integer> one, IRow<? extends Integer> two) {
        String s1 = format(one);
        String s2 = format(two);
        return StringStats.diff_cleanupSemanticScore(s1, s2);
    }

}
