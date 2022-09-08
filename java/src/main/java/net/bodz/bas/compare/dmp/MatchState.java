package net.bodz.bas.compare.dmp;

public enum MatchState {

    MATCH,

    NO_MATCH,

    GOOD_MATCH,

    BAD_MATCH,

    ;

    public boolean isError() {
        switch (this) {
        case MATCH:
        case GOOD_MATCH:
            return false;
        default:
            return true;
        }
    }

}
