package no.obos.util.servicebuilder.addon;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.Handle;

import javax.inject.Inject;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TransactJdbi {
    private final Handle handle;

    public <T> T inTransaction(Supplier<T> fun) {
        try {
            handle.begin();
            T t = fun.get();
            handle.commit();
            return t;
        } finally {
            if (handle.isInTransaction()) {
                log.debug("Rolling back transaction");
                handle.rollback();
            }
        }
    }


    public <Y, T> T inTransaction(Function<Y, T> fun, Y y) {
        try {
            handle.begin();
            T t = fun.apply(y);
            handle.commit();
            return t;
        } finally {
            if (handle.isInTransaction()) {
                log.debug("Rolling back transaction");
                handle.rollback();
            }
        }
    }

    public <T> void inTransaction(Consumer<T> fun, T t) {
        try {
            handle.begin();
            fun.accept(t);
            handle.commit();
        } finally {
            if (handle.isInTransaction()) {
                log.debug("Rolling back transaction");
                handle.rollback();
            }
        }
    }

    public <T,U> void inTransaction(BiConsumer<T,U> fun, T t, U u) {
        try {
            handle.begin();
            fun.accept(t, u);
            handle.commit();
        } finally {
            if (handle.isInTransaction()) {
                log.debug("Rolling back transaction");
                handle.rollback();
            }
        }
    }

    public <T,U,R> R inTransaction(BiFunction<T,U, R> fun, T t, U u) {
        try {
            handle.begin();
            R r = fun.apply(t, u);
            handle.commit();
            return r;
        } finally {
            if (handle.isInTransaction()) {
                log.debug("Rolling back transaction");
                handle.rollback();
            }
        }
    }
}
