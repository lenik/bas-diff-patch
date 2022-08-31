package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.LinesText;
import net.bodz.bas.text.LinesText.Builder;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.StringRow;

public class LinesComparator
        extends DMPRowComparator<String> {

    boolean useUnicodeFormFeed = false;

    public LinesComparator(Config config) {
        super(config);
    }

    protected <T> void configParser(LinesText.Builder options) {
        options.removeEOL(false);
        options.returnLastBlankLine(true);
    }

    @Override
    public StringRow parse(String s) {
        StringRow row = new StringRow();
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
    protected String separator() {
        if (useUnicodeFormFeed)
            return "\u21A1"; // form feed
        else
            // page break ^L, 0x0C
            // Often, it will also cause a carriage return.
            return "\f";
    }

    @Override
    protected String createPadding() {
        return "#";
    }

    @Override
    protected String createPadding(int hint) {
        return "#" + String.valueOf(hint);
    }

    @Override
    public int cleanupSemanticScore(IRow<? extends String> one, IRow<? extends String> two) {
        String s1 = format(one);
        String s2 = format(two);
        return StringStats.diff_cleanupSemanticScore(s1, s2);
    }

}
