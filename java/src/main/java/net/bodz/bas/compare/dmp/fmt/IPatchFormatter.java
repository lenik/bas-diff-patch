package net.bodz.bas.compare.dmp.fmt;

import java.io.IOException;

import net.bodz.bas.compare.dmp.Patch;

public interface IPatchFormatter<cell_t> {

    void format(Appendable out, Patch<cell_t> patch)
            throws IOException;

    String format(Patch<cell_t> patch)
            throws IOException;

}
