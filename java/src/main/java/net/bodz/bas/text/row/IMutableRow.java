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
    IMutableRow<cell_t> concat(cell_t cell);

    @Override
    IMutableRow<cell_t> concat(IRow<? extends cell_t> row);

    void clear();

    IMutableRow<cell_t> append(cell_t cell);

    IMutableRow<cell_t> append(IRow<? extends cell_t> row);

    void insert(int index, cell_t cell);

    void insert(int index, IRow<? extends cell_t> row);

    void replace(int begin, int end, IRow<? extends cell_t> row);

    void delete(int begin);

    void delete(int begin, int end);

    void preserve(int begin);

    void preserve(int begin, int end);

    IMutableRow<cell_t> splice(int begin);

    IMutableRow<cell_t> splice(int begin, int end);

}
