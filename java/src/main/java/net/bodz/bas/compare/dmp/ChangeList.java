package net.bodz.bas.compare.dmp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.ListIterator;

import net.bodz.bas.text.Nullables;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;
import net.bodz.bas.text.row.Rows;

public class ChangeList<cell_t>
        extends LinkedList<RowChangement<cell_t>> {

    private static final long serialVersionUID = 1L;

    Config config;
    DMPDiff<cell_t> diff;

    public ChangeList(DMPDiff<cell_t> diff) {
        this.config = diff.config;
        this.diff = diff;
    }

    @SuppressWarnings("unchecked")
    public void addConst(RowChangement<? extends cell_t> element) {
        super.add((RowChangement<cell_t>) element);
    }

    @SuppressWarnings("unchecked")
    public void addFirstConst(RowChangement<? extends cell_t> e) {
        super.addFirst((RowChangement<cell_t>) e);
    }

    @SuppressWarnings("unchecked")
    public void addLastConst(RowChangement<? extends cell_t> e) {
        super.addLast((RowChangement<cell_t>) e);
    }

    /**
     * Reduce the number of edits by eliminating semantically trivial equalities.
     *
     * @param diffs
     *            LinkedList of Diff objects.
     */
    public void cleanupSemantic() {
        if (isEmpty()) {
            return;
        }
        boolean changes = false;

        // Double-ended queue of qualities.
        Deque<RowChangement<cell_t>> equalities = new ArrayDeque<RowChangement<cell_t>>();
        IRow<cell_t> lastEquality = null; // Always equal to equalities.peek().text
        ListIterator<RowChangement<cell_t>> pointer = listIterator();

        // Number of characters that changed prior to the equality.
        int length_insertions1 = 0;
        int length_deletions1 = 0;

        // Number of characters that changed after the equality.
        int length_insertions2 = 0;
        int length_deletions2 = 0;
        RowChangement<cell_t> thisDiff = pointer.next();
        while (thisDiff != null) {
            if (thisDiff.operation == Operation.EQUAL) {
                // Equality found.
                equalities.push(thisDiff);
                length_insertions1 = length_insertions2;
                length_deletions1 = length_deletions2;
                length_insertions2 = 0;
                length_deletions2 = 0;
                lastEquality = thisDiff.row;
            } else {
                // An insertion or deletion.
                if (thisDiff.operation == Operation.INSERT) {
                    length_insertions2 += thisDiff.row.length();
                } else {
                    length_deletions2 += thisDiff.row.length();
                }
                // Eliminate an equality that is smaller or equal to the edits on both sides of it.
                if (lastEquality != null && (lastEquality.length() <= Math.max(length_insertions1, length_deletions1))
                        && (lastEquality.length() <= Math.max(length_insertions2, length_deletions2))) {
                    // System.out.println("Splitting: '" + lastEquality + "'");
                    // Walk back to offending equality.
                    while (thisDiff != equalities.peek()) {
                        thisDiff = pointer.previous();
                    }
                    pointer.next();

                    // Replace equality with a delete.
                    pointer.set(new RowChangement<cell_t>(Operation.DELETE, lastEquality));
                    // Insert a corresponding an insert.
                    pointer.add(new RowChangement<cell_t>(Operation.INSERT, lastEquality));

                    equalities.pop(); // Throw away the equality we just deleted.
                    if (!equalities.isEmpty()) {
                        // Throw away the previous equality (it needs to be reevaluated).
                        equalities.pop();
                    }
                    if (equalities.isEmpty()) {
                        // There are no previous equalities, walk back to the start.
                        while (pointer.hasPrevious()) {
                            pointer.previous();
                        }
                    } else {
                        // There is a safe equality we can fall back to.
                        thisDiff = equalities.peek();
                        while (thisDiff != pointer.previous()) {
                            // Intentionally empty loop.
                        }
                    }

                    length_insertions1 = 0; // Reset the counters.
                    length_insertions2 = 0;
                    length_deletions1 = 0;
                    length_deletions2 = 0;
                    lastEquality = null;
                    changes = true;
                }
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }

        // Normalize the diff.
        if (changes) {
            cleanupMerge();
        }
        cleanupSemanticLossless();

        // Find any overlaps between deletions and insertions.
        // e.g: <del>abcxxx</del><ins>xxxdef</ins>
        // -> <del>abc</del>xxx<ins>def</ins>
        // e.g: <del>xxxabc</del><ins>defxxx</ins>
        // -> <ins>def</ins>xxx<del>abc</del>
        // Only extract an overlap if it is as big as the edit ahead or behind it.
        pointer = listIterator();
        RowChangement<cell_t> prevDiff = null;
        thisDiff = null;
        if (pointer.hasNext()) {
            prevDiff = pointer.next();
            if (pointer.hasNext()) {
                thisDiff = pointer.next();
            }
        }
        while (thisDiff != null) {
            if (prevDiff.operation == Operation.DELETE && thisDiff.operation == Operation.INSERT) {
                IRow<cell_t> deletion = prevDiff.row;
                IRow<cell_t> insertion = thisDiff.row;
                int overlap_length1 = RowUtils.commonOverlap(deletion, insertion);
                int overlap_length2 = RowUtils.commonOverlap(insertion, deletion);
                if (overlap_length1 >= overlap_length2) {
                    if (overlap_length1 >= deletion.length() / 2.0 || overlap_length1 >= insertion.length() / 2.0) {
                        // Overlap found. Insert an equality and trim the surrounding edits.
                        pointer.previous();
                        pointer.add(new RowChangement<cell_t>(Operation.EQUAL, insertion.slice(0, overlap_length1)));
                        prevDiff.row = deletion.slice(0, deletion.length() - overlap_length1);
                        thisDiff.row = insertion.slice(overlap_length1, insertion.length());
                        // pointer.add inserts the element before the cursor, so there is
                        // no need to step past the new element.
                    }
                } else {
                    if (overlap_length2 >= deletion.length() / 2.0 || overlap_length2 >= insertion.length() / 2.0) {
                        // Reverse overlap found.
                        // Insert an equality and swap and trim the surrounding edits.
                        pointer.previous();
                        pointer.add(new RowChangement<cell_t>(Operation.EQUAL, deletion.slice(0, overlap_length2)));
                        prevDiff.operation = Operation.INSERT;
                        prevDiff.row = insertion.slice(0, insertion.length() - overlap_length2);
                        thisDiff.operation = Operation.DELETE;
                        thisDiff.row = deletion.slice(overlap_length2, deletion.length());
                        // pointer.add inserts the element before the cursor, so there is
                        // no need to step past the new element.
                    }
                }
                thisDiff = pointer.hasNext() ? pointer.next() : null;
            }
            prevDiff = thisDiff;
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
    }

    /**
     * Look for single edits surrounded on both sides by equalities which can be shifted sideways to
     * align the edit to a word boundary. e.g: The c<ins>at c</ins>ame. -> The <ins>cat </ins>came.
     *
     * @param diffs
     *            LinkedList of Diff objects.
     */
    public void cleanupSemanticLossless() {
        IRow<cell_t> equality1, edit, equality2;
        IRow<cell_t> commonString;
        int commonOffset;
        int score, bestScore;
        IRow<cell_t> bestEquality1, bestEdit, bestEquality2;
        // Create a new iterator at the start.
        ListIterator<RowChangement<cell_t>> pointer = this.listIterator();
        RowChangement<cell_t> prevDiff = pointer.hasNext() ? pointer.next() : null;
        RowChangement<cell_t> thisDiff = pointer.hasNext() ? pointer.next() : null;
        RowChangement<cell_t> nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL && nextDiff.operation == Operation.EQUAL) {
                // This is a single edit surrounded by equalities.
                equality1 = prevDiff.row;
                edit = thisDiff.row;
                equality2 = nextDiff.row;

                // First, shift the edit as far left as possible.
                commonOffset = RowUtils.commonSuffix(equality1, edit);
                if (commonOffset != 0) {
                    commonString = edit.slice(edit.length() - commonOffset, edit.length());
                    equality1 = equality1.slice(0, equality1.length() - commonOffset);
                    edit = commonString.concat(edit.slice(0, edit.length() - commonOffset));
                    equality2 = commonString.concat(equality2);
                }

                // Second, step character by character right, looking for the best fit.
                bestEquality1 = equality1;
                bestEdit = edit;
                bestEquality2 = equality2;
                bestScore = diff.cleanupSemanticScore(equality1, edit) + diff.cleanupSemanticScore(edit, equality2);
                while (edit.length() != 0 && equality2.length() != 0
                        && Nullables.equals(edit.cellAt(0), equality2.cellAt(0))) {
                    equality1 = equality1.concat(edit.cellAt(0));
                    edit = edit.slice(1).concat(equality2.cellAt(0));
                    equality2 = equality2.slice(1);
                    score = diff.cleanupSemanticScore(equality1, edit) + diff.cleanupSemanticScore(edit, equality2);
                    // The >= encourages trailing rather than leading whitespace on edits.
                    if (score >= bestScore) {
                        bestScore = score;
                        bestEquality1 = equality1;
                        bestEdit = edit;
                        bestEquality2 = equality2;
                    }
                }

                if (!prevDiff.row.equals(bestEquality1)) {
                    // We have an improvement, save it back to the diff.
                    if (bestEquality1.length() != 0) {
                        prevDiff.row = bestEquality1;
                    } else {
                        pointer.previous(); // Walk past nextDiff.
                        pointer.previous(); // Walk past thisDiff.
                        pointer.previous(); // Walk past prevDiff.
                        pointer.remove(); // Delete prevDiff.
                        pointer.next(); // Walk past thisDiff.
                        pointer.next(); // Walk past nextDiff.
                    }
                    thisDiff.row = bestEdit;
                    if (bestEquality2.length() != 0) {
                        nextDiff.row = bestEquality2;
                    } else {
                        pointer.remove(); // Delete nextDiff.
                        nextDiff = thisDiff;
                        thisDiff = prevDiff;
                    }
                }
            }
            prevDiff = thisDiff;
            thisDiff = nextDiff;
            nextDiff = pointer.hasNext() ? pointer.next() : null;
        }
    }

    /**
     * Reduce the number of edits by eliminating operationally trivial equalities.
     *
     * @param diffs
     *            LinkedList of Diff objects.
     */
    public void cleanupEfficiency() {
        if (isEmpty())
            return;

        boolean changes = false;
        // queue of equalities.
        Deque<RowChangement<cell_t>> equalities = new ArrayDeque<RowChangement<cell_t>>(); // Double-ended
        IRow<cell_t> lastEquality = null; // Always equal to equalities.peek().text
        ListIterator<RowChangement<cell_t>> pointer = listIterator();
        // Is there an insertion operation before the last equality.
        boolean pre_ins = false;
        // Is there a deletion operation before the last equality.
        boolean pre_del = false;
        // Is there an insertion operation after the last equality.
        boolean post_ins = false;
        // Is there a deletion operation after the last equality.
        boolean post_del = false;
        RowChangement<cell_t> thisDiff = pointer.next();
        RowChangement<cell_t> safeDiff = thisDiff; // The last Diff that is known to be
                                                   // unsplittable.
        while (thisDiff != null) {
            if (thisDiff.operation == Operation.EQUAL) {
                // Equality found.
                if (thisDiff.row.length() < config.Diff_EditCost && (post_ins || post_del)) {
                    // Candidate found.
                    equalities.push(thisDiff);
                    pre_ins = post_ins;
                    pre_del = post_del;
                    lastEquality = thisDiff.row;
                } else {
                    // Not a candidate, and can never become one.
                    equalities.clear();
                    lastEquality = null;
                    safeDiff = thisDiff;
                }
                post_ins = post_del = false;
            } else {
                // An insertion or deletion.
                if (thisDiff.operation == Operation.DELETE) {
                    post_del = true;
                } else {
                    post_ins = true;
                }
                /*
                 * Five types to be split: <ins>A</ins><del>B</del>XY<ins>C</ins><del>D</del>
                 * <ins>A</ins>X<ins>C</ins><del>D</del> <ins>A</ins><del>B</del>X<ins>C</ins>
                 * <ins>A</del>X<ins>C</ins><del>D</del> <ins>A</ins><del>B</del>X<del>C</del>
                 */
                if (lastEquality != null && ((pre_ins && pre_del && post_ins && post_del)
                        || ((lastEquality.length() < config.Diff_EditCost / 2) && ((pre_ins ? 1 : 0) + (pre_del ? 1 : 0)
                                + (post_ins ? 1 : 0) + (post_del ? 1 : 0)) == 3))) {
                    // System.out.println("Splitting: '" + lastEquality + "'");
                    // Walk back to offending equality.
                    while (thisDiff != equalities.peek()) {
                        thisDiff = pointer.previous();
                    }
                    pointer.next();

                    // Replace equality with a delete.
                    pointer.set(new RowChangement<cell_t>(Operation.DELETE, lastEquality));
                    // Insert a corresponding an insert.
                    pointer.add(thisDiff = new RowChangement<cell_t>(Operation.INSERT, lastEquality));

                    equalities.pop(); // Throw away the equality we just deleted.
                    lastEquality = null;
                    if (pre_ins && pre_del) {
                        // No changes made which could affect previous entry, keep going.
                        post_ins = post_del = true;
                        equalities.clear();
                        safeDiff = thisDiff;
                    } else {
                        if (!equalities.isEmpty()) {
                            // Throw away the previous equality (it needs to be reevaluated).
                            equalities.pop();
                        }
                        if (equalities.isEmpty()) {
                            // There are no previous questionable equalities,
                            // walk back to the last known safe diff.
                            thisDiff = safeDiff;
                        } else {
                            // There is an equality we can fall back to.
                            thisDiff = equalities.peek();
                        }
                        while (thisDiff != pointer.previous()) {
                            // Intentionally empty loop.
                        }
                        post_ins = post_del = false;
                    }

                    changes = true;
                }
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }

        if (changes) {
            cleanupMerge();
        }
    }

    /**
     * Reorder and merge like edit sections. Merge equalities. Any edit section can move as long as
     * it doesn't cross an equality.
     *
     * @param diffs
     *            LinkedList of Diff objects.
     */
    public void cleanupMerge() {
        // Add a dummy entry at the end.
        this.add(new RowChangement<cell_t>( //
                Operation.EQUAL, Rows.<cell_t> empty()));

        ListIterator<RowChangement<cell_t>> pointer = this.listIterator();
        int count_delete = 0;
        int count_insert = 0;
        MutableRow<cell_t> text_delete = new MutableRow<cell_t>();
        MutableRow<cell_t> text_insert = new MutableRow<cell_t>();
        RowChangement<cell_t> thisDiff = pointer.next();
        RowChangement<cell_t> prevEqual = null;
        int commonlength;
        while (thisDiff != null) {
            switch (thisDiff.operation) {
            case INSERT:
                count_insert++;
                text_insert.append(thisDiff.row);
                prevEqual = null;
                break;
            case DELETE:
                count_delete++;
                text_delete.append(thisDiff.row);
                prevEqual = null;
                break;
            case EQUAL:
                if (count_delete + count_insert > 1) {
                    boolean both_types = count_delete != 0 && count_insert != 0;
                    // Delete the offending records.
                    pointer.previous(); // Reverse direction.
                    while (count_delete-- > 0) {
                        pointer.previous();
                        pointer.remove();
                    }
                    while (count_insert-- > 0) {
                        pointer.previous();
                        pointer.remove();
                    }
                    if (both_types) {
                        // Factor out any common prefixies.
                        commonlength = RowUtils.commonPrefix(text_insert, text_delete);
                        if (commonlength != 0) {
                            if (pointer.hasPrevious()) {
                                thisDiff = pointer.previous();
                                assert thisDiff.operation == Operation.EQUAL : "Previous diff should have been an equality.";
                                thisDiff.row = thisDiff.row.concat(text_insert.slice(0, commonlength));
                                pointer.next();
                            } else {
                                pointer.add(
                                        new RowChangement<cell_t>(Operation.EQUAL, text_insert.slice(0, commonlength)));
                            }
                            text_insert.preserve(commonlength);
                            text_delete.preserve(commonlength);
                        }
                        // Factor out any common suffixies.
                        commonlength = RowUtils.commonSuffix(text_insert, text_delete);
                        if (commonlength != 0) {
                            thisDiff = pointer.next();
                            thisDiff.row = text_insert.slice(text_insert.length() - commonlength)
                                    .concat(thisDiff.row);
                            text_insert.delete(text_insert.length() - commonlength);
                            text_delete.delete(text_delete.length() - commonlength);
                            pointer.previous();
                        }
                    }
                    // Insert the merged records.
                    if (text_delete.length() != 0) {
                        pointer.add(new RowChangement<cell_t>(Operation.DELETE, text_delete.copy()));
                    }
                    if (text_insert.length() != 0) {
                        pointer.add(new RowChangement<cell_t>(Operation.INSERT, text_insert.copy()));
                    }
                    // Step forward to the equality.
                    thisDiff = pointer.hasNext() ? pointer.next() : null;
                } else if (prevEqual != null) {
                    // Merge this equality with the previous one.
                    prevEqual.row = prevEqual.row.concat(thisDiff.row);
                    pointer.remove();
                    thisDiff = pointer.previous();
                    pointer.next(); // Forward direction
                }
                count_insert = 0;
                count_delete = 0;
                text_delete.clear();
                text_insert.clear();
                prevEqual = thisDiff;
                break;
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
        if (this.getLast().row.length() == 0) {
            this.removeLast(); // Remove the dummy entry at the end.
        }

        /*
         * Second pass: look for single edits surrounded on both sides by equalities which can be
         * shifted sideways to eliminate an equality. e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
         */
        boolean changes = false;
        // Create a new iterator at the start.
        // (As opposed to walking the current one back.)
        pointer = this.listIterator();
        RowChangement<cell_t> prevDiff = pointer.hasNext() ? pointer.next() : null;
        thisDiff = pointer.hasNext() ? pointer.next() : null;
        RowChangement<cell_t> nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL && nextDiff.operation == Operation.EQUAL) {
                // This is a single edit surrounded by equalities.
                if (thisDiff.row.endsWith(prevDiff.row)) {
                    // Shift the edit over the previous equality.
                    thisDiff.row = prevDiff.row
                            .concat(thisDiff.row.slice(0, thisDiff.row.length() - prevDiff.row.length()));
                    nextDiff.row = prevDiff.row.concat(nextDiff.row);
                    pointer.previous(); // Walk past nextDiff.
                    pointer.previous(); // Walk past thisDiff.
                    pointer.previous(); // Walk past prevDiff.
                    pointer.remove(); // Delete prevDiff.
                    pointer.next(); // Walk past thisDiff.
                    thisDiff = pointer.next(); // Walk past nextDiff.
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                } else if (thisDiff.row.startsWith(nextDiff.row)) {
                    // Shift the edit over the next equality.
                    prevDiff.row = prevDiff.row.concat(nextDiff.row);
                    thisDiff.row = thisDiff.row.slice(nextDiff.row.length()).concat(nextDiff.row);
                    pointer.remove(); // Delete nextDiff.
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                }
            }
            prevDiff = thisDiff;
            thisDiff = nextDiff;
            nextDiff = pointer.hasNext() ? pointer.next() : null;
        }
        // If shifts were made, the diff needs reordering and another shift sweep.
        if (changes) {
            cleanupMerge();
        }
    }

    /**
     * loc is a location in text1, compute and return the equivalent location in text2. e.g. "The
     * cat" vs "The big cat", 1->1, 5->8
     *
     * @param diffs
     *            List of Diff objects.
     * @param loc
     *            Location within text1.
     * @return Location within text2.
     */
    public int xIndex(int loc) {
        int chars1 = 0;
        int chars2 = 0;
        int last_chars1 = 0;
        int last_chars2 = 0;
        RowChangement<cell_t> lastDiff = null;
        for (RowChangement<cell_t> aDiff : this) {
            if (aDiff.operation != Operation.INSERT) {
                // Equality or deletion.
                chars1 += aDiff.row.length();
            }
            if (aDiff.operation != Operation.DELETE) {
                // Equality or insertion.
                chars2 += aDiff.row.length();
            }
            if (chars1 > loc) {
                // Overshot the location.
                lastDiff = aDiff;
                break;
            }
            last_chars1 = chars1;
            last_chars2 = chars2;
        }
        if (lastDiff != null && lastDiff.operation == Operation.DELETE) {
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
    public String prettyHtml() {
        StringBuilder html = new StringBuilder();
        for (RowChangement<cell_t> aDiff : this) {
            String text = aDiff.getTextAsString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\n", "&para;<br>");
            switch (aDiff.operation) {
            case INSERT:
                html.append("<ins style=\"background:#e6ffe6;\">").append(text).append("</ins>");
                break;
            case DELETE:
                html.append("<del style=\"background:#ffe6e6;\">").append(text).append("</del>");
                break;
            case EQUAL:
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
    public IRow<cell_t> restoreRow1() {
        MutableRow<cell_t> concat = new MutableRow<cell_t>();
        for (RowChangement<cell_t> aDiff : this) {
            if (aDiff.operation != Operation.INSERT) {
                concat.append(aDiff.row);
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
    public IRow<cell_t> restoreRow2() {
        MutableRow<cell_t> concat = new MutableRow<cell_t>();
        for (RowChangement<cell_t> aDiff : this) {
            if (aDiff.operation != Operation.DELETE) {
                concat.append(aDiff.row);
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
    public int levenshtein() {
        int levenshtein = 0;
        int insertions = 0;
        int deletions = 0;
        for (RowChangement<cell_t> aDiff : this) {
            switch (aDiff.operation) {
            case INSERT:
                insertions += aDiff.row.length();
                break;
            case DELETE:
                deletions += aDiff.row.length();
                break;
            case EQUAL:
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
     * Crush the diff into an encoded string which describes the operations required to transform
     * text1 into text2. E.g. =3\t-2\t+ing -> Keep 3 chars, delete 2 chars, insert 'ing'. Operations
     * are tab-separated. Inserted text is escaped using %xx notation.
     *
     * @param diffs
     *            List of Diff objects.
     * @return Delta text.
     */
    public String toDelta() {
        StringBuilder sb = new StringBuilder();
        for (RowChangement<cell_t> aDiff : this) {
            switch (aDiff.operation) {
            case INSERT:
                sb.append("+").append(JsCompat.encodeUri(aDiff.getTextAsString())).append("\t");
                break;
            case DELETE:
                sb.append("-").append(aDiff.row.length()).append("\t");
                break;
            case EQUAL:
                sb.append("=").append(aDiff.row.length()).append("\t");
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

    /**
     * Given the original text1, and an encoded string which describes the operations required to
     * transform text1 into text2, compute the full diff.
     *
     * @param text1
     *            Source string for the diff.
     * @param delta
     *            Delta text.
     * @throws IllegalArgumentException
     *             If invalid input.
     */
    public static <cell_t> ChangeList<cell_t> fromDelta(DMPDiff<cell_t> diff, //
            IRow<cell_t> text1, String delta)
            throws IllegalArgumentException {

        ChangeList<cell_t> diffs = new ChangeList<cell_t>(diff);

        int pointer = 0; // Cursor in text1
        String[] tokens = delta.split("\t");
        for (String token : tokens) {
            if (token.length() == 0) {
                // Blank tokens are ok (from a trailing \t).
                continue;
            }
            // Each token begins with a one character parameter which specifies the
            // operation of this token (delete, insert, equality).
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
                IRow<cell_t> line = diff.parse(param);
                diffs.add(new RowChangement<cell_t>(Operation.INSERT, line));
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
                    text = text1.slice(pointer, pointer += n);
                } catch (StringIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(
                            "Delta length (" + pointer + ") larger than source text length (" + text1.length() + ").",
                            e);
                }
                if (token.charAt(0) == '=') {
                    diffs.add(new RowChangement<cell_t>(Operation.EQUAL, text));
                } else {
                    diffs.add(new RowChangement<cell_t>(Operation.DELETE, text));
                }
                break;
            default:
                // Anything else is an error.
                throw new IllegalArgumentException("Invalid diff operation in diff_fromDelta: " + token.charAt(0));
            }
        }
        if (pointer != text1.length()) {
            throw new IllegalArgumentException(
                    "Delta length (" + pointer + ") smaller than source text length (" + text1.length() + ").");
        }
        return diffs;
    }

    /**
     * Compute a list of patches to turn text1 into text2. A set of diffs will be computed.
     *
     * @param row1
     *            Old text.
     * @param row2
     *            New text.
     * @return LinkedList of Patch objects.
     */
    public PatchList<cell_t> createPatch(IRow<cell_t> row1) {
        if (row1 == null)
            throw new IllegalArgumentException("Null inputs. (patch_make)");

        if (size() > 2) {
            cleanupSemantic();
            cleanupEfficiency();
        }

        return patch_make(row1, this);
    }

    /**
     * Compute a list of patches to turn text1 into text2. text1 will be derived from the provided
     * diffs.
     *
     * @param diffs
     *            Array of Diff objects for text1 to text2.
     * @return LinkedList of Patch objects.
     */
    public PatchList<cell_t> createPatch() {
        // No origin string provided, compute our own.
        IRow<cell_t> row1 = restoreRow1();
        return patch_make(row1, this);
    }

    /**
     * Compute a list of patches to turn text1 into text2. text2 is not provided, diffs are the
     * delta between text1 and text2.
     *
     * @param text1
     *            Old text.
     * @param diffs
     *            Array of Diff objects for text1 to text2.
     * @return LinkedList of Patch objects.
     */
    PatchList<cell_t> patch_make(IRow<cell_t> text1, ChangeList<cell_t> diffs) {
        if (text1 == null || diffs == null) {
            throw new IllegalArgumentException("Null inputs. (patch_make)");
        }

        PatchList<cell_t> patches = new PatchList<cell_t>(diff);
        if (diffs.isEmpty()) {
            return patches; // Get rid of the null case.
        }
        Patch<cell_t> patch = new Patch<cell_t>(diff);
        int char_count1 = 0; // Number of characters into the text1 string.
        int char_count2 = 0; // Number of characters into the text2 string.
        // Start with text1 (prepatch_text) and apply the diffs until we arrive at
        // text2 (postpatch_text). We recreate the patches one by one to determine
        // context info.
        IRow<cell_t> prepatch_text = text1;
        IRow<cell_t> postpatch_text = text1;
        for (RowChangement<cell_t> aDiff : diffs) {
            if (patch.diffs.isEmpty() && aDiff.operation != Operation.EQUAL) {
                // A new patch starts here.
                patch.start1 = char_count1;
                patch.start2 = char_count2;
            }

            switch (aDiff.operation) {
            case INSERT:
                patch.diffs.add(aDiff);
                patch.length2 += aDiff.row.length();
                postpatch_text = postpatch_text.slice(0, char_count2).concat(aDiff.row)
                        .concat(postpatch_text.slice(char_count2));
                break;
            case DELETE:
                patch.length1 += aDiff.row.length();
                patch.diffs.add(aDiff);
                postpatch_text = postpatch_text.slice(0, char_count2)
                        .concat(postpatch_text.slice(char_count2 + aDiff.row.length()));
                break;
            case EQUAL:
                if (aDiff.row.length() <= 2 * config.Patch_Margin && !patch.diffs.isEmpty()
                        && aDiff != diffs.getLast()) {
                    // Small equality inside a patch.
                    patch.diffs.add(aDiff);
                    patch.length1 += aDiff.row.length();
                    patch.length2 += aDiff.row.length();
                }

                if (aDiff.row.length() >= 2 * config.Patch_Margin && !patch.diffs.isEmpty()) {
                    // Time for a new patch.
                    if (!patch.diffs.isEmpty()) {
                        patch.addContext(prepatch_text);
                        patches.add(patch);
                        patch = new Patch<cell_t>(diff);
                        // Unlike Unidiff, our patch lists have a rolling context.
                        // https://github.com/google/diff-match-patch/wiki/Unidiff
                        // Update prepatch text & pos to reflect the application of the
                        // just completed patch.
                        prepatch_text = postpatch_text;
                        char_count1 = char_count2;
                    }
                }
                break;
            }

            // Update the current character count.
            if (aDiff.operation != Operation.INSERT) {
                char_count1 += aDiff.row.length();
            }
            if (aDiff.operation != Operation.DELETE) {
                char_count2 += aDiff.row.length();
            }
        }
        // Pick up the leftover patch if not empty.
        if (!patch.diffs.isEmpty()) {
            patch.addContext(prepatch_text);
            patches.add(patch);
        }

        return patches;
    }

}
