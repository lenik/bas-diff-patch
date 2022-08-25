package name.fraser.neil.generic;

import java.util.StringTokenizer;

public interface ICharType<char_t> {

    char_t createJunk();

    char_t createPadding();

    char_t createPadding(int hint);

    String format(Text<char_t> text);

    Text<char_t> parse(String s);

    CharArrayConv CHAR_ARRAY = new CharArrayConv();
    IntListConv INT_LIST = new IntListConv();

}

class CharArrayConv
        implements
            ICharType<Character> {

    @Override
    public Character createJunk() {
        return '?';
    }

    @Override
    public Character createPadding() {
        return '#';
    }

    @Override
    public Character createPadding(int hint) {
        return (char) hint;
    }

    @Override
    public String format(Text<Character> text) {
        return text.asString();
    }

    @Override
    public Text<Character> parse(String s) {
        return new CharText(s);
    }

}

class IntListConv
        implements
            ICharType<Integer> {

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
    public String format(Text<Integer> text) {
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

}
