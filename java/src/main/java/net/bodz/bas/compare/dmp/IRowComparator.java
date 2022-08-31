package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IRowComparator<cell_t> {

    <C extends cell_t> IDiffList<? extends IRowDifference<cell_t>, cell_t> compare(IRow<C> row1, IRow<C> row2);

    <T extends cell_t> EditList<cell_t> precompare(IRow<T> row1, IRow<T> row2);

}
