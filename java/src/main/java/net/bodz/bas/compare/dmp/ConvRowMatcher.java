package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;

public class ConvRowMatcher<cell_t>
        implements
            IRowMatcher<cell_t> {

    RowMatcher<Integer> impl;

    public ConvRowMatcher(DMPConfig config) {
        impl = new RowMatcher<Integer>(config);
    }

    @Override
    public <T extends cell_t> int search(IRow<T> row, IRow<T> pattern, int loc) {
        AtomMap<cell_t> map = new AtomMap<cell_t>();
        MutableRow<Integer> atomRow = map.atomize(row);
        MutableRow<Integer> atomPattern = map.atomize(pattern);
        return impl.search(atomRow, atomPattern, loc);
    }

}
