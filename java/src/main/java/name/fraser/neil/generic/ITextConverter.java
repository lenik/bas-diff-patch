package name.fraser.neil.generic;

public interface ITextConverter<char_t> {

    String format(Text<char_t> text);

    Text<char_t> parse(String s);

}
