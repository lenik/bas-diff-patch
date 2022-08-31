package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRowType;

public abstract class AbstractRowComparator<cell_t>
        implements
            IRowComparator<cell_t>,
            IRowType<cell_t> {

    IRowType<cell_t> rowType = this;

    public AbstractRowComparator() {
        this.rowType = this;
    }

}
