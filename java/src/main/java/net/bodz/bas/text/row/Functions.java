package net.bodz.bas.text.row;

public interface Functions {

    class Cast<R, T extends R>
            implements
                Function<T, R> {

        @Override
        public R apply(T t) {
            return t;
        }

    }

}
