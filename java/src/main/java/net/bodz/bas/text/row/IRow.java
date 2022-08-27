package net.bodz.bas.text.row;

import java.util.Iterator;

/**
 * char_t element in the object can't be null.
 */
public interface IRow<cell_t>
        extends
            Iterable<cell_t> {

    @Override
    Iterator<cell_t> iterator();

    int length();

    boolean isEmpty();

    cell_t cellAt(int index);

    IRow<cell_t> slice(int begin);

    IRow<cell_t> slice(int begin, int end);

    IRow<cell_t> concat(cell_t cell);

    IRow<cell_t> concat(IRow<? extends cell_t> row);

    int indexOf(Object pattern);

    int indexOf(Object pattern, int from);

    int lastIndexOf(Object pattern);

    int lastIndexOf(Object pattern, int from);

    int indexOf(IRow<? extends cell_t> pattern);

    int indexOf(IRow<? extends cell_t> pattern, int from);

    int lastIndexOf(IRow<? extends cell_t> pattern);

    int lastIndexOf(IRow<? extends cell_t> pattern, int from);

    boolean startsWith(IRow<? extends cell_t> pattern);

    boolean endsWith(IRow<? extends cell_t> pattern);

}
