package net.bodz.bas.compare.dmp.fmt;

import java.io.IOException;
import java.io.StringWriter;

import net.bodz.bas.compare.dmp.Patch;

public abstract class AbstractDiffListFormatter<cell_t>
        implements
            IPatchFormatter<cell_t> {

    @Override
    public String format(Patch<cell_t> patch)
            throws IOException {
        StringWriter buf = new StringWriter(4000);
        try {
            format(buf, patch);
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }
        return buf.toString();
    }

}
