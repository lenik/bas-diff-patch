package net.bodz.bas.text.generic;

import java.util.List;

public class LineText
        extends ListText<String> {

    public LineText() {
        super();
    }

    public LineText(List<String> list, boolean copyOnWrite) {
        super(list, copyOnWrite);
    }

    public LineText(List<String> list) {
        super(list);
    }

    public static class Builder
            extends MultiLineOptions<Builder> {

        public LineText build() {
            List<String> list = new TextLines(new LineIterator.Builder(this)).toList();
            return new LineText(list, false);
        }

    }

    public static Builder builder(String s) {
        return new Builder().text(s).preserveEOL(true);
    }

    @Override
    public String asString() {
        int n = length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            String ch = charAt(i);
            sb.append(ch.toString());
        }
        return sb.toString();
    }

}
