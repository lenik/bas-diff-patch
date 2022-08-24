package name.fraser.neil.generic;

public class TextFn {

    public static Text<Character> toText(String s) {
        int n = s.length();
        Character[] v = new Character[n];
        for (int i = 0; i < n; i++)
            v[i] = s.charAt(i);
        return new ArrayText<Character>(v);
    }

    static ArrayText<Object> EMPTY = new ArrayText<Object>(new Object[0]);

    @SuppressWarnings("unchecked")
    public static <char_t> Text<char_t> empty() {
        return (Text<char_t>) EMPTY;
    }

}
