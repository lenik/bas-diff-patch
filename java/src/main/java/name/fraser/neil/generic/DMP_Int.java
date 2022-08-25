package name.fraser.neil.generic;

public class DMP_Int
        extends DiffMatchPatch<Integer> {

    public DMP_Int() {
        super(ICharType.INT_LIST);
    }

    public static final DMP_Int INSTANCE = new DMP_Int();

}
