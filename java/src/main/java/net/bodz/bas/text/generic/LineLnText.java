package net.bodz.bas.text.generic;

import java.util.List;

public class LineLnText
        extends ListText<String> {

    public LineLnText() {
        super();
    }

    public LineLnText(List<String> list) {
        super(list);
    }

    public LineLnText(List<String> list, boolean copy) {
        super(list, copy);
    }

    public static class Builder
            extends MultiLineOptions<Builder> {

        public LineText build() {
            List<String> list = new TextLines(new LineIterator.Builder(this)).toList();
            return new LineText(list, false);
        }

    }

    public static Builder builder(String s) {
        return new Builder().text(s).preserveEOL(false);
    }

    @Override
    public String asString() {
        int n = length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            String ch = charAt(i);
            sb.append(ch.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

}
