package name.fraser.neil.generic;

public class DMP_Char
        extends DiffMatchPatch<Character> {

    public DMP_Char() {
        super(ICharType.CHAR_ARRAY);
    }

    public static final DMP_Char INSTANCE = new DMP_Char();

}
