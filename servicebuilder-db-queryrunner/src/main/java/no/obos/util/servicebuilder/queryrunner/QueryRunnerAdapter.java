package no.obos.util.servicebuilder.queryrunner;

import lombok.AllArgsConstructor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * An adapter to handle all the annoying try catches that comes with use of the QueryRunner
 * http://www.oodesign.com/adapter-pattern.html
 */

@SuppressWarnings("deprecation")
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class QueryRunnerAdapter {

    private final QueryRunner queryRunner;


    public int[] batch(Connection conn, String sql, Object[][] params) {
        return wrap(() -> queryRunner.batch(conn, sql, params));
    }

    public int[] batch(String sql, Object[][] params) {
        return wrap(() -> queryRunner.batch(sql, params));
    }

    @Deprecated
    public <T> T query(Connection conn, String sql, Object param, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.query(conn, sql, param, rsh));
    }

    @Deprecated
    public <T> T query(Connection conn, String sql, Object[] params, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.query(conn, sql, params, rsh));
    }

    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) {
        return wrap(() -> queryRunner.query(conn, sql, rsh, params));
    }

    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.query(conn, sql, rsh));
    }

    @Deprecated
    public <T> T query(String sql, Object param, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.query(sql, param, rsh));
    }

    @Deprecated
    public <T> T query(String sql, Object[] params, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.query(sql, params, rsh));
    }

    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) {
        return wrap(() -> queryRunner.query(sql, rsh, params));
    }

    public <T> T query(String sql, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.query(sql, rsh));
    }

    public int update(Connection conn, String sql) {
        return wrap(() -> queryRunner.update(conn, sql));
    }

    public int update(Connection conn, String sql, Object param) {
        return wrap(() -> queryRunner.update(conn, sql, param));
    }

    public int update(Connection conn, String sql, Object... params) {
        return wrap(() -> queryRunner.update(conn, sql, params));
    }

    public int update(String sql) {
        return wrap(() -> queryRunner.update(sql));
    }

    public int update(String sql, Object param) {
        return wrap(() -> queryRunner.update(sql, param));
    }

    public int update(String sql, Object... params) {
        return wrap(() -> queryRunner.update(sql, params));
    }

    public <T> T insert(String sql, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.insert(sql, rsh));
    }

    public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) {
        return wrap(() -> queryRunner.insert(sql, rsh, params));
    }

    public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh) {
        return wrap(() -> queryRunner.insert(conn, sql, rsh));
    }

    public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) {
        return wrap(() -> queryRunner.insert(conn, sql, rsh, params));
    }

    public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) {
        return wrap(() -> queryRunner.insertBatch(sql, rsh, params));
    }

    public <T> T insertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, Object[][] params) {
        return wrap(() -> queryRunner.insertBatch(conn, sql, rsh, params));
    }

    private <T> T wrap(Wrapped<T> fun) {
        try {
            return fun.run();
        } catch (SQLException e) {
            throw new QueryRunnerException(e);
        }
    }

    private interface Wrapped<T> {
        T run() throws SQLException;
    }
}
