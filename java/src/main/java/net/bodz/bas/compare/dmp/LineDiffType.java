package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.generic.LineIterator;
import net.bodz.bas.text.generic.LineLnText;
import net.bodz.bas.text.generic.Text;

public class LineDiffType
        implements
            ICharDiffType<String> {

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
    public String format(Text<? extends String> text) {
        int n = text.length();
        StringBuilder sb = new StringBuilder(n * 100);
        for (int i = 0; i < n; i++) {
            String line = text.charAt(i);
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public LineLnText parse(String s) {
        LineLnText text = new LineLnText();
        LineIterator iterator = new LineIterator.Builder()//
                .text(s).preserveEOL(true).returnLastBlankLine(false).build();
        while (iterator.hasNext()) {
            String line = iterator.next();
            text.append(line);
        }
        return text;
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
    public int cleanupSemanticScore(Text<? extends String> one, Text<? extends String> two) {
        String s1 = one.asString();
        String s2 = two.asString();
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

    public static void main(String[] args) {
        System.out.println("a\fb");
    }
}
