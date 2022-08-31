package net.bodz.bas.compare.dmp;

public class IndexedRef<T> {

    public final int index;
    public final T target;

    public IndexedRef(int index, T target) {
        this.index = index;
        this.target = target;
    }

}
