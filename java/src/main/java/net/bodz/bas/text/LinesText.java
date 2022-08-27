package net.bodz.bas.text;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinesText
        implements
            Iterable<String> {

    final String text;
    final int start;
    final int end;
    final boolean removeEOL;
    final boolean returnLastBlankLine;
    final boolean trimLeft;
    final boolean trimRight;

    public LinesText(String s) {
        this(new Builder().text(s));
    }

    public LinesText(String s, boolean removeEOL) {
        this(new Builder().text(s).removeEOL(removeEOL));
    }

    private LinesText(Builder b) {
        text = b.text;
        start = b.start;
        end = b.end;
        removeEOL = b.removeEOL;
        returnLastBlankLine = b.returnLastBlankLine;
        trimLeft = b.trimLeft;
        trimRight = b.trimRight;
    }

    @Override
    public Iterator<String> iterator() {
        return new LineIterator(this);
    }

    public static class Builder {
        String text;
        int start;
        int end;
        boolean removeEOL;
        boolean returnLastBlankLine;
        boolean trimLeft;
        boolean trimRight;

        public Builder() {
        }

        public Builder(Builder o) {
            text = o.text;
            start = o.start;
            end = o.end;
            removeEOL = o.removeEOL;
            returnLastBlankLine = o.returnLastBlankLine;
        }

        public Builder text(String text) {
            if (text == null)
                throw new NullPointerException("text");
            this.text = text;
            this.start = 0;
            this.end = text.length();
            return this;
        }

        public Builder text(String text, int start, int end) {
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
            return this;
        }

        public Builder removeEOL() {
            this.removeEOL = true;
            return this;
        }

        public Builder removeEOL(boolean preserveEOL) {
            this.removeEOL = preserveEOL;
            return this;
        }

        public Builder returnLastBlankLine() {
            this.returnLastBlankLine = true;
            return this;
        }

        public Builder returnLastBlankLine(boolean returnLastBlankLink) {
            this.returnLastBlankLine = returnLastBlankLink;
            return this;
        }

        public LinesText build() {
            return new LinesText(this);
        }

    }

}

class LineIterator
        implements
            Iterator<String> {

    LinesText text;

    private int nextStart = 0;
    private int nextEol = -1;

    private boolean lastLineHasEOL;
    private boolean lastLineHasCRLF;

    private boolean returnedLastBlankLine;

    private int index;

    public LineIterator(LinesText text) {
        this.text = text;
        this.returnedLastBlankLine = text.returnLastBlankLine;
    }

    @Override
    public boolean hasNext() {
        if (nextStart == -1)
            return false; // EOF

        if (nextStart >= nextEol) {
            nextEol = text.text.indexOf('\n', nextEol + 1);
            if (nextEol == -1)
                nextEol = text.end;
            else {
                nextEol++;
                if (nextEol > text.end)
                    nextEol = text.end;
            }
            if (nextEol == nextStart)
                if (returnedLastBlankLine)
                    returnedLastBlankLine = false;
                else
                    nextStart = nextEol = -1;
        }
        return nextStart != -1;
    }

    @Override
    public String next() {
        if (!hasNext())
            throw new NoSuchElementException();

        if (nextEol > nextStart)
            lastLineHasEOL = text.text.charAt(nextEol - 1) == '\n';
        else
            lastLineHasEOL = false;
        lastLineHasCRLF = false;

        int lineEnd = nextEol;
        if (text.removeEOL)
            if (lastLineHasEOL) {
                lineEnd--;
                if (lineEnd - 1 >= text.start && text.text.charAt(lineEnd - 1) == '\r') {
                    lastLineHasCRLF = true;
                    lineEnd--;
                }
            }

        String line = text.text.substring(nextStart, lineEnd);

        nextStart = nextEol;

        index++;
        return line;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean isLastLineHasEOL() {
        return lastLineHasEOL;
    }

    public boolean isLastLineHasCRLF() {
        return lastLineHasCRLF;
    }

    public int getLineIndex() {
        return index;
    }

    public int getLineNumber() {
        return index + 1;
    }

}
