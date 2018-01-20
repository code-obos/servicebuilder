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

import static no.obos.util.servicebuilder.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static no.obos.util.servicebuilder.addon.H2InMemoryDatasourceAddon.h2InMemoryDatasourceAddon;
import static no.obos.util.servicebuilder.addon.JdbiAddon.jdbiAddon;
import static org.assertj.core.api.Assertions.assertThat;

public class JdbiAddonTest {


    ServiceConfig serviceConfig = ServiceConfig.defaults(ServiceDefinitionUtil.simple(Api.class))
            .addon(exceptionMapperAddon)
            .addon(h2InMemoryDatasourceAddon.name("Banan")
                    .script("CREATE TABLE testable (id INTEGER, name VARCHAR);")
                    .insert("testable", 101, "'Per'")
                    .insert("testable", 303, "'Espen'")
                    .script("INSERT INTO testable VALUES (202, 'Per');")
            )
            .addon(jdbiAddon.dao(JdbiDto.class).name("Banan"))
            .bind(ApiImpl.class, Api.class);


    @Test
    public void runsWithJdbi() {
        List<Integer> expected = Lists.newArrayList(101, 202);
        List<Integer> actual = TestServiceRunner.defaults(serviceConfig)
                .oneShot(Api.class, Api::get);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testChainExample() {
        List<Integer> expected = Lists.newArrayList(101, 202);
        TestServiceRunner.defaults(serviceConfig)
                .chain()
                .call(Api.class, Api::get)
                .addonNamed(addon_name, JdbiAddon.class, it -> {
                    List<Integer> actual = it.createDao(JdbiDto.class).doGet("Per");
                    assertThat(actual).isEqualTo(expected);
                })
                .run();
    }

    public interface JdbiDto {

        @SqlQuery("SELECT\n"
                + "  id\n"
                + "FROM testable \n"
                + "WHERE name = :param\n")
        List<Integer> doGet(@Bind("param") String param);
    }


    public @Path("")
    interface Api {
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


    static final String addon_name = "Banan";
}
