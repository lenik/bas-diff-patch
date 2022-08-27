package net.bodz.bas.compare.dmp.rowtype;

import net.bodz.bas.compare.dmp.DiffMatchPatch;
import net.bodz.bas.text.LinesText;
import net.bodz.bas.text.LinesText.Builder;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.StringRow;

public class LinesType
        implements
            IDmpRowType<StringRow, IRow<? extends String>, String> {

    boolean useUnicodeFormFeed = false;

    @Override
    public String separator() {
        if (useUnicodeFormFeed)
            return "\u21A1"; // form feed
        else
            // page break ^L, 0x0C
            // Often, it will also cause a carriage return.
            return "\f";
    }

    @Override
    public StringRow newRow() {
        return new StringRow();
    }

    protected <T> void configParser(LinesText.Builder options) {
        options.removeEOL(false);
        options.returnLastBlankLine(true);
    }

    @Override
    public StringRow parse(String s) {
        StringRow row = newRow();
        Builder builder = new LinesText.Builder();
        configParser(builder);
        for (String line : builder.build())
            row.append(line);
        return row;
    }

    @Override
    public String format(IRow<? extends String> text) {
        int n = text.length();
        StringBuilder sb = new StringBuilder(n * 100);
        for (int i = 0; i < n; i++) {
            String line = text.cellAt(i);
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public String createPadding() {
        return "#";
    }

    @Override
    public String createPadding(int hint) {
        return "#" + String.valueOf(hint);
    }

    @Override
    public int cleanupSemanticScore(IRow<? extends String> one, IRow<? extends String> two) {
        String s1 = format(one);
        String s2 = format(two);
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

}
