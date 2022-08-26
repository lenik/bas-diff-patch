package name.fraser.neil.generic;

import net.bodz.bas.text.generic.Text;

public class HalfMatch<char_t> {

    public Text<char_t> prefix1; // 0
    public Text<char_t> suffix1; // 1
    public Text<char_t> prefix2; // 2
    public Text<char_t> suffix2; // 3
    public Text<char_t> common; // 4

    public HalfMatch(Text<char_t> prefix1, Text<char_t> suffix1, Text<char_t> prefix2, Text<char_t> suffix2,
            Text<char_t> common) {
        this.prefix1 = prefix1;
        this.suffix1 = suffix1;
        this.prefix2 = prefix2;
        this.suffix2 = suffix2;
        this.common = common;
    }

}
