package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

public interface IRowDifference<cell_t> {

    DifferenceType getDifferenceType();

    IRow<cell_t> getRow();

    String getTextAsString();

}
