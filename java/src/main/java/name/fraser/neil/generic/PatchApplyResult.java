package name.fraser.neil.generic;

import net.bodz.bas.text.generic.Text;

public class PatchApplyResult<char_t> {

    public Text<char_t> text;
    public boolean[] results;

    public PatchApplyResult(Text<char_t> text, boolean... results) {
        this.text = text;
        this.results = results;
    }

}
