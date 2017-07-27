package no.obos.util.servicebuilder;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.client.WebTarget;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TestRuntime {
    <T> T call(Function<WebTarget, T> testfun);

    <T, Y> T call(Class<Y> clazz, Function<Y, T> testfun);

    <Y> void callVoid(Class<Y> clazz, Consumer<Y> testfun);

    void callVoid(Consumer<WebTarget> testfun);

    ResourceConfig getResourceConfig();

    void stop();
}
