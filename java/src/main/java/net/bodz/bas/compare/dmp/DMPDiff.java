package net.bodz.bas.compare.dmp;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.bodz.bas.text.Nullables;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;
import net.bodz.bas.text.row.Rows;

public abstract class DMPDiff<cell_t>
        extends AbstractRowCompare<cell_t>
        implements
            ICleanupSemanticScore<cell_t> {

    public final Config config;
    RowPacker<cell_t> packer;
    RowMatcher<cell_t> rowMatcher;

    public DMPDiff(Config config) {
        this.config = config;
        packer = new RowPacker<cell_t>(this);
        rowMatcher = new RowMatcher<cell_t>(config);
    }

    protected abstract cell_t separator();

    protected abstract cell_t createPadding();

    protected abstract cell_t createPadding(int hint);

    @Override
    public <T extends cell_t> ChangeList<T> compare(IRow<T> row1, IRow<T> row2) {
        return compare(row1, row2, false);
    }

    public <T extends cell_t> ChangeList<T> comparePacked(IRow<T> row1, IRow<T> row2) {
        return compare(row1, row2, true);
    }

    public <T extends cell_t> ChangeList<T> compare(IRow<T> row1, IRow<T> row2, boolean packed) {
        // Set a deadline by which time the diff must be complete.
        long deadline;
        if (config.Diff_Timeout <= 0) {
            deadline = Long.MAX_VALUE;
        } else {
            deadline = System.currentTimeMillis() + (long) (config.Diff_Timeout * 1000);
        }
        return compareImpl(row1, row2, true, deadline);
    }

    /**
     * Find the differences between two texts. Simplifies the problem by stripping any common prefix
     * or suffix off the texts before diffing.
     *
     * @param text1
     *            Old string to be diffed.
     * @param text2
     *            New string to be diffed.
     * @param checklines
     *            Speedup flag. If false, then don't run a line-level diff first to identify the
     *            changed areas. If true, then run a faster slightly less optimal diff.
     * @param deadline
     *            Time when the diff should be complete by. Used internally for recursive calls.
     *            Users should set DiffTimeout instead.
     * @return Linked List of Diff objects.
     */
    <T extends cell_t> ChangeList<T> compareImpl(IRow<T> text1, IRow<T> text2, boolean checklines, long deadline) {
        // Check for null inputs.
        if (text1 == null || text2 == null) {
            throw new IllegalArgumentException("Null inputs. (diff_main)");
        }

        // Check for equality (speedup).
        ChangeList<T> diffs;
        if (text1.equals(text2)) {
            diffs = new ChangeList<T>(this);
            if (text1.length() != 0) {
                diffs.add(new RowChangement<T>(Operation.EQUAL, text1));
            }
            return diffs;
        }

        // Trim off common prefix (speedup).
        int commonlength = RowUtils.commonPrefix(text1, text2);
        IRow<T> commonprefix = text1.slice(0, commonlength);
        text1 = text1.slice(commonlength, text1.length());
        text2 = text2.slice(commonlength, text2.length());

        // Trim off common suffix (speedup).
        commonlength = RowUtils.commonSuffix(text1, text2);
        IRow<T> commonsuffix = text1.slice(text1.length() - commonlength, text1.length());
        text1 = text1.slice(0, text1.length() - commonlength);
        text2 = text2.slice(0, text2.length() - commonlength);

        // Compute the diff on the middle block.
        diffs = diff_compute(text1, text2, checklines, deadline);

        // Restore the prefix and suffix.
        if (commonprefix.length() != 0) {
            diffs.addFirst(new RowChangement<T>(Operation.EQUAL, commonprefix));
        }
        if (commonsuffix.length() != 0) {
            diffs.addLast(new RowChangement<T>(Operation.EQUAL, commonsuffix));
        }

        diffs.cleanupMerge();
        return diffs;
    }

    /**
     * Find the differences between two texts. Assumes that the texts do not have any common prefix
     * or suffix.
     *
     * @param text1
     *            Old string to be diffed.
     * @param text2
     *            New string to be diffed.
     * @param checklines
     *            Speedup flag. If false, then don't run a line-level diff first to identify the
     *            changed areas. If true, then run a faster slightly less optimal diff.
     * @param deadline
     *            Time when the diff should be complete by.
     * @return Linked List of Diff objects.
     */
    private <T extends cell_t> ChangeList<T> diff_compute(IRow<T> text1, IRow<T> text2, boolean checklines,
            long deadline) {
        ChangeList<T> diffs = new ChangeList<T>(this);

        if (text1.length() == 0) {
            // Just add some text (speedup).
            diffs.add(new RowChangement<T>(Operation.INSERT, text2));
            return diffs;
        }

        if (text2.length() == 0) {
            // Just delete some text (speedup).
            diffs.add(new RowChangement<T>(Operation.DELETE, text1));
            return diffs;
        }

        IRow<T> longtext = text1.length() > text2.length() ? text1 : text2;
        IRow<T> shorttext = text1.length() > text2.length() ? text2 : text1;
        int i = longtext.indexOf(shorttext);
        if (i != -1) {
            // Shorter text is inside the longer text (speedup).
            Operation op = (text1.length() > text2.length()) ? Operation.DELETE : Operation.INSERT;
            diffs.add(new RowChangement<T>(op, longtext.slice(0, i)));
            diffs.add(new RowChangement<T>(Operation.EQUAL, shorttext));
            diffs.add(new RowChangement<T>(op, longtext.slice(i + shorttext.length(), longtext.length())));
            return diffs;
        }

        if (shorttext.length() == 1) {
            // Single character string.
            // After the previous speedup, the character can't be an equality.
            diffs.add(new RowChangement<T>(Operation.DELETE, text1));
            diffs.add(new RowChangement<T>(Operation.INSERT, text2));
            return diffs;
        }

        // Check to see if the problem can be split in two.
        HalfMatch<T> hm = RowUtils.halfMatch(config, text1, text2);
        if (hm != null) {
            // A half-match was found, sort out the return data.
            IRow<T> text1_a = hm.prefix1;
            IRow<T> text1_b = hm.suffix1;
            IRow<T> text2_a = hm.prefix2;
            IRow<T> text2_b = hm.suffix2;
            IRow<T> mid_common = hm.common;
            // Send both pairs off for separate processing.
            ChangeList<T> diffs_a = compareImpl(text1_a, text2_a, checklines, deadline);
            ChangeList<T> diffs_b = compareImpl(text1_b, text2_b, checklines, deadline);
            // Merge the results.
            diffs = diffs_a;
            diffs.add(new RowChangement<T>(Operation.EQUAL, mid_common));
            diffs.addAll(diffs_b);
            return diffs;
        }

        if (checklines && text1.length() > 100 && text2.length() > 100) {
            return diff_lineMode(text1, text2, deadline);
        }

        return diff_bisect(text1, text2, deadline);
    }

    /**
     * Do a quick line-level diff on both strings, then rediff the parts for greater accuracy. This
     * speedup can produce non-minimal diffs.
     *
     * @param text1
     *            Old string to be diffed.
     * @param text2
     *            New string to be diffed.
     * @param deadline
     *            Time when the diff should be complete by.
     * @return Linked List of Diff objects.
     */
    private <T extends cell_t> ChangeList<T> diff_lineMode(IRow<T> text1, IRow<T> text2, long deadline) {
        // Scan the text on a line-by-line basis first.
        LinesToCharsResult<T> a = packer.linesToChars(text1, text2);
        List<IRow<T>> linearray = a.lineArray;

        IntCharsDiff intCharsDiff = new IntCharsDiff(config);
        LinkedList<RowChangement<Integer>> atom_diffs = //
                intCharsDiff.compareImpl(a.chars1, a.chars2, false, deadline);

        // Convert the diff back to original text.
        ChangeList<T> diffs = packer.charsToLines(atom_diffs, linearray);
        // Eliminate freak matches (e.g. blank lines)
        diffs.cleanupSemantic();

        // Rediff any replacement blocks, this time character-by-character.
        // Add a dummy entry at the end.
        diffs.add(new RowChangement<T>(Operation.EQUAL, Rows.<T> empty()));
        int count_delete = 0;
        int count_insert = 0;
        MutableRow<T> text_delete = new MutableRow<T>();
        MutableRow<T> text_insert = new MutableRow<T>();
        ListIterator<RowChangement<T>> pointer = diffs.listIterator();
        RowChangement<T> thisDiff = pointer.next();
        while (thisDiff != null) {
            switch (thisDiff.operation) {
            case INSERT:
                count_insert++;
                text_insert.append(thisDiff.text);
                break;
            case DELETE:
                count_delete++;
                text_delete.append(thisDiff.text);
                break;
            case EQUAL:
                // Upon reaching an equality, check for prior redundancies.
                if (count_delete >= 1 && count_insert >= 1) {
                    // Delete the offending records and add the merged ones.
                    pointer.previous();
                    for (int j = 0; j < count_delete + count_insert; j++) {
                        pointer.previous();
                        pointer.remove();
                    }
                    for (RowChangement<T> subDiff : compareImpl(text_delete, text_insert, false, deadline)) {
                        pointer.add(subDiff);
                    }
                }
                count_insert = 0;
                count_delete = 0;
                text_delete.clear();
                text_insert.clear();
                break;
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
        diffs.removeLast(); // Remove the dummy entry at the end.

        return diffs;
    }

    /**
     * Find the 'middle snake' of a diff, split the problem in two and return the recursively
     * constructed diff. See Myers 1986 paper: An O(ND) Difference Algorithm and Its Variations.
     *
     * @param text1
     *            Old string to be diffed.
     * @param text2
     *            New string to be diffed.
     * @param deadline
     *            Time at which to bail if not yet complete.
     * @return LinkedList of Diff objects.
     */
    <T extends cell_t> ChangeList<T> diff_bisect(IRow<T> text1, IRow<T> text2, long deadline) {
        // Cache the text lengths to prevent multiple calls.
        int text1_length = text1.length();
        int text2_length = text2.length();
        int max_d = (text1_length + text2_length + 1) / 2;
        int v_offset = max_d;
        int v_length = 2 * max_d;
        int[] v1 = new int[v_length];
        int[] v2 = new int[v_length];
        for (int x = 0; x < v_length; x++) {
            v1[x] = -1;
            v2[x] = -1;
        }
        v1[v_offset + 1] = 0;
        v2[v_offset + 1] = 0;
        int delta = text1_length - text2_length;
        // If the total number of characters is odd, then the front path will
        // collide with the reverse path.
        boolean front = (delta % 2 != 0);
        // Offsets for start and end of k loop.
        // Prevents mapping of space beyond the grid.
        int k1start = 0;
        int k1end = 0;
        int k2start = 0;
        int k2end = 0;
        for (int d = 0; d < max_d; d++) {
            // Bail out if deadline is reached.
            if (System.currentTimeMillis() > deadline) {
                break;
            }

            // Walk the front path one step.
            for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2) {
                int k1_offset = v_offset + k1;
                int x1;
                if (k1 == -d || (k1 != d && v1[k1_offset - 1] < v1[k1_offset + 1])) {
                    x1 = v1[k1_offset + 1];
                } else {
                    x1 = v1[k1_offset - 1] + 1;
                }
                int y1 = x1 - k1;
                while (x1 < text1_length && y1 < text2_length && Nullables.equals(text1.cellAt(x1), text2.cellAt(y1))) {
                    x1++;
                    y1++;
                }
                v1[k1_offset] = x1;
                if (x1 > text1_length) {
                    // Ran off the right of the graph.
                    k1end += 2;
                } else if (y1 > text2_length) {
                    // Ran off the bottom of the graph.
                    k1start += 2;
                } else if (front) {
                    int k2_offset = v_offset + delta - k1;
                    if (k2_offset >= 0 && k2_offset < v_length && v2[k2_offset] != -1) {
                        // Mirror x2 onto top-left coordinate system.
                        int x2 = text1_length - v2[k2_offset];
                        if (x1 >= x2) {
                            // Overlap detected.
                            return diff_bisectSplit(text1, text2, x1, y1, deadline);
                        }
                    }
                }
            }

            // Walk the reverse path one step.
            for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2) {
                int k2_offset = v_offset + k2;
                int x2;
                if (k2 == -d || (k2 != d && v2[k2_offset - 1] < v2[k2_offset + 1])) {
                    x2 = v2[k2_offset + 1];
                } else {
                    x2 = v2[k2_offset - 1] + 1;
                }
                int y2 = x2 - k2;
                while (x2 < text1_length && y2 < text2_length
                        && Nullables.equals(text1.cellAt(text1_length - x2 - 1), text2.cellAt(text2_length - y2 - 1))) {
                    x2++;
                    y2++;
                }
                v2[k2_offset] = x2;
                if (x2 > text1_length) {
                    // Ran off the left of the graph.
                    k2end += 2;
                } else if (y2 > text2_length) {
                    // Ran off the top of the graph.
                    k2start += 2;
                } else if (!front) {
                    int k1_offset = v_offset + delta - k2;
                    if (k1_offset >= 0 && k1_offset < v_length && v1[k1_offset] != -1) {
                        int x1 = v1[k1_offset];
                        int y1 = v_offset + x1 - k1_offset;
                        // Mirror x2 onto top-left coordinate system.
                        x2 = text1_length - x2;
                        if (x1 >= x2) {
                            // Overlap detected.
                            return diff_bisectSplit(text1, text2, x1, y1, deadline);
                        }
                    }
                }
            }
        }
        // Diff took too long and hit the deadline or
        // number of diffs equals number of characters, no commonality at all.
        ChangeList<T> diffs = new ChangeList<T>(this);
        diffs.add(new RowChangement<T>(Operation.DELETE, text1));
        diffs.add(new RowChangement<T>(Operation.INSERT, text2));
        return diffs;
    }

    /**
     * Given the location of the 'middle snake', split the diff in two parts and recurse.
     *
     * @param text1
     *            Old string to be diffed.
     * @param text2
     *            New string to be diffed.
     * @param x
     *            Index of split point in text1.
     * @param y
     *            Index of split point in text2.
     * @param deadline
     *            Time at which to bail if not yet complete.
     * @return LinkedList of Diff objects.
     */
    private <T extends cell_t> ChangeList<T> diff_bisectSplit(IRow<T> text1, IRow<T> text2, int x, int y,
            long deadline) {
        IRow<T> text1a = text1.slice(0, x);
        IRow<T> text2a = text2.slice(0, y);
        IRow<T> text1b = text1.slice(x, text1.length());
        IRow<T> text2b = text2.slice(y, text2.length());

        // Compute both diffs serially.
        ChangeList<T> diffs = compareImpl(text1a, text2a, false, deadline);
        ChangeList<T> diffsb = compareImpl(text1b, text2b, false, deadline);

        diffs.addAll(diffsb);
        return diffs;
    }

    public RowMatcher<cell_t> matcher() {
        return rowMatcher;
    }

}
