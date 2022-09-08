package net.bodz.bas.compare.dmp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.bodz.bas.text.row.CharsView;
import net.bodz.bas.text.row.IRow;

public class diff_match_patch_compat {

    public DMPConfig config = new DMPConfig();
    CharsComparator dmp = new CharsComparator(config);

    _Diff conv(IRowDifference<?> diff) {
        return new _Diff(diff.getDifferenceType(), diff.getTextAsString());
    }

    RowDifference<Character> convR(_Diff diff) {
        CharsView text = convText(diff.text);
        return new RowDifference<Character>(diff.type, text);
    }

    LinkedList<_Diff> convDiffs(IDiffList<? extends IRowDifference<Character>, ?> o) {
        if (o == null)
            return null;
        LinkedList<_Diff> list = new LinkedList<_Diff>();
        for (IRowDifference<Character> item : o)
            list.add(conv(item));
        return list;
    }

    EditList<Character> convDiffsR(List<_Diff> o) {
        if (o == null)
            return null;
        EditList<Character> list = new EditList<Character>(dmp);
        for (_Diff item : o)
            list.append(convR(item));
        return list;
    }

    _Patch conv(Patch<Character> patch) {
        if (patch == null)
            return null;
        _Patch a = new _Patch();
        a.diffs = convDiffs(patch.diffs);
        a.length1 = patch.length1;
        a.length2 = patch.length2;
        a.start1 = patch.start1;
        a.start2 = patch.start2;
        return a;
    }

    Patch<Character> convR(_Patch patch) {
        if (patch == null)
            return null;
        Patch<Character> a = new Patch<Character>(dmp);
        a.diffs = convDiffsR(patch.diffs);
        a.length1 = patch.length1;
        a.length2 = patch.length2;
        a.start1 = patch.start1;
        a.start2 = patch.start2;
        return a;
    }

    LinkedList<_Patch> convPatches(List<Patch<Character>> o) {
        if (o == null)
            return null;
        LinkedList<_Patch> list = new LinkedList<_Patch>();
        for (Patch<Character> item : o)
            list.add(conv(item));
        return list;
    }

    PatchList<Character> convPatchesR(List<_Patch> o) {
        if (o == null)
            return null;
        PatchList<Character> list = new PatchList<Character>(dmp);
        for (_Patch item : o)
            list.add(convR(item));
        return list;
    }

    CharsView convText(String text) {
        return text == null ? null : new CharsView(text);
    }

    String convText(CharsView text) {
        return text == null ? null : dmp.format(text);
    }

    List<String> convText(List<CharsView> text) {
        if (text == null)
            return null;
        List<String> list = new ArrayList<String>();
        for (CharsView s : text)
            list.add(convText(s));
        return list;
    }

    List<CharsView> convTextR(List<String> text) {
        if (text == null)
            return null;
        List<CharsView> list = new ArrayList<CharsView>();
        for (String s : text)
            list.add(convText(s));
        return list;
    }

    public LinkedList<_Diff> diff_main(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        return convDiffs(dmp.compareByPack(text1, text2));
    }

