package net.bodz.bas.compare.dmp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ListIterator;

import net.bodz.bas.text.Nullables;
import net.bodz.bas.text.row.IMutableRow;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;

public class EditList<cell_t>
        extends AbstractDiffList<RowEdit<cell_t>, cell_t> {

    public EditList(DMPRowComparator<cell_t> dmp) {
        super(dmp);
    }

    @Override
    public <T extends cell_t> void prepend(IRowDifference<T> o) {
        list.addFirst(RowEdit.<cell_t, T> copy(o));
    }

    @Override
    public <T extends cell_t> void append(IRowDifference<T> o) {
        list.addLast(RowEdit.<cell_t, T> copy(o));
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
        Deque<RowEdit<cell_t>> equalities = new ArrayDeque<RowEdit<cell_t>>();
        IMutableRow<cell_t> lastEquality = null; // Always equal to equalities.peek().text
        ListIterator<RowEdit<cell_t>> pointer = listIterator();

        // Number of characters that changed prior to the equality.
        int length_insertions1 = 0;
        int length_deletions1 = 0;

        // Number of characters that changed after the equality.
        int length_insertions2 = 0;
        int length_deletions2 = 0;
        RowEdit<cell_t> thisDiff = pointer.next();
        while (thisDiff != null) {
            if (thisDiff.operation == DifferenceType.MATCH) {
                // Equality found.
                equalities.push(thisDiff);
                length_insertions1 = length_insertions2;
                length_deletions1 = length_deletions2;
                length_insertions2 = 0;
                length_deletions2 = 0;
                lastEquality = thisDiff.row;
            } else {
                // An insertion or deletion.
                if (thisDiff.operation == DifferenceType.INSERTION) {
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
                    pointer.set(new RowEdit<cell_t>(DifferenceType.REMOVAL, lastEquality));
                    // Insert a corresponding an insert.
                    pointer.add(new RowEdit<cell_t>(DifferenceType.INSERTION, lastEquality));

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
        RowEdit<cell_t> prevDiff = null;
        thisDiff = null;
        if (pointer.hasNext()) {
            prevDiff = pointer.next();
            if (pointer.hasNext()) {
                thisDiff = pointer.next();
            }
        }
        while (thisDiff != null) {
            if (prevDiff.operation == DifferenceType.REMOVAL && thisDiff.operation == DifferenceType.INSERTION) {
                IMutableRow<cell_t> deletion = prevDiff.row;
                IMutableRow<cell_t> insertion = thisDiff.row;
                int overlap_length1 = RowUtils.commonOverlap(deletion, insertion);
                int overlap_length2 = RowUtils.commonOverlap(insertion, deletion);
                if (overlap_length1 >= overlap_length2) {
                    if (overlap_length1 >= deletion.length() / 2.0 || overlap_length1 >= insertion.length() / 2.0) {
                        // Overlap found. Insert an equality and trim the surrounding edits.
                        pointer.previous();
                        pointer.add(new RowEdit<cell_t>(DifferenceType.MATCH, insertion.slice(0, overlap_length1)));
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
                        pointer.add(new RowEdit<cell_t>(DifferenceType.MATCH, deletion.slice(0, overlap_length2)));
                        prevDiff.operation = DifferenceType.INSERTION;
                        prevDiff.row = insertion.slice(0, insertion.length() - overlap_length2);
                        thisDiff.operation = DifferenceType.REMOVAL;
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
        IMutableRow<cell_t> equality1, edit, equality2;
        IMutableRow<cell_t> commonString;
        int commonOffset;
        int score, bestScore;
        IMutableRow<cell_t> bestEquality1, bestEdit, bestEquality2;
        // Create a new iterator at the start.
        ListIterator<RowEdit<cell_t>> pointer = this.listIterator();
        RowEdit<cell_t> prevDiff = pointer.hasNext() ? pointer.next() : null;
        RowEdit<cell_t> thisDiff = pointer.hasNext() ? pointer.next() : null;
        RowEdit<cell_t> nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == DifferenceType.MATCH && nextDiff.operation == DifferenceType.MATCH) {
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
                bestScore = dmp.cleanupSemanticScore(equality1, edit) + dmp.cleanupSemanticScore(edit, equality2);
                while (edit.length() != 0 && equality2.length() != 0
                        && Nullables.equals(edit.cellAt(0), equality2.cellAt(0))) {
                    equality1 = equality1.concat(edit.cellAt(0));
                    edit = edit.slice(1).concat(equality2.cellAt(0));
                    equality2 = equality2.slice(1);
                    score = dmp.cleanupSemanticScore(equality1, edit) + dmp.cleanupSemanticScore(edit, equality2);
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
        Deque<RowEdit<cell_t>> equalities = new ArrayDeque<RowEdit<cell_t>>(); // Double-ended
        IMutableRow<cell_t> lastEquality = null; // Always equal to equalities.peek().text
        ListIterator<RowEdit<cell_t>> pointer = listIterator();
        // Is there an insertion operation before the last equality.
        boolean pre_ins = false;
        // Is there a deletion operation before the last equality.
        boolean pre_del = false;
        // Is there an insertion operation after the last equality.
        boolean post_ins = false;
        // Is there a deletion operation after the last equality.
        boolean post_del = false;
        RowEdit<cell_t> thisDiff = pointer.next();
        RowEdit<cell_t> safeDiff = thisDiff; // The last Diff that is known to be
                                             // unsplittable.
        while (thisDiff != null) {
            if (thisDiff.operation == DifferenceType.MATCH) {
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
                if (thisDiff.operation == DifferenceType.REMOVAL) {
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
                    pointer.set(new RowEdit<cell_t>(DifferenceType.REMOVAL, lastEquality));
                    // Insert a corresponding an insert.
                    pointer.add(thisDiff = new RowEdit<cell_t>(DifferenceType.INSERTION, lastEquality));

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
        this.add(new RowEdit<cell_t>( //
                DifferenceType.MATCH, new MutableRow<cell_t>()));

        ListIterator<RowEdit<cell_t>> pointer = this.listIterator();
        int count_delete = 0;
        int count_insert = 0;
        MutableRow<cell_t> text_delete = new MutableRow<cell_t>();
        MutableRow<cell_t> text_insert = new MutableRow<cell_t>();
        RowEdit<cell_t> thisDiff = pointer.next();
        RowEdit<cell_t> prevEqual = null;
        int commonlength;
        while (thisDiff != null) {
            switch (thisDiff.operation) {
            case INSERTION:
                count_insert++;
                text_insert.append(thisDiff.row);
                prevEqual = null;
                break;
            case REMOVAL:
                count_delete++;
                text_delete.append(thisDiff.row);
                prevEqual = null;
                break;
            case MATCH:
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
                                assert thisDiff.operation == DifferenceType.MATCH : "Previous diff should have been an equality.";
                                thisDiff.row = thisDiff.row.concat(text_insert.slice(0, commonlength));
                                pointer.next();
                            } else {
                                pointer.add(
                                        new RowEdit<cell_t>(DifferenceType.MATCH, text_insert.slice(0, commonlength)));
                            }
                            text_insert.preserve(commonlength);
                            text_delete.preserve(commonlength);
                        }
                        // Factor out any common suffixies.
                        commonlength = RowUtils.commonSuffix(text_insert, text_delete);
                        if (commonlength != 0) {
                            thisDiff = pointer.next();
                            thisDiff.row = text_insert.slice(text_insert.length() - commonlength).concat(thisDiff.row);
                            text_insert.delete(-commonlength);
                            text_delete.delete(-commonlength);
                            pointer.previous();
                        }
                    }
                    // Insert the merged records.
                    if (text_delete.length() != 0) {
                        pointer.add(new RowEdit<cell_t>(DifferenceType.REMOVAL, text_delete.copy()));
                    }
                    if (text_insert.length() != 0) {
                        pointer.add(new RowEdit<cell_t>(DifferenceType.INSERTION, text_insert.copy()));
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
        RowEdit<cell_t> prevDiff = pointer.hasNext() ? pointer.next() : null;
        thisDiff = pointer.hasNext() ? pointer.next() : null;
        RowEdit<cell_t> nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == DifferenceType.MATCH && nextDiff.operation == DifferenceType.MATCH) {
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

    @Override
    protected RowEdit<cell_t> createDifference(DifferenceType type, IRow<cell_t> row, boolean allocated) {
        IMutableRow<cell_t> m;
        if (allocated)
            m = row.unlock();
        else
            m = row.copy();
        return new RowEdit<cell_t>(type, m);
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
    public static <cell_t> EditList<cell_t> fromDelta(DMPRowComparator<cell_t> dmp, //
            IRow<cell_t> text1, String delta)
            throws IllegalArgumentException {
        EditList<cell_t> list = new EditList<cell_t>(dmp);
        list.readDelta(text1, delta);
        return list;
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
    PatchList<cell_t> patch_make(IRow<cell_t> text1, EditList<cell_t> diffs) {
        if (text1 == null || diffs == null) {
            throw new IllegalArgumentException("Null inputs. (patch_make)");
        }

        PatchList<cell_t> patches = new PatchList<cell_t>(dmp);
        if (diffs.isEmpty()) {
            return patches; // Get rid of the null case.
        }
        Patch<cell_t> patch = new Patch<cell_t>(dmp);
        int char_count1 = 0; // Number of characters into the text1 string.
        int char_count2 = 0; // Number of characters into the text2 string.
        // Start with text1 (prepatch_text) and apply the diffs until we arrive at
        // text2 (postpatch_text). We recreate the patches one by one to determine
        // context info.
        IRow<cell_t> prepatch_text = text1;
        IRow<cell_t> postpatch_text = text1;
        for (RowEdit<cell_t> aDiff : diffs) {
            if (patch.diffs.isEmpty() && aDiff.operation != DifferenceType.MATCH) {
                // A new patch starts here.
                patch.start1 = char_count1;
                patch.start2 = char_count2;
            }

            switch (aDiff.operation) {
            case INSERTION:
                patch.diffs.add(aDiff);
                patch.length2 += aDiff.row.length();
                postpatch_text = postpatch_text.slice(0, char_count2).concat(aDiff.row)
                        .concat(postpatch_text.slice(char_count2));
                break;
            case REMOVAL:
                patch.length1 += aDiff.row.length();
                patch.diffs.add(aDiff);
                postpatch_text = postpatch_text.slice(0, char_count2)
                        .concat(postpatch_text.slice(char_count2 + aDiff.row.length()));
                break;
            case MATCH:
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
                        patch = new Patch<cell_t>(dmp);
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
            if (aDiff.operation != DifferenceType.INSERTION) {
                char_count1 += aDiff.row.length();
            }
            if (aDiff.operation != DifferenceType.REMOVAL) {
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
