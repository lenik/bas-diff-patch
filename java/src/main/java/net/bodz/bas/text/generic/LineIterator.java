package net.bodz.bas.text.generic;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LineIterator
        implements
            Iterator<String> {

    private String s;
    private int start;
    private int end;

    private int nextStart = 0;
    private int nextEol = -1;

    private boolean preserveEOL = true;
    private boolean lastLineHasEOL;
    private boolean lastLineHasCRLF;

    private boolean returnLastBlankLine = false;

    private int index;

    public LineIterator(String s) {
        this.s = s;
        this.start = 0;
        this.end = s.length();
    }

    public LineIterator(String s, int start, int end) {
        this.s = s;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        if (nextStart == -1)
            return false; // EOF

        if (nextStart >= nextEol) {
            nextEol = s.indexOf('\n', nextEol + 1);
            if (nextEol == -1)
                nextEol = end;
            else {
                nextEol++;
                if (nextEol > end)
                    nextEol = end;
            }
            if (nextEol == nextStart)
                if (returnLastBlankLine)
                    returnLastBlankLine = false;
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
            lastLineHasEOL = s.charAt(nextEol - 1) == '\n';
        else
            lastLineHasEOL = false;
        lastLineHasCRLF = false;

        int lineEnd = nextEol;
        if (!preserveEOL)
            if (lastLineHasEOL) {
                lineEnd--;
                if (lineEnd - 1 >= start && s.charAt(lineEnd - 1) == '\r') {
                    lastLineHasCRLF = true;
                    lineEnd--;
                }
            }

        String line = s.substring(nextStart, lineEnd);

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

    public static class Builder
            extends MultiLineOptions<Builder> {

        public Builder() {
            super();
        }

        public Builder(MultiLineOptions<?> o) {
            super(o);
        }

        public LineIterator build() {
            LineIterator iterator = new LineIterator(text, start, end);
            iterator.preserveEOL = preserveEOL;
            iterator.returnLastBlankLine = returnLastBlankLine;
            return iterator;
        }

    }

}
