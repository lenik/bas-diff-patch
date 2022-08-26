package name.fraser.neil.generic;

import net.bodz.bas.text.generic.Text;

public interface ICharType<char_t> {

    String format(Text<? extends char_t> text);

    Text<char_t> parse(String s);

}
