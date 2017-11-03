package no.obos.util.servicebuilder;

public interface TestServiceRunnerBase {
    TestServiceRunnerBase withStartedRuntime();

    TestServiceRunnerBase serviceConfig(ServiceConfig serviceConfig);

    ServiceConfig getServiceConfig();

    TestRuntime getRuntime();
}
