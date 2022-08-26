package net.bodz.bas.text.generic;

public class MultiLineOptions<this_t> {

    protected String text;
    protected int start;
    protected int end;
    protected boolean preserveEOL = false;
    protected boolean returnLastBlankLine = false;

    @SuppressWarnings("unchecked")
    protected final this_t __this = (this_t) this;

    public MultiLineOptions() {
    }

    public MultiLineOptions(MultiLineOptions<?> o) {
        text = o.text;
        start = o.start;
        end = o.end;
        preserveEOL = o.preserveEOL;
        returnLastBlankLine = o.returnLastBlankLine;
    }

    public this_t text(String text) {
        if (text == null)
            throw new NullPointerException("text");
        this.text = text;
        this.start = 0;
        this.end = text.length();
        return __this;
    }

    public this_t text(String text, int start, int end) {
        if (text == null)
            throw new NullPointerException("text");
        if (start < 0 || start > text.length())
            throw new IndexOutOfBoundsException();
        if (end < 0 || end > text.length())
            throw new IndexOutOfBoundsException();
        if (start > end)
            throw new IllegalArgumentException();
        this.text = text;
        this.start = start;
        this.end = end;
        return __this;
    }

    public this_t preserveEOL() {
        this.preserveEOL = true;
        return __this;
    }

    public this_t preserveEOL(boolean preserveEOL) {
        this.preserveEOL = preserveEOL;
        return __this;
    }

    public this_t returnLastBlankLine() {
        this.returnLastBlankLine = true;
        return __this;
    }

    public this_t returnLastBlankLine(boolean returnLastBlankLink) {
        this.returnLastBlankLine = returnLastBlankLink;
        return __this;
    }

}
