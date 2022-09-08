package net.bodz.bas.compare.dmp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;
import net.bodz.bas.text.row.Rows;

public class PatchList<cell_t>
        extends LinkedList<Patch<cell_t>> {

    private static final long serialVersionUID = 1L;

    DMPConfig config;
    DMPRowComparator<cell_t> dmp;
    RowMatcher<cell_t> matcher;

    public PatchList(DMPRowComparator<cell_t> dmp) {
        this.config = dmp.config;
        this.dmp = dmp;
        this.matcher = new RowMatcher<cell_t>(config);
    }

    public boolean isChanged() {
        for (Patch<cell_t> patch : this) {
            if (!patch.diffs.isEmpty())
                return true;
        }
        return false;
    }

    public boolean isNoChange() {
        return !isChanged();
    }

    public boolean isSame() {
        return isNoChange();
    }

    public boolean isDifferent() {
        return isChanged();
    }

    /**
     * Given an array of patches, return another array that is identical.
     *
     * @param patches
     *            Array of Patch objects.
     * @return Array of Patch objects.
     */
    public PatchList<cell_t> deepCopy() {
        PatchList<cell_t> patchesCopy = new PatchList<cell_t>(dmp);
        for (Patch<cell_t> aPatch : this) {
            Patch<cell_t> patchCopy = new Patch<cell_t>(dmp);
            for (RowEdit<cell_t> aDiff : aPatch.diffs) {
                RowEdit<cell_t> diffCopy = new RowEdit<cell_t>(aDiff.type, aDiff.delta);
                patchCopy.diffs.append(diffCopy);
            }
            patchCopy.start1 = aPatch.start1;
            patchCopy.start2 = aPatch.start2;
            patchCopy.length1 = aPatch.length1;
            patchCopy.length2 = aPatch.length2;
            patchesCopy.add(patchCopy);
        }
        return patchesCopy;
    }

    /**
     * Merge a set of patches onto the text. Return a patched text, as well as an array of
     * true/false values indicating which patches were applied.
     *
     * @param patches
     *            Array of Patch objects
     * @param row
     *            Old text.
     * @return Two element Object array, containing the new text and an array of boolean values.
     */
    public PatchApplyResult<cell_t> apply(IRow<cell_t> row) {
        PatchApplyResult<cell_t> result = new PatchApplyResult<cell_t>();
        if (this.isEmpty()) {
            result.setPatchedRow(row.copy());
            return result;
        }

        // Deep copy the patches so that no changes are made to originals.
        PatchList<cell_t> patches = this.deepCopy();

        IRow<cell_t> nullPadding = patches.addPadding();
        MutableRow<cell_t> buf = new MutableRow<cell_t>(row.length() * 3 / 2);
        buf.append(nullPadding);
        buf.append(row);
        buf.append(nullPadding);

        patches.splitMax();

        // delta keeps track of the offset between the expected and actual location
        // of the previous patch. If there are patches expected at positions 10 and
        // 20, but the first patch was found at 12, delta is 2 and the second patch
        // has an effective expected position of 22.
        int delta = 0;
        for (Patch<cell_t> aPatch : patches) {
            int expected_loc = aPatch.start2 + delta;
            IRow<cell_t> row1 = aPatch.diffs.restoreRow1();
            int start_loc;
            int end_loc = -1;
            if (row1.length() > config.Match_MaxBits) {
                // patch_splitMax will only provide an oversized pattern in the case of
                // a monster delete.
                start_loc = matcher.search(buf, row1.slice(0, config.Match_MaxBits), expected_loc);
                if (start_loc != -1) {
                    end_loc = matcher.search(buf, row1.slice(row1.length() - config.Match_MaxBits),
                            expected_loc + row1.length() - config.Match_MaxBits);
                    if (end_loc == -1 || start_loc >= end_loc) {
                        // Can't find valid trailing context. Drop this patch.
                        start_loc = -1;
                    }
                }
            } else {
                start_loc = matcher.search(buf, row1, expected_loc);
            }
            MatchState state;
            if (start_loc == -1) {
                // No match found. :(
                state = MatchState.NO_MATCH;
                // Subtract the delta for this failed patch from subsequent patches.
                delta -= aPatch.length2 - aPatch.length1;
            } else {
                // Found a match. :)
                state = MatchState.MATCH;
                delta = start_loc - expected_loc;
                IRow<cell_t> row2;
                if (end_loc == -1) {
                    row2 = buf.slice(start_loc, Math.min(start_loc + row1.length(), buf.length()));
                } else {
                    row2 = buf.slice(start_loc, Math.min(end_loc + config.Match_MaxBits, buf.length()));
                }
                if (row1.equals(row2)) {
                    // Perfect match, just shove the replacement text in.
                    buf.replace(start_loc, start_loc + row1.length(), aPatch.diffs.restoreRow2());
                } else {
                    // Imperfect match. Run a diff to get a framework of equivalent
                    // indices.
                    EditList<cell_t> diffs = dmp.compare(row1, row2);
                    if (row1.length() > config.Match_MaxBits
                            && diffs.levenshtein() / (float) row1.length() > config.Patch_DeleteThreshold) {
                        // The end points match, but the content is unacceptably bad.
                        state = MatchState.BAD_MATCH;
                    } else {
                        diffs.cleanupSemanticLossless();
                        int index1 = 0;
                        for (RowEdit<cell_t> aDiff : aPatch.diffs) {
                            if (aDiff.type != DifferenceType.MATCH) {
                                int index2 = diffs.xIndex(index1);
                                if (aDiff.type == DifferenceType.INSERTION) {
                                    // Insertion
                                    buf.insert(start_loc + index2, aDiff.delta);
                                } else if (aDiff.type == DifferenceType.REMOVAL) {
                                    // Deletion
                                    buf.delete(start_loc + index2,
                                            start_loc + diffs.xIndex(index1 + aDiff.delta.length()));
                                }
                            }
                            if (aDiff.type != DifferenceType.REMOVAL) {
                                index1 += aDiff.delta.length();
                            }
                        }
                        state = MatchState.GOOD_MATCH;
                    }
                }
            }
            result.add(new PatchApplyStatus<cell_t>(aPatch, state));
        }
        // Strip the padding off.
        buf.preserve(nullPadding.length(), buf.length() - nullPadding.length());
        result.setPatchedRow(buf);
        return result;
    }

    /**
     * Add some padding on text start and end so that edges can match something. Intended to be
     * called only from within patch_apply.
     *
     * @param patches
     *            Array of Patch objects.
     * @return The padding string added to each side.
     */
    public IRow<cell_t> addPadding() {
        short paddingLength = this.config.Patch_Margin;
        MutableRow<cell_t> nullPadding = new MutableRow<cell_t>();
        for (short x = 1; x <= paddingLength; x++) {
            cell_t pad = dmp.createPadding(x);
            nullPadding.append(pad);
        }

        // Bump all the patches forward.
        for (Patch<cell_t> aPatch : this) {
            aPatch.start1 += paddingLength;
            aPatch.start2 += paddingLength;
        }

        // Add some padding on start of first diff.
        Patch<cell_t> patch = this.getFirst();
        EditList<cell_t> diffs = patch.diffs;
        if (diffs.isEmpty() || diffs.getFirst().type != DifferenceType.MATCH) {
            // Add nullPadding equality.
            diffs.prepend(new RowDifference<cell_t>(DifferenceType.MATCH, nullPadding));
            patch.start1 -= paddingLength; // Should be 0.
            patch.start2 -= paddingLength; // Should be 0.
            patch.length1 += paddingLength;
            patch.length2 += paddingLength;
        } else if (paddingLength > diffs.getFirst().delta.length()) {
            // Grow first equality.
            RowEdit<cell_t> firstDiff = diffs.getFirst();
            int extraLength = paddingLength - firstDiff.delta.length();
            firstDiff.delta = nullPadding.slice(firstDiff.delta.length()).concat(firstDiff.delta);
            patch.start1 -= extraLength;
            patch.start2 -= extraLength;
            patch.length1 += extraLength;
            patch.length2 += extraLength;
        }

        // Add some padding on end of last diff.
        patch = this.getLast();
        diffs = patch.diffs;
        if (diffs.isEmpty() || diffs.getLast().type != DifferenceType.MATCH) {
            // Add nullPadding equality.
            diffs.append(new RowDifference<cell_t>(DifferenceType.MATCH, nullPadding));
            patch.length1 += paddingLength;
            patch.length2 += paddingLength;
        } else if (paddingLength > diffs.getLast().delta.length()) {
            // Grow last equality.
            RowEdit<cell_t> lastDiff = diffs.getLast();
            int extraLength = paddingLength - lastDiff.delta.length();
            lastDiff.delta = lastDiff.delta.concat(nullPadding.slice(0, extraLength));
            patch.length1 += extraLength;
            patch.length2 += extraLength;
        }

        return nullPadding;
    }

    /**
     * Look through the patches and break up any which are longer than the maximum limit of the
     * match algorithm. Intended to be called only from within patch_apply.
     *
     * @param patches
     *            LinkedList of Patch objects.
     */
    public void splitMax() {
        short patch_size = config.Match_MaxBits;
        IRow<cell_t> precontext, postcontext;
        Patch<cell_t> patch;
        int start1, start2;
        boolean empty;
        DifferenceType diff_type;
        IRow<cell_t> diff_text;
        ListIterator<Patch<cell_t>> pointer = this.listIterator();
        Patch<cell_t> bigpatch = pointer.hasNext() ? pointer.next() : null;
        while (bigpatch != null) {
            if (bigpatch.length1 <= config.Match_MaxBits) {
                bigpatch = pointer.hasNext() ? pointer.next() : null;
                continue;
            }
            // Remove the big old patch.
            pointer.remove();
            start1 = bigpatch.start1;
            start2 = bigpatch.start2;
            precontext = Rows.empty();
            while (!bigpatch.diffs.isEmpty()) {
                // Create one of several smaller patches.
                patch = new Patch<cell_t>(dmp);
                empty = true;
                patch.start1 = start1 - precontext.length();
                patch.start2 = start2 - precontext.length();
                if (precontext.length() != 0) {
                    patch.length1 = patch.length2 = precontext.length();
                    patch.diffs.append(new RowDifference<cell_t>(DifferenceType.MATCH, precontext));
                }
                while (!bigpatch.diffs.isEmpty() && patch.length1 < patch_size - config.Patch_Margin) {
                    diff_type = bigpatch.diffs.getFirst().type;
                    diff_text = bigpatch.diffs.getFirst().delta;
                    if (diff_type == DifferenceType.INSERTION) {
                        // Insertions are harmless.
                        patch.length2 += diff_text.length();
                        start2 += diff_text.length();
                        patch.diffs.append(bigpatch.diffs.removeFirst());
                        empty = false;
                    } else if (diff_type == DifferenceType.REMOVAL && patch.diffs.size() == 1
                            && patch.diffs.getFirst().type == DifferenceType.MATCH
                            && diff_text.length() > 2 * patch_size) {
                        // This is a large deletion. Let it pass in one chunk.
                        patch.length1 += diff_text.length();
                        start1 += diff_text.length();
                        empty = false;
                        patch.diffs.append(new RowDifference<cell_t>(diff_type, diff_text));
                        bigpatch.diffs.removeFirst();
                    } else {
                        // Deletion or equality. Only take as much as we can stomach.
                        diff_text = diff_text.slice(0,
                                Math.min(diff_text.length(), patch_size - patch.length1 - config.Patch_Margin));
                        patch.length1 += diff_text.length();
                        start1 += diff_text.length();
                        if (diff_type == DifferenceType.MATCH) {
                            patch.length2 += diff_text.length();
                            start2 += diff_text.length();
                        } else {
                            empty = false;
                        }
                        patch.diffs.append(new RowDifference<cell_t>(diff_type, diff_text));
                        if (diff_text.equals(bigpatch.diffs.getFirst().delta)) {
                            bigpatch.diffs.removeFirst();
                        } else {
                            bigpatch.diffs.getFirst().delta = bigpatch.diffs.getFirst().delta.slice(diff_text.length());
                        }
                    }
                }
                // Compute the head context for the next patch.
                precontext = patch.diffs.restoreRow2();
                precontext = precontext.slice(Math.max(0, precontext.length() - config.Patch_Margin));
                // Append the end context for this patch.
                if (bigpatch.diffs.restoreRow1().length() > config.Patch_Margin) {
                    postcontext = bigpatch.diffs.restoreRow1().slice(0, config.Patch_Margin);
                } else {
                    postcontext = bigpatch.diffs.restoreRow1();
                }
                if (postcontext.length() != 0) {
                    patch.length1 += postcontext.length();
                    patch.length2 += postcontext.length();
                    if (!patch.diffs.isEmpty() && patch.diffs.getLast().type == DifferenceType.MATCH) {
                        patch.diffs.getLast().delta = patch.diffs.getLast().delta.concat(postcontext);
                    } else {
                        patch.diffs.append(new RowDifference<cell_t>(DifferenceType.MATCH, postcontext));
                    }
                }
                if (!empty) {
                    pointer.add(patch);
                }
            }
            bigpatch = pointer.hasNext() ? pointer.next() : null;
        }
    }

    /**
     * Take a list of patches and return a textual representation.
     *
     * @param patches
     *            List of Patch objects.
     * @return Text representation of patches.
     */
    public String toText() {
        StringBuilder text = new StringBuilder();
        for (Patch<cell_t> aPatch : this) {
            text.append(aPatch);
        }
        return text.toString();
    }

    /**
     * Parse a textual representation of patches and return a List of Patch objects.
     *
     * @param patchcode
     *            Text representation of patches.
     * @return List of Patch objects.
     * @throws IllegalArgumentException
     *             If invalid input.
     */
    public static <cell_t> PatchList<cell_t> fromText(DMPRowComparator<cell_t> diff, String patchcode)
            throws IllegalArgumentException {
        PatchList<cell_t> patches = new PatchList<cell_t>(diff);
        if (patchcode.length() == 0) {
            return patches;
        }

        LinkedList<String> codelines = new LinkedList<String>(Arrays.asList(patchcode.split("\n")));
        Patch<cell_t> patch;
        Pattern patchHeader = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@$");
        Matcher m;
        char sign;
        String codeline;
        while (!codelines.isEmpty()) {
            m = patchHeader.matcher(codelines.getFirst());
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid patch string: " + codelines.getFirst());
            }
            patch = new Patch<cell_t>(diff);
            patches.add(patch);
            patch.start1 = Integer.parseInt(m.group(1));
            if (m.group(2).length() == 0) {
                patch.start1--;
                patch.length1 = 1;
            } else if (m.group(2).equals("0")) {
                patch.length1 = 0;
            } else {
                patch.start1--;
                patch.length1 = Integer.parseInt(m.group(2));
            }

            patch.start2 = Integer.parseInt(m.group(3));
            if (m.group(4).length() == 0) {
                patch.start2--;
                patch.length2 = 1;
            } else if (m.group(4).equals("0")) {
                patch.length2 = 0;
            } else {
                patch.start2--;
                patch.length2 = Integer.parseInt(m.group(4));
            }
            codelines.removeFirst();

            while (!codelines.isEmpty()) {
                try {
                    sign = codelines.getFirst().charAt(0);
                } catch (IndexOutOfBoundsException e) {
                    // Blank line? Whatever.
                    codelines.removeFirst();
                    continue;
                }
                codeline = codelines.getFirst().substring(1);
                codeline = codeline.replace("+", "%2B"); // decode would change all "+" to " "
                try {
                    codeline = URLDecoder.decode(codeline, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Not likely on modern system.
                    throw new Error("This system does not support UTF-8.", e);
                } catch (IllegalArgumentException e) {
                    // Malformed URI sequence.
                    throw new IllegalArgumentException("Illegal escape in patch_fromText: " + codeline, e);
                }

                IRow<cell_t> textline = diff.parse(codeline);
                if (sign == '-') {
                    // Deletion.
                    patch.diffs.append(new RowDifference<cell_t>(DifferenceType.REMOVAL, textline));
                } else if (sign == '+') {
                    // Insertion.
                    patch.diffs.append(new RowDifference<cell_t>(DifferenceType.INSERTION, textline));
                } else if (sign == ' ') {
                    // Minor equality.
                    patch.diffs.append(new RowDifference<cell_t>(DifferenceType.MATCH, textline));
                } else if (sign == '@') {
                    // Start of next patch.
                    break;
                } else {
                    // WTF?
                    throw new IllegalArgumentException("Invalid patch mode '" + sign + "' in: " + codeline);
                }
                codelines.removeFirst();
            }
        }
        return patches;
    }

}
