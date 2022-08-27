package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public class HalfMatch<char_t> {

    public IRow<char_t> prefix1; // 0
    public IRow<char_t> suffix1; // 1
    public IRow<char_t> prefix2; // 2
    public IRow<char_t> suffix2; // 3
    public IRow<char_t> common; // 4

    public HalfMatch(IRow<char_t> prefix1, IRow<char_t> suffix1, IRow<char_t> prefix2, IRow<char_t> suffix2,
            IRow<char_t> common) {
        this.prefix1 = prefix1;
        this.suffix1 = suffix1;
        this.prefix2 = prefix2;
        this.suffix2 = suffix2;
        this.common = common;
    }

}
