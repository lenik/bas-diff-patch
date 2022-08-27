package net.bodz.bas.compare.dmp.rowtype;

import java.util.StringTokenizer;

import net.bodz.bas.compare.dmp.DiffMatchPatch;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;

public class CsvIntegersType
        implements
            IDmpRowType<IntegerRow, IRow<? extends Integer>, Integer> {

    public static final int SEP = -1;

    @Override
    public Integer separator() {
        return -1;
    }

    @Override
    public IntegerRow newRow() {
        return new IntegerRow();
    }

    @Override
    public IntegerRow parse(String s) {
        IntegerRow buf = newRow();
        StringTokenizer tokens = new StringTokenizer(",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            int val = Integer.parseInt(token);
            buf.append(val);
        }
        return buf;
    }

    @Override
    public String format(IRow<? extends Integer> row) {
        StringBuilder sb = new StringBuilder(row.length() * 8);
        int n = row.length();
        for (int i = 0; i < n; i++) {
            if (i != 0)
                sb.append(',');
            sb.append(row.cellAt(i));
        }
        return sb.toString();
    }

    @Override
    public Integer createPadding() {
        return 233;
    }

    @Override
    public Integer createPadding(int hint) {
        return hint;
    }

    @Override
    public int cleanupSemanticScore(IRow<? extends Integer> one, IRow<? extends Integer> two) {
        String s1 = format(one);
        String s2 = format(two);
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

}
