package net.bodz.bas.compare.dmp;

public class PatchApplyStatus<cell_t> {

    public Patch<cell_t> patch;
    public MatchState state;

    public PatchApplyStatus(Patch<cell_t> patch, MatchState state) {
        this.patch = patch;
        this.state = state;
    }

    public boolean isError() {
        return state.isError();
    }

}
