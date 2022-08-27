package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IRowCompare<cell_t> {

    <T extends cell_t> ChangeList<cell_t> compare(IRow<T> text1, IRow<T> text2);

}
