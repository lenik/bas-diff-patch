package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRowType;

public abstract class AbstractRowCompare<cell_t>
        implements
            IRowCompare<cell_t>,
            IRowType<cell_t> {

    IRowType<cell_t> rowType = this;

    public AbstractRowCompare() {
        this.rowType = this;
    }

}
