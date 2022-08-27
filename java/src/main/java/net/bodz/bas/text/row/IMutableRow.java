package net.bodz.bas.text.row;

public interface IMutableRow<cell_t>
        extends
            IRow<cell_t> {

    void set(int index, cell_t cell);

    @Override
    IMutableRow<cell_t> slice(int begin);

    @Override
    IMutableRow<cell_t> slice(int begin, int end);

    @Override
    IMutableRow<cell_t> concat(cell_t o);

    @Override
    IMutableRow<cell_t> concat(IRow<? extends cell_t> o);

    void clear();

    IMutableRow<cell_t> append(cell_t o);

    IMutableRow<cell_t> append(IRow<? extends cell_t> o);

    void delete(int begin);

    void delete(int begin, int end);

    void preserve(int begin);

    void preserve(int begin, int end);

    IMutableRow<cell_t> splice(int begin);

    IMutableRow<cell_t> splice(int begin, int end);

}
