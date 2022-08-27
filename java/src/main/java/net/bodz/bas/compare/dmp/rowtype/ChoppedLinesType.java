package net.bodz.bas.compare.dmp.rowtype;

import net.bodz.bas.text.LinesText.Builder;
import net.bodz.bas.text.row.IRow;

public class ChoppedLinesType
        extends LinesType {

    @Override
    protected <T> void configParser(Builder options) {
        options.removeEOL(true);
        options.returnLastBlankLine(false);
    }

    @Override
    public String format(IRow<? extends String> text) {
        int n = text.length();
        StringBuilder sb = new StringBuilder(n * 100);
        for (int i = 0; i < n; i++) {
            String line = text.cellAt(i);
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

}
