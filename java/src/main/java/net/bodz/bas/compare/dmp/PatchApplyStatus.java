package net.bodz.bas.compare.dmp;

public class PatchApplyStatus<cell_t> {

    public Patch<cell_t> patch;
    public MatchStatus status;

    public PatchApplyStatus(Patch<cell_t> patch, MatchStatus status) {
        this.patch = patch;
        this.status = status;
    }

    public boolean isError() {
        switch (status) {
        case BAD_MATCH:
        case NO_MATCH:
            return true;
        default:
        }
        return false;
    }

}
