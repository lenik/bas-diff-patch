package name.fraser.neil.generic;

import java.util.StringTokenizer;

public class IntDiffType
        implements
            ICharDiffType<Integer> {

    @Override
    public String format(Text<? extends Integer> text) {
        StringBuilder sb = new StringBuilder(text.length() * 8);
        int n = text.length();
        for (int i = 0; i < n; i++) {
            if (i != 0)
                sb.append(',');
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }

    @Override
    public Text<Integer> parse(String s) {
        ListText<Integer> buf = new ListText<Integer>();
        StringTokenizer tokens = new StringTokenizer(",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            int val = Integer.parseInt(token);
            buf.append(val);
        }
        return buf;
    }

    @Override
    public Integer createJunk() {
        return -1;
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
    public int cleanupSemanticScore(Text<? extends Integer> one, Text<? extends Integer> two) {
        String s1 = format(one);
        String s2 = format(two);
        return DiffMatchPatch.diff_cleanupSemanticScore(s1, s2);
    }

}
