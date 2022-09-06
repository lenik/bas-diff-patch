package net.bodz.bas.compare.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bodz.bas.text.row.IMutableRow;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;

public class AtomMap<T> {

    List<T> array = new ArrayList<T>();
    Map<T, Integer> map = new HashMap<T, Integer>();
    int nextIndex = 0;

    public AtomMap() {
        array.add(null);
        nextIndex++;
    }

    public synchronized int atomize(T obj) {
        Integer atom = map.get(obj);
        if (atom == null) {
            atom = nextIndex++;
            array.add(obj);
            map.put(obj, atom);
        }
        return atom;
    }

    public T restore(int atom) {
        int index = atom;
        if (index <= 0 || index >= array.size())
            throw new IllegalArgumentException("bad atom: " + atom);
        T value = array.get(index);
        return value;
    }

    public MutableRow<Integer> atomize(IRow<? extends T> row) {
        MutableRow<Integer> atoms = new MutableRow<Integer>(row.length());
        for (T cell : row) {
            int atom = atomize(cell);
            atoms.append(atom);
        }
        return atoms;
    }

    public EditList<T> restore(DMPRowComparator<T> dmp, EditList<Integer> aDiffs) {
        EditList<T> diffs = new EditList<T>(dmp);
        for (RowEdit<Integer> aDiff : aDiffs) {
            IMutableRow<Integer> aDelta = aDiff.delta;
            MutableRow<T> delta = new MutableRow<T>(aDelta.length());
            for (Integer atom : aDelta) {
                T cell = array.get(atom);
                delta.append(cell);
            }
            RowEdit<T> diff = new RowEdit<T>(aDiff.getDifferenceType(), delta);
            diffs.add(diff);
        }
        return diffs;
    }

    public PatchList<T> restore(DMPRowComparator<T> dmp, PatchList<Integer> aPatchList) {
        PatchList<T> patchList = new PatchList<T>(dmp);
        for (Patch<Integer> aPatch : aPatchList) {
            EditList<T> _Diffs = restore(dmp, aPatch.diffs);
            Patch<T> patch = new Patch<T>(dmp);
            patch.diffs = _Diffs;
            patch.start1 = aPatch.start1;
            patch.start2 = aPatch.start2;
            patch.length1 = aPatch.length1;
            patch.length2 = aPatch.length2;
            patchList.add(patch);
        }
        return patchList;
    }

}
