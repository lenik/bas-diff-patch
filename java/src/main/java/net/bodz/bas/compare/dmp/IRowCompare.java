package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IRowCompare<cell_t> {

    <T extends cell_t> ChangeList<T> compare(IRow<T> text1, IRow<T> text2);

}
