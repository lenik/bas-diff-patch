package net.bodz.bas.text.generic;

public class IndexList
        extends ListText<Integer> {

    boolean compatMode = true;

    @Override
    public String asString() {
        if (compatMode) {
            int n = length();
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                int ch = charAt(i);
                sb.append((char) ch);
            }
            return sb.toString();
        } else
            return super.asString();
    }

}
