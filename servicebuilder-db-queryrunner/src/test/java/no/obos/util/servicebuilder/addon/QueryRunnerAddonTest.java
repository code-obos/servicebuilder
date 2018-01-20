package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.queryrunner.QueryRunnerAdapter;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;

import static no.obos.util.servicebuilder.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static no.obos.util.servicebuilder.addon.H2InMemoryDatasourceAddon.h2InMemoryDatasourceAddon;
import static no.obos.util.servicebuilder.addon.QueryRunnerAddon.queryRunnerAddon;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryRunnerAddonTest {

    final ServiceConfig serviceConfig = ServiceConfig.defaults(ServiceDefinitionUtil.simple(Api.class))
            .addon(exceptionMapperAddon)
            .addon(h2InMemoryDatasourceAddon
                    .script("CREATE TABLE testable (id INTEGER, name VARCHAR);")
                    .insert("testable", 101, "'Per'")
                    .insert("testable", 303, "'Espen'")
                    .script("INSERT INTO testable VALUES (202, 'Per');")
            )
            .addon(queryRunnerAddon);

    @Test
    public void runsWithQueryRunner() {
        ServiceConfig serviceConfig = this.serviceConfig
                .bind(ApiImpl.class, Api.class);
        Integer actual = TestServiceRunner.defaults(serviceConfig).oneShot(Api.class, Api::get);
        assertThat(actual).isEqualTo(303);
    }

    @Test
    public void runsWithQueryRunnerAdapter() {
        ServiceConfig serviceConfig = this.serviceConfig
                .bind(ApiImplAdapter.class, Api.class);
        Integer actual = TestServiceRunner.defaults(serviceConfig).oneShot(Api.class, Api::get);
        assertThat(actual).isEqualTo(303);
    }


    public @Path("")
    interface Api {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        Integer get();
    }


    public static class ApiImpl implements Api {
        @Inject
        QueryRunner queryRunner;

        public Integer get() {
            try {
                return queryRunner.query("SELECT * FROM testable WHERE name = 'Espen'", rs -> {
                            rs.next();
                            try {
                                return rs.getInt("id");
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                );
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    public static class ApiImplAdapter implements Api {
        @Inject
        QueryRunnerAdapter queryRunner;

        public Integer get() {
            return queryRunner.query("SELECT * FROM testable WHERE name = 'Espen'", rs -> {
                        rs.next();
                        try {
                            return rs.getInt("id");
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
            );
        }
    }
}
