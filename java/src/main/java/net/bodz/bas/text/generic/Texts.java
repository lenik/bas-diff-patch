package net.bodz.bas.text.generic;

public class Texts {

    static ArrayText<Object> EMPTY = new ArrayText<Object>(new Object[0]);

    @SuppressWarnings("unchecked")
    public static <char_t> Text<char_t> empty() {
        return (Text<char_t>) EMPTY;
    }

}
