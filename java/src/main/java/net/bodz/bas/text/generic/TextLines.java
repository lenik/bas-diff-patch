package net.bodz.bas.text.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextLines
        implements
            Iterable<String> {

    final LineIterator.Builder iteratorBuilder;

    TextLines(LineIterator.Builder iteratorBuilder) {
        this.iteratorBuilder = iteratorBuilder;
    }

    public static class Builder
            extends MultiLineOptions<Builder> {

        public TextLines build() {
            return new TextLines(new LineIterator.Builder(this));
        }

    }

    @Override
    public Iterator<String> iterator() {
        return iteratorBuilder.build();
    }

    public List<String> toList() {
        List<String> list = new ArrayList<String>();
        for (String s : this)
            list.add(s);
        return list;
    }

    public static Builder builder(String s) {
        return new Builder().text(s);
    }

}
