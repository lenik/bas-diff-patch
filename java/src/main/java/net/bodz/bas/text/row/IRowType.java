package net.bodz.bas.text.row;

public interface IRowType<cell_t> {

    IRow<cell_t> parse(String s);

    String format(IRow<? extends cell_t> row);

}
