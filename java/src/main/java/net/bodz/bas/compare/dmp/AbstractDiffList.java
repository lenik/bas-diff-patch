package net.bodz.bas.compare.dmp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;

public abstract class AbstractDiffList<diff_t extends IRowDifference<cell_t>, cell_t>
        implements
            IDiffList<diff_t, cell_t> {

    Config config;
    DMPRowComparator<cell_t> dmp;

    protected LinkedList<diff_t> list = new LinkedList<diff_t>();

    public AbstractDiffList(DMPRowComparator<cell_t> dmp) {
        this.config = dmp.config;
        this.dmp = dmp;
    }

    @Override
    public Iterator<diff_t> iterator() {
        return list.iterator();
    }

    @Override
    public ListIterator<diff_t> listIterator() {
        return list.listIterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public diff_t getFirst() {
        return list.getFirst();
    }

    @Override
    public diff_t getLast() {
        return list.getLast();
    }

    @Override
    public diff_t removeFirst() {
        return list.removeFirst();
    }

    @Override
    public diff_t removeLast() {
        return list.removeLast();
    }

    @Override
    public void add(diff_t diff) {
        list.add(diff);
    }

    @Override
    public void addAll(Collection<? extends diff_t> diffs) {
        list.addAll(diffs);
    }

    @Override
    public void addAll(IDiffList<? extends diff_t, ? extends cell_t> diffs) {
        for (diff_t diff : diffs)
            list.add(diff);
    }

    /**
     * loc is a location in row1, compute and return the equivalent location in row2. e.g. "The
     * cat" vs "The big cat", 1->1, 5->8
     *
     * @param diffs
     *            List of Diff objects.
     * @param loc
     *            Location within row1.
     * @return Location within row2.
     */
    public final int xIndex(int loc) {
        int chars1 = 0;
        int chars2 = 0;
        int last_chars1 = 0;
        int last_chars2 = 0;
        IRowDifference<cell_t> lastDiff = null;
        for (IRowDifference<cell_t> aDiff : this) {
            DifferenceType type = aDiff.getDifferenceType();
            IRow<cell_t> row = aDiff.getRow();
            if (type != DifferenceType.INSERTION) {
                // Equality or deletion.
                chars1 += row.length();
            }
            if (type != DifferenceType.REMOVAL) {
                // Equality or insertion.
                chars2 += row.length();
            }
            if (chars1 > loc) {
                // Overshot the location.
                lastDiff = aDiff;
                break;
            }
            last_chars1 = chars1;
            last_chars2 = chars2;
        }
        if (lastDiff != null && lastDiff.getDifferenceType() == DifferenceType.REMOVAL) {
            // The location was deleted.
            return last_chars2;
        }
        // Add the remaining character length.
        return last_chars2 + (loc - last_chars1);
    }

    /**
     * Convert a Diff list into a pretty HTML report.
     *
     * @param diffs
     *            List of Diff objects.
     * @return HTML representation.
     */
    public final String prettyHtml() {
        StringBuilder html = new StringBuilder();
        for (IRowDifference<cell_t> aDiff : this) {
            String text = aDiff.getTextAsString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\n", "&para;<br>");
            switch (aDiff.getDifferenceType()) {
            case INSERTION:
                html.append("<ins style=\"background:#e6ffe6;\">").append(text).append("</ins>");
                break;
            case REMOVAL:
                html.append("<del style=\"background:#ffe6e6;\">").append(text).append("</del>");
                break;
            case MATCH:
                html.append("<span>").append(text).append("</span>");
                break;
            }
        }
        return html.toString();
    }

    /**
     * Compute and return the source text (all equalities and deletions).
     *
     * @param diffs
     *            List of Diff objects.
     * @return Source text.
     */
    public final IRow<cell_t> restoreRow1() {
        MutableRow<cell_t> concat = new MutableRow<cell_t>();
        for (IRowDifference<cell_t> aDiff : this) {
            if (aDiff.getDifferenceType() != DifferenceType.INSERTION) {
                concat.append(aDiff.getRow());
            }
        }
        return concat;
    }

    /**
     * Compute and return the destination text (all equalities and insertions).
     *
     * @param diffs
     *            List of Diff objects.
     * @return Destination text.
     */
    public final IRow<cell_t> restoreRow2() {
        MutableRow<cell_t> concat = new MutableRow<cell_t>();
        for (IRowDifference<cell_t> aDiff : this) {
            if (aDiff.getDifferenceType() != DifferenceType.REMOVAL) {
                concat.append(aDiff.getRow());
            }
        }
        return concat;
    }

    /**
     * Compute the Levenshtein distance; the number of inserted, deleted or substituted characters.
     *
     * @param diffs
     *            List of Diff objects.
     * @return Number of changes.
     */
    public final int levenshtein() {
        int levenshtein = 0;
        int insertions = 0;
        int deletions = 0;
        for (IRowDifference<cell_t> aDiff : this) {
            IRow<cell_t> row = aDiff.getRow();
            switch (aDiff.getDifferenceType()) {
            case INSERTION:
                insertions += row.length();
                break;
            case REMOVAL:
                deletions += row.length();
                break;
            case MATCH:
                // A deletion and an insertion is one substitution.
                levenshtein += Math.max(insertions, deletions);
                insertions = 0;
                deletions = 0;
                break;
            }
        }
        levenshtein += Math.max(insertions, deletions);
        return levenshtein;
    }

    /**
     * Crush the diff into an encoded string which describes the types required to transform
     * row1 into row2. E.g. =3\t-2\t+ing -> Keep 3 chars, delete 2 chars, insert 'ing'. Operations
     * are tab-separated. Inserted text is escaped using %xx notation.
     *
     * @param diffs
     *            List of Diff objects.
     * @return Delta text.
     */
    public final String toDelta() {
        StringBuilder sb = new StringBuilder();
        for (IRowDifference<cell_t> aDiff : this) {
            IRow<cell_t> row = aDiff.getRow();
            switch (aDiff.getDifferenceType()) {
            case INSERTION:
                sb.append("+").append(JsCompat.encodeUri(aDiff.getTextAsString())).append("\t");
                break;
            case REMOVAL:
                sb.append("-").append(row.length()).append("\t");
                break;
            case MATCH:
                sb.append("=").append(row.length()).append("\t");
                break;
            }
        }
        String delta = sb.toString();
        if (delta.length() != 0) {
            // Strip off trailing tab character.
            delta = delta.substring(0, delta.length() - 1);
        }
        return delta;
    }

    protected final diff_t createDifference(DifferenceType type, IRow<cell_t> row) {
        return createDifference(type, row, false);
    }

    protected abstract diff_t createDifference(DifferenceType type, IRow<cell_t> row, boolean allocated);

    /**
     * Given the original row1, and an encoded string which describes the types required to
     * transform row1 into row2, compute the full diff.
     *
     * @param row1
     *            Source string for the diff.
     * @param delta
     *            Delta text.
     * @throws IllegalArgumentException
     *             If invalid input.
     */
    public final void readDelta(IRow<cell_t> row1, String delta)
            throws IllegalArgumentException {

        int pointer = 0; // Cursor in row1
        String[] tokens = delta.split("\t");
        for (String token : tokens) {
            if (token.length() == 0) {
                // Blank tokens are ok (from a trailing \t).
                continue;
            }
            // Each token begins with a one character parameter which specifies the
            // type of this token (delete, insert, equality).
            String param = token.substring(1);
            switch (token.charAt(0)) {
            case '+':
                // decode would change all "+" to " "
                param = param.replace("+", "%2B");
                try {
                    param = URLDecoder.decode(param, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Not likely on modern system.
                    throw new Error("This system does not support UTF-8.", e);
                } catch (IllegalArgumentException e) {
                    // Malformed URI sequence.
                    throw new IllegalArgumentException("Illegal escape in diff_fromDelta: " + param, e);
                }
                IRow<cell_t> line = dmp.parse(param);
                list.add(createDifference(DifferenceType.INSERTION, line, true));
                break;
            case '-':
                // Fall through.
            case '=':
                int n;
                try {
                    n = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number in diff_fromDelta: " + param, e);
                }
                if (n < 0) {
                    throw new IllegalArgumentException("Negative number in diff_fromDelta: " + param);
                }
                IRow<cell_t> text;
                try {
                    text = row1.slice(pointer, pointer += n);
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(
                            "Delta length (" + pointer + ") larger than source text length (" + row1.length() + ").",
                            e);
                }
                if (token.charAt(0) == '=') {
                    list.add(createDifference(DifferenceType.MATCH, text));
                } else {
                    list.add(createDifference(DifferenceType.REMOVAL, text));
                }
                break;
            default:
                // Anything else is an error.
                throw new IllegalArgumentException("Invalid diff type in diff_fromDelta: " + token.charAt(0));
            }
        }
        if (pointer != row1.length()) {
            throw new IllegalArgumentException(
                    "Delta length (" + pointer + ") smaller than source text length (" + row1.length() + ").");
        }
    }

}
