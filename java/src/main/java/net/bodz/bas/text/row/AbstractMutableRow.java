package net.bodz.bas.text.row;

public abstract class AbstractMutableRow<cell_t>
        extends AbstractRow<cell_t>
        implements
            IMutableRow<cell_t> {

    @Override
    public IMutableRow<cell_t> slice(int begin) {
        return (IMutableRow<cell_t>) super.slice(begin);
    }

    @Override
    public void clear() {
        delete(0, length());
    }

    @Override
    public void delete(int begin) {
        delete(begin, length());
    }

    @Override
    public void preserve(int begin) {
        preserve(begin, length());
    }

    @Override
    public IMutableRow<cell_t> splice(int begin) {
        return splice(begin, length());
    }

    @Override
    public IMutableRow<cell_t> append(IRow<? extends cell_t> row) {
        for (cell_t cell : row)
            append(cell);
        return this;
    }

    @Override
    public void insert(int index, IRow<? extends cell_t> row) {
        for (cell_t cell : row)
            insert(index++, cell);
    }

    @Override
    public IRow<cell_t> lock() {
        return copy();
    }

    @Override
    public IMutableRow<cell_t> unlock() {
        return this;
    }

}
