package name.fraser.neil.generic;

public interface ICleanupSemanticScore<char_t> {

    int compute(Text<? extends char_t> one, Text<? extends char_t> two);

}
