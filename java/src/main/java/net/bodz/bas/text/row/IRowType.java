package net.bodz.bas.text.row;

public interface IRowType<mutable_t extends IRow<?>, view_t extends IRow<?>> {

    mutable_t newRow();

    mutable_t parse(String s);

    String format(view_t row);

}
