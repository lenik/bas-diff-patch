package name.fraser.neil.generic;

import net.bodz.bas.text.generic.LineIterator;
import net.bodz.bas.text.generic.LineLnText;
import net.bodz.bas.text.generic.Text;

public class LineLnDiffType
        extends LineDiffType {

    @Override
    public String format(Text<? extends String> text) {
        int n = text.length();
        StringBuilder sb = new StringBuilder(n * 100);
        for (int i = 0; i < n; i++) {
            String line = text.charAt(i);
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public LineLnText parse(String s) {
        LineLnText text = new LineLnText();
        LineIterator iterator = new LineIterator.Builder()//
                .text(s).preserveEOL(false).returnLastBlankLine(false).build();
        while (iterator.hasNext()) {
            String line = iterator.next();
            text.append(line);
        }
        return text;
    }

}
