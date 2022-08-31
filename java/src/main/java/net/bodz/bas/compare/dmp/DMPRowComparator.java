package net.bodz.bas.compare.dmp;

import java.util.List;
import java.util.ListIterator;

import net.bodz.bas.text.Nullables;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.MutableRow;
import net.bodz.bas.text.row.Rows;

public abstract class DMPRowComparator<cell_t>
        extends AbstractRowComparator<cell_t>
        implements
            ICleanupSemanticScore<cell_t> {

    public final Config config;
    RowPacker<cell_t> packer;
    RowMatcher<cell_t> rowMatcher;

    public DMPRowComparator(Config config) {
        this.config = config;
        packer = new RowPacker<cell_t>(this);
        rowMatcher = new RowMatcher<cell_t>(config);
    }

    protected abstract cell_t separator();

    protected abstract cell_t createPadding();

    protected abstract cell_t createPadding(int hint);

    @Override
    public <T extends cell_t> EditList<cell_t> compare(IRow<T> row1, IRow<T> row2) {
        return _compare(row1, row2, false);
    }

    public <T extends cell_t> EditList<cell_t> compareByPack(IRow<T> row1, IRow<T> row2) {
        return _compare(row1, row2, true);
    }

    @Override
    public <T extends cell_t> EditList<cell_t> precompare(IRow<T> row1, IRow<T> row2) {
        return compare(row1, row2);
    }

    private <T extends cell_t> EditList<cell_t> _compare(IRow<T> row1, IRow<T> row2, boolean packing) {
        // Set a deadline by which time the diff must be complete.
        long deadline;
        if (config.Diff_Timeout <= 0) {
            deadline = Long.MAX_VALUE;
        } else {
            deadline = System.currentTimeMillis() + (long) (config.Diff_Timeout * 1000);
        }
        return _compare(row1, row2, true, deadline);
    }

    /**
     * Find the differences between two texts. Simplifies the problem by stripping any common prefix
     * or suffix off the texts before diffing.
     *
     * @param row1
     *            Old string to be diffed.
     * @param row2
     *            New string to be diffed.
     * @param packing
     *            Speedup flag. If false, then don't run a line-level diff first to identify the
     *            changed areas. If true, then run a faster slightly less optimal diff.
     * @param deadline
     *            Time when the diff should be complete by. Used internally for recursive calls.
     *            Users should set DiffTimeout instead.
     * @return Linked List of Diff objects.
     */
    <T extends cell_t> EditList<cell_t> _compare(IRow<T> row1, IRow<T> row2, boolean packing, long deadline) {
        // Check for null inputs.
        if (row1 == null || row2 == null) {
            throw new IllegalArgumentException("Null inputs. (diff_main)");
        }

        // Check for equality (speedup).
        EditList<cell_t> diffs;
        if (row1.equals(row2)) {
            diffs = new EditList<cell_t>(this);
            if (row1.length() != 0) {
                diffs.append(new RowDifference<T>(DifferenceType.MATCH, row1));
            }
            return diffs;
        }

        // Trim off common prefix (speedup).
        int commonlength = RowUtils.commonPrefix(row1, row2);
        IRow<T> commonprefix = row1.slice(0, commonlength);
        row1 = row1.slice(commonlength, row1.length());
        row2 = row2.slice(commonlength, row2.length());

        // Trim off common suffix (speedup).
        commonlength = RowUtils.commonSuffix(row1, row2);
        IRow<T> commonsuffix = row1.slice(row1.length() - commonlength, row1.length());
        row1 = row1.slice(0, row1.length() - commonlength);
        row2 = row2.slice(0, row2.length() - commonlength);

        // Compute the diff on the middle block.
        diffs = compute(row1, row2, packing, deadline);

        // Restore the prefix and suffix.
        if (commonprefix.length() != 0) {
            diffs.prepend(new RowDifference<T>(DifferenceType.MATCH, commonprefix));
        }
        if (commonsuffix.length() != 0) {
            diffs.append(new RowDifference<T>(DifferenceType.MATCH, commonsuffix));
        }

        diffs.cleanupMerge();
        return diffs;
    }

    /**
     * Find the differences between two texts. Assumes that the texts do not have any common prefix
     * or suffix.
     *
     * @param row1
     *            Old string to be diffed.
     * @param row2
     *            New string to be diffed.
     * @param packing
     *            Speedup flag. If false, then don't run a line-level diff first to identify the
     *            changed areas. If true, then run a faster slightly less optimal diff.
     * @param deadline
     *            Time when the diff should be complete by.
     * @return Linked List of Diff objects.
     */
    private <T extends cell_t> EditList<cell_t> compute(IRow<T> row1, IRow<T> row2, boolean packing, long deadline) {
        EditList<cell_t> diffs = new EditList<cell_t>(this);

        if (row1.length() == 0) {
            // Just add some text (speedup).
            diffs.append(new RowDifference<T>(DifferenceType.INSERTION, row2));
            return diffs;
        }

        if (row2.length() == 0) {
            // Just delete some text (speedup).
            diffs.append(new RowDifference<T>(DifferenceType.REMOVAL, row1));
            return diffs;
        }

        IRow<T> longtext = row1.length() > row2.length() ? row1 : row2;
        IRow<T> shorttext = row1.length() > row2.length() ? row2 : row1;
        int substr = longtext.indexOf(shorttext);
        if (substr != -1) {
            // Shorter text is inside the longer text (speedup).
            DifferenceType op = (row1.length() > row2.length()) ? DifferenceType.REMOVAL : DifferenceType.INSERTION;
            diffs.append(new RowDifference<T>(op, longtext.slice(0, substr)));
            diffs.append(new RowDifference<T>(DifferenceType.MATCH, shorttext));
            diffs.append(new RowDifference<T>(op, longtext.slice(substr + shorttext.length(), longtext.length())));
            return diffs;
        }

        if (shorttext.length() == 1) {
            // Single character string.
            // After the previous speedup, the character can't be an equality.
            diffs.append(new RowDifference<T>(DifferenceType.REMOVAL, row1));
            diffs.append(new RowDifference<T>(DifferenceType.INSERTION, row2));
            return diffs;
        }

        // Check to see if the problem can be split in two.
        HalfMatch<T> hm = RowUtils.halfMatch(config, row1, row2);
        if (hm != null) {
            // A half-match was found, sort out the return data.
            IRow<T> prefix1 = hm.prefix1;
            IRow<T> suffix1 = hm.suffix1;
            IRow<T> prefix2 = hm.prefix2;
            IRow<T> suffix2 = hm.suffix2;
            IRow<T> common = hm.common;
            // Send both pairs off for separate processing.
            EditList<cell_t> prefixDiffs = _compare(prefix1, prefix2, packing, deadline);
            EditList<cell_t> suffixDiffs = _compare(suffix1, suffix2, packing, deadline);
            // Merge the results.
            diffs = prefixDiffs;
            diffs.append(new RowDifference<T>(DifferenceType.MATCH, common));
            diffs.addAll(suffixDiffs);
            return diffs;
        }

        if (packing && row1.length() > 100 && row2.length() > 100) {
            return _compareByPack(row1, row2, deadline);
        }

        return _bisect(row1, row2, deadline);
    }

    /**
     * Do a quick line-level diff on both strings, then rediff the parts for greater accuracy. This
     * speedup can produce non-minimal diffs.
     *
     * @param row1
     *            Old string to be diffed.
     * @param row2
     *            New string to be diffed.
     * @param deadline
     *            Time when the diff should be complete by.
     * @return Linked List of Diff objects.
     */
    private <T extends cell_t> EditList<cell_t> _compareByPack(IRow<T> row1, IRow<T> row2, long deadline) {
        // Scan the text on a line-by-line basis first.
        LinesToCharsResult<T> a = packer.pack(row1, row2);
        List<IRow<T>> linearray = a.lineArray;

        IntCharsComparator atomsComparator = new IntCharsComparator(config);
        EditList<Integer> atomDiffs = //
                atomsComparator._compare(a.chars1, a.chars2, false, deadline);

        // Convert the diff back to original text.
        EditList<cell_t> diffs = packer.unpack(atomDiffs, linearray);
        // Eliminate freak matches (e.g. blank lines)
        diffs.cleanupSemantic();

        // Rediff any replacement blocks, this time character-by-character.
        // Add a dummy entry at the end.
        diffs.append(new RowDifference<T>(DifferenceType.MATCH, Rows.<T> empty()));
        int count_delete = 0;
        int count_insert = 0;
        MutableRow<cell_t> text_delete = new MutableRow<cell_t>();
        MutableRow<cell_t> text_insert = new MutableRow<cell_t>();
        ListIterator<RowEdit<cell_t>> pointer = diffs.listIterator();
        RowEdit<cell_t> thisDiff = pointer.next();
        while (thisDiff != null) {
            switch (thisDiff.type) {
            case INSERTION:
                count_insert++;
                text_insert.append(thisDiff.row);
                break;
            case REMOVAL:
                count_delete++;
                text_delete.append(thisDiff.row);
                break;
            case MATCH:
                // Upon reaching an equality, check for prior redundancies.
                if (count_delete >= 1 && count_insert >= 1) {
                    // Delete the offending records and add the merged ones.
                    pointer.previous();
                    for (int j = 0; j < count_delete + count_insert; j++) {
                        pointer.previous();
                        pointer.remove();
                    }
                    for (RowEdit<cell_t> subDiff : _compare(text_delete, text_insert, false, deadline)) {
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
     * @param row1
     *            Old string to be diffed.
     * @param row2
     *            New string to be diffed.
     * @param deadline
     *            Time at which to bail if not yet complete.
     * @return LinkedList of Diff objects.
     */
    <T extends cell_t> EditList<cell_t> _bisect(IRow<T> row1, IRow<T> row2, long deadline) {
        // Cache the text lengths to prevent multiple calls.
        int row1_length = row1.length();
        int row2_length = row2.length();
        int max_d = (row1_length + row2_length + 1) / 2;
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
        int delta = row1_length - row2_length;
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
                while (x1 < row1_length && y1 < row2_length && Nullables.equals(row1.cellAt(x1), row2.cellAt(y1))) {
                    x1++;
                    y1++;
                }
                v1[k1_offset] = x1;
                if (x1 > row1_length) {
                    // Ran off the right of the graph.
                    k1end += 2;
                } else if (y1 > row2_length) {
                    // Ran off the bottom of the graph.
                    k1start += 2;
                } else if (front) {
                    int k2_offset = v_offset + delta - k1;
                    if (k2_offset >= 0 && k2_offset < v_length && v2[k2_offset] != -1) {
                        // Mirror x2 onto top-left coordinate system.
                        int x2 = row1_length - v2[k2_offset];
                        if (x1 >= x2) {
                            // Overlap detected.
                            return _bisectSplit(row1, row2, x1, y1, deadline);
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
                while (x2 < row1_length && y2 < row2_length
                        && Nullables.equals(row1.cellAt(row1_length - x2 - 1), row2.cellAt(row2_length - y2 - 1))) {
                    x2++;
                    y2++;
                }
                v2[k2_offset] = x2;
                if (x2 > row1_length) {
                    // Ran off the left of the graph.
                    k2end += 2;
                } else if (y2 > row2_length) {
                    // Ran off the top of the graph.
                    k2start += 2;
                } else if (!front) {
                    int k1_offset = v_offset + delta - k2;
                    if (k1_offset >= 0 && k1_offset < v_length && v1[k1_offset] != -1) {
                        int x1 = v1[k1_offset];
                        int y1 = v_offset + x1 - k1_offset;
                        // Mirror x2 onto top-left coordinate system.
                        x2 = row1_length - x2;
                        if (x1 >= x2) {
                            // Overlap detected.
                            return _bisectSplit(row1, row2, x1, y1, deadline);
                        }
                    }
                }
            }
        }
        // Diff took too long and hit the deadline or
        // number of diffs equals number of characters, no commonality at all.
        EditList<cell_t> diffs = new EditList<cell_t>(this);
        diffs.append(new RowDifference<T>(DifferenceType.REMOVAL, row1));
        diffs.append(new RowDifference<T>(DifferenceType.INSERTION, row2));
        return diffs;
    }

    /**
     * Given the location of the 'middle snake', split the diff in two parts and recurse.
     *
     * @param row1
     *            Old string to be diffed.
     * @param row2
     *            New string to be diffed.
     * @param x
     *            Index of split point in row1.
     * @param y
     *            Index of split point in row2.
     * @param deadline
     *            Time at which to bail if not yet complete.
     * @return LinkedList of Diff objects.
     */
    private <T extends cell_t> EditList<cell_t> _bisectSplit(IRow<T> row1, IRow<T> row2, int x, int y, long deadline) {
        IRow<T> left1 = row1.slice(0, x);
        IRow<T> left2 = row2.slice(0, y);
        IRow<T> right1 = row1.slice(x, row1.length());
        IRow<T> right2 = row2.slice(y, row2.length());

        // Compute both diffs serially.
        EditList<cell_t> leftDiffs = _compare(left1, left2, false, deadline);
        EditList<cell_t> rightDiffs = _compare(right1, right2, false, deadline);

        leftDiffs.addAll(rightDiffs);
        return leftDiffs;
    }

    public RowMatcher<cell_t> matcher() {
        return rowMatcher;
    }

}
