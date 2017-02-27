package no.obos.util.servicebuilder.addon;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.TestServiceRunner;
import org.junit.Test;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbiAddonTest {
    @Test
    public void runsWithJdbi() {
        List<Integer> expected = Lists.newArrayList(101, 202);
        ServiceConfig serviceConfig = ServiceConfig.defaults(ServiceDefinition.simple(Api.class))
                .addon(ExceptionMapperAddon.defaults)
                .addon(H2InMemoryDatasourceAddon.defaults.withName("Banan")
                        .plusScript("CREATE TABLE testable (id INTEGER, name VARCHAR);")
                        .insert("testable", 101, "'Per'")
                        .insert("testable", 303, "'Espen'")
                        .plusScript("INSERT INTO testable VALUES (202, 'Per');")
                )
                .addon(JdbiAddon.defaults.plusDao(JdbiDto.class).withName("Banan"))
                .bind(ApiImpl.class, Api.class);
        List<Integer> actual = TestServiceRunner.defaults(serviceConfig).oneShot(Api.class, Api::get);
        assertThat(actual).isEqualTo(expected);
    }

    public interface JdbiDto {

        @SqlQuery("SELECT\n"
                + "  id\n"
                + "FROM testable \n"
                + "WHERE name = :param\n")
        List<Integer> doGet(@Bind("param") String param);
    }


    public @Path("") interface Api {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        List<Integer> get();
    }


    public static class ApiImpl implements Api {
        @Inject
        JdbiDto jdbiDto;

        public List<Integer> get() {
            return jdbiDto.doGet("Per");
        }
    }
}
