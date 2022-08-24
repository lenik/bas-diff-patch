package name.fraser.neil.generic;

public interface Text<char_t>
        extends
            Iterable<char_t> {

    int length();

    boolean isEmpty();

    char_t charAt(int index);

    Text<char_t> substring(int begin);

    Text<char_t> substring(int begin, int end);

    Text<char_t> concat(char_t o);

    Text<char_t> concat(Text<char_t> o);

    int indexOf(Text<char_t> pattern);

    int indexOf(Text<char_t> pattern, int from);

    boolean startsWith(Text<char_t> pattern);

    boolean endsWith(Text<char_t> pattern);

    String asString();

}
