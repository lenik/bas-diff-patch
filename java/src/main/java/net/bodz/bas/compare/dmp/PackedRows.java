package net.bodz.bas.compare.dmp;

import java.util.ArrayList;
import java.util.List;

import net.bodz.bas.text.row.IMutableRow;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;
import net.bodz.bas.text.row.MutableRow;

public class PackedRows<cell_t>
        extends ArrayList<IntegerRow> {

    private static final long serialVersionUID = 1L;

    public final List<IRow<cell_t>> packArray;

    public PackedRows(List<IRow<cell_t>> packArray, IntegerRow indexRow1, IntegerRow indexRow2) {
        super(2);
        this.packArray = packArray;
        add(indexRow1);
        add(indexRow2);
    }

    public IntegerRow getIndexRow1() {
        return get(0);
    }

    public IntegerRow getIndexRow2() {
        return get(1);
    }

    public EditList<cell_t> unpack(DMPRowComparator<cell_t> dmp, EditList<Integer> diffs) {
        EditList<cell_t> a = new EditList<cell_t>(dmp);
        for (RowEdit<Integer> diff : diffs) {
            IMutableRow<Integer> delta = diff.delta;
            MutableRow<cell_t> aDelta = new MutableRow<cell_t>(delta.length());
            for (Integer cell : delta) {
                IRow<cell_t> aCell = packArray.get(cell);
                aDelta.append(aCell);
            }
            RowEdit<cell_t> aDiff = new RowEdit<cell_t>(diff.getDifferenceType(), aDelta);
            a.add(aDiff);
        }
        return a;
    }

    public PatchList<cell_t> unpack(DMPRowComparator<cell_t> dmp, PatchList<Integer> patchList) {
        PatchList<cell_t> a = new PatchList<cell_t>(dmp);
        for (Patch<Integer> patch : patchList) {
            EditList<cell_t> aDiffs = unpack(dmp, patch.diffs);
            Patch<cell_t> aPatch = new Patch<cell_t>(dmp);
            aPatch.diffs = aDiffs;
            aPatch.start1 = patch.start1;
            aPatch.start2 = patch.start2;
            aPatch.length1 = patch.length1;
            aPatch.length2 = patch.length2;
            a.add(aPatch);
        }
        return a;
    }

}
