package net.bodz.bas.text.row;

public class StringArray
        extends ArrayView<String> {

    public StringArray(String[] array) {
        super(array);
    }

    public StringArray(String[] array, int begin, int end) {
        super(array, begin, end);
    }

}
