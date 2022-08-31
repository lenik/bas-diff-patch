package net.bodz.bas.compare.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;

public class PackBuilder<cell_t>
        implements
            IPackBuilder<cell_t> {

    List<IRow<cell_t>> packArray;
    Map<IRow<cell_t>, Integer> packHash;
    List<IntegerRow> indexRowList;

    public PackBuilder(int capacityInRows) {
        packArray = new ArrayList<IRow<cell_t>>(capacityInRows + 1);
        packHash = new HashMap<IRow<cell_t>, Integer>(capacityInRows);
        indexRowList = new ArrayList<IntegerRow>(2);
    }

    @Override
    public int pack(IRow<cell_t> row) {
        Integer index = packHash.get(row);
        if (index != null)
            return index.intValue();

        int newIndex = packArray.size();
        packArray.add(row);
        packHash.put(row, index);
        return newIndex;
    }

    @Override
    public IRow<cell_t> unpack(int index) {
        return packArray.get(index);
    }

    @Override
    public int addIndexRow(IntegerRow indexRow) {
        int indexIndex = indexRowList.size();
        indexRowList.add(indexRow);
        return indexIndex;
    }

    @Override
    public IntegerRow getIndexRow(int indexIndex) {
        return indexRowList.get(indexIndex);
    }

    @Override
    public void setIndexRow(int indexIndex, IntegerRow indexRow) {
        indexRowList.set(indexIndex, indexRow);
    }

    public PackedRows<cell_t> build() {
        return new PackedRows<cell_t>(packArray, getIndexRow(0), getIndexRow(1));
    }

}