    public LinkedList<_Diff> diff_main(String _text1, String _text2, boolean checklines) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        if (checklines)
            return convDiffs(dmp.compareByPack(text1, text2));
        else
            return convDiffs(dmp.compare(text1, text2));
    }

    public LinkedList<_Diff> diff_bisect(String _text1, String _text2, long deadline) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        return convDiffs(dmp._bisect(text1, text2, deadline));
    }

    IntCharsComparator INT_CHARS = new IntCharsComparator(config);

    protected _LinesToCharsResult diff_linesToChars(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        PackedRows<Character> pack = dmp.rowPacker.pack(text1, text2);
        List<String> lineArray = new ArrayList<String>();
        for (IRow<Character> line : pack.packArray)
            lineArray.add(dmp.format(line));

        return new _LinesToCharsResult( //
                INT_CHARS.format(pack.getIndexRow1()), //
                INT_CHARS.format(pack.getIndexRow2()), //
                lineArray);
    }

    public int diff_commonPrefix(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        return RowUtils.commonPrefix(text1, text2);
    }

    public int diff_commonSuffix(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        return RowUtils.commonSuffix(text1, text2);
    }

    protected int diff_commonOverlap(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        return RowUtils.commonOverlap(text1, text2);
    }

    protected String[] diff_halfMatch(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        HalfMatch<Character> hm = RowUtils.halfMatch(config, text1, text2);
        return hm == null ? null
                : new String[] { //
                        dmp.format(hm.prefix1), //
                        dmp.format(hm.suffix1), //
                        dmp.format(hm.prefix2), //
                        dmp.format(hm.suffix2), //
                        dmp.format(hm.common), //
                };
    }

    public String diff_text1(List<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        return dmp.format(diffs.restoreRow1());
    }

    public String diff_text2(List<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        return dmp.format(diffs.restoreRow2());
    }

    public LinkedList<_Diff> diff_fromDelta(String _text1, String delta)
            throws IllegalArgumentException {
        CharsView text1 = convText(_text1);
        return convDiffs(EditList.fromDelta(dmp, text1, delta));
    }

    public int match_main(String _text, String _pattern, int loc) {
        CharsView text = convText(_text);
        CharsView pattern = convText(_pattern);
        return dmp.matcher().search(text, pattern, loc);
    }

    protected int match_bitap(String _text, String _pattern, int loc) {
        CharsView text = convText(_text);
        CharsView pattern = convText(_pattern);
        return dmp.matcher().bitap(text, pattern, loc);
    }

    protected Map<Character, Integer> match_alphabet(String _pattern) {
        CharsView pattern = convText(_pattern);
        return dmp.matcher().alphabet(pattern);
    }

    protected void patch_addContext(_Patch _patch, String _text) {
        CharsView text = convText(_text);
        Patch<Character> patch = convR(_patch);
        patch.addContext(text);
        _Patch _patch2 = conv(patch);
        _patch.init(_patch2);
    }

    public LinkedList<_Patch> patch_make(String _text1, String _text2) {
        CharsView text1 = convText(_text1);
        CharsView text2 = convText(_text2);
        return convPatches(dmp.compare(text1, text2).createPatch(text1));
    }

    public LinkedList<_Patch> patch_make(String _text1, LinkedList<_Diff> _diffs) {
        CharsView text1 = convText(_text1);
        EditList<Character> diffs = convDiffsR(_diffs);
        PatchList<Character> patches = diffs.createPatch(text1);
        return convPatches(patches);
    }

    public Object[] patch_apply(LinkedList<_Patch> patches, String _text) {
        CharsView text = convText(_text);
        PatchApplyResult<Character> result = convPatchesR(patches).apply(text);
        boolean[] bools = new boolean[result.size()];
        for (int i = 0; i < bools.length; i++)
            bools[i] = !result.get(i).isError();
        return new Object[] { dmp.format(result.patchedRow), bools };
    }

    public String patch_addPadding(LinkedList<_Patch> _patches) {
        PatchList<Character> patches = convPatchesR(_patches);
        IRow<Character> retval = patches.addPadding();
        LinkedList<_Patch> _patches2 = convPatches(patches);
        _patches.clear();
        _patches.addAll(_patches2);
        return dmp.format(retval);
    }

    protected static class _LinesToCharsResult {
        protected String chars1;
        protected String chars2;
        protected List<String> lineArray;

        protected _LinesToCharsResult(String chars1, String chars2, List<String> lineArray) {
            this.chars1 = chars1;
            this.chars2 = chars2;
            this.lineArray = lineArray;
        }
    }

    /**
     * Class representing one diff type.
     */
    public static class _Diff {

        /**
         * One of: INSERT, DELETE or EQUAL.
         */
        public DifferenceType type;
        /**
         * The text associated with this diff type.
         */
        public String text;

        public boolean atom;

        /**
         * Constructor. Initializes the diff with the provided values.
         *
         * @param type
         *            One of INSERT, DELETE or EQUAL.
         * @param text
         *            The text being applied.
         */
        public _Diff(DifferenceType type, String text) {
            // Construct a diff with the specified type and text.
            this.type = type;
            this.text = text;
        }

        /**
         * Display a human-readable version of this Diff.
         *
         * @return text version.
         */
        @Override
        public String toString() {
            String prettyText = text.replace('\n', '\u00b6');
            return "Diff(" + this.type + ",\"" + prettyText + "\")";
        }

        /**
         * Create a numeric hash value for a Diff. This function is not used by DMP.
         *
         * @return Hash value.
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = (type == null) ? 0 : type.hashCode();
            result += prime * ((text == null) ? 0 : text.hashCode());
            return result;
        }

        /**
         * Is this Diff equivalent to another Diff?
         *
         * @param obj
         *            Another Diff to compare against.
         * @return true or false.
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            _Diff other = (_Diff) obj;
            if (type != other.type) {
                return false;
            }
            if (text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!text.equals(other.text)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Class representing one patch type.
     */
    public static class _Patch {
        public LinkedList<_Diff> diffs;
        public int start1;
        public int start2;
        public int length1;
        public int length2;

        /**
         * Constructor. Initializes with an empty list of diffs.
         */
        public _Patch() {
            this.diffs = new LinkedList<_Diff>();
        }

        void init(_Patch o) {
            this.diffs = o.diffs;
            this.start1 = o.start1;
            this.start2 = o.start2;
            this.length1 = o.length1;
            this.length2 = o.length2;
        }

        /**
         * Emulate GNU diff's format. Header: @@ -382,8 +481,9 @@ Indices are printed as 1-based,
         * not 0-based.
         *
         * @return The GNU diff string.
         */
        @Override
        public String toString() {
            String coords1, coords2;
            if (this.length1 == 0) {
                coords1 = this.start1 + ",0";
            } else if (this.length1 == 1) {
                coords1 = Integer.toString(this.start1 + 1);
            } else {
                coords1 = (this.start1 + 1) + "," + this.length1;
            }
            if (this.length2 == 0) {
                coords2 = this.start2 + ",0";
            } else if (this.length2 == 1) {
                coords2 = Integer.toString(this.start2 + 1);
            } else {
                coords2 = (this.start2 + 1) + "," + this.length2;
            }
            StringBuilder text = new StringBuilder();
            text.append("@@ -").append(coords1).append(" +").append(coords2).append(" @@\n");
            // Escape the body of the patch with %xx notation.
            for (_Diff aDiff : this.diffs) {
                switch (aDiff.type) {
                case INSERTION:
                    text.append('+');
                    break;
                case REMOVAL:
                    text.append('-');
                    break;
                case MATCH:
                    text.append(' ');
                    break;
                }
                text.append(JsCompat.encodeUri(aDiff.text)).append("\n");
            }
            return text.toString();
        }
    }

    protected void diff_charsToLines(List<_Diff> _diffs, List<String> _lineArray) {
        for (_Diff diff : _diffs) {
            StringBuilder text = new StringBuilder();
            for (int j = 0; j < diff.text.length(); j++) {
                int ch = diff.text.charAt(j);
                text.append(_lineArray.get(ch));
            }
            diff.text = text.toString();
        }
    }

    public void diff_cleanupSemantic(LinkedList<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        diffs.cleanupSemantic();
        LinkedList<_Diff> _diffs2 = convDiffs(diffs);
        _diffs.clear();
        _diffs.addAll(_diffs2);
    }

    public void diff_cleanupSemanticLossless(LinkedList<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        diffs.cleanupSemanticLossless();
        LinkedList<_Diff> _diffs2 = convDiffs(diffs);
        _diffs.clear();
        _diffs.addAll(_diffs2);
    }

    public void diff_cleanupEfficiency(LinkedList<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        diffs.cleanupEfficiency();
        LinkedList<_Diff> _diffs2 = convDiffs(diffs);
        _diffs.clear();
        _diffs.addAll(_diffs2);
    }

    public void diff_cleanupMerge(LinkedList<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        diffs.cleanupMerge();
        LinkedList<_Diff> _diffs2 = convDiffs(diffs);
        _diffs.clear();
        _diffs.addAll(_diffs2);
    }

    public int diff_xIndex(List<_Diff> diffs, int loc) {
        return convDiffsR(diffs).xIndex(loc);
    }

    public String diff_prettyHtml(List<_Diff> diffs) {
        return convDiffsR(diffs).prettyHtml();
    }

    public int diff_levenshtein(List<_Diff> diffs) {
        return convDiffsR(diffs).levenshtein();
    }

    public String diff_toDelta(List<_Diff> diffs) {
        return convDiffsR(diffs).toDelta();
    }

    public LinkedList<_Patch> patch_make(LinkedList<_Diff> _diffs) {
        EditList<Character> diffs = convDiffsR(_diffs);
        return convPatches(diffs.createPatch());
    }

    public LinkedList<_Patch> patch_deepCopy(LinkedList<_Patch> patches) {
        return convPatches(convPatchesR(patches).deepCopy());
    }

    public void patch_splitMax(LinkedList<_Patch> _patches) {
        PatchList<Character> patches = convPatchesR(_patches);
        patches.splitMax();
        LinkedList<_Patch> _patches2 = convPatches(patches);
        _patches.clear();
        _patches.addAll(_patches2);
    }

    public String patch_toText(List<_Patch> patches) {
        return convPatchesR(patches).toText();
    }

    public List<_Patch> patch_fromText(String patchcode)
            throws IllegalArgumentException {
        return convPatches(PatchList.fromText(dmp, patchcode));
    }

}
