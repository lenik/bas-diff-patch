package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface ICleanupSemanticScore<cell_t> {

    int cleanupSemanticScore(IRow<? extends cell_t> one, IRow<? extends cell_t> two);

}
