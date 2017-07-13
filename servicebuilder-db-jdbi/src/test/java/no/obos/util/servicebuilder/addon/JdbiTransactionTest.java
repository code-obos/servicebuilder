package no.obos.util.servicebuilder.addon;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
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

public class JdbiTransactionTest {
    @Test
    public void happyPath() {
        List<Integer> expected = Lists.newArrayList(101, 202);
        ServiceConfig serviceConfig = ServiceConfig.defaults(ServiceDefinitionUtil.simple(Api.class))
                .addon(ExceptionMapperAddon.defaults)
                .addon(H2InMemoryDatasourceAddon.defaults.name("Banan")
                        .script("CREATE TABLE testable (id INTEGER, name VARCHAR);")
                        .insert("testable", 101, "'Per'")
                        .insert("testable", 303, "'Espen'")
                        .script("INSERT INTO testable VALUES (202, 'Per');")
                )
                .addon(JdbiAddon.defaults.dao(JdbiDto.class).name("Banan"))
                .bind(ApiImpl.class, Api.class);
        List<Integer> actual = TestServiceRunner.defaults(serviceConfig).oneShot(Api.class, Api::get);
        assertThat(actual).isEqualTo(expected);
    }

    public interface JdbiDto {

        @SqlQuery("DELETE\n"
                + "FROM testable\n"
                + "where  name = :param")
        Integer doDelete(@Bind("param") String param);

        @SqlQuery("SELECT sum(id)\n"
                + "FROM testable")
        Integer doGet();
    }

    public interface Controller {
        Response doTransactionStruff get();
    }

    public @Path("") interface Api {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        Integer get();
    }


    public static class ApiImpl implements Api {
        @Inject
        JdbiDto jdbiDto;

        public Integer get() {
            return jdbiDto.doGet();
        }
    }
}
