package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.addon.BetweenTestsAddon;
import no.obos.util.servicebuilder.addon.NamedAddon;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestChain {
    @Wither(AccessLevel.PRIVATE)
    final TestServiceRunnerBase serviceRunner;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableList<Action> actions;
    final ServiceLocatorHolder serviceLocator;

    public TestChain(TestServiceRunnerBase serviceRunner) {
        this.actions = ImmutableList.of();
        this.serviceLocator = new ServiceLocatorHolder();

        Feature lifecycleListener = new Feature() {
            @Inject
            public void setServiceLocator(ServiceLocator s) {
                serviceLocator.set(s);
            }

            @Override
            public boolean configure(FeatureContext context) {
                return false;
            }
        };
        this.serviceRunner = serviceRunner
                .serviceConfig(serviceRunner.getServiceConfig()
                        .registerInstance(lifecycleListener)
                );
    }

    public TestChain call(Consumer<WebTarget> testfun) {
        return action(testChain -> testChain.serviceRunner.getRuntime().callVoid(testfun));
    }

    public <Y> TestChain call(Class<Y> clazz, Consumer<Y> testfun) {
        return action(testChain ->
                testChain.serviceRunner.getRuntime().callVoid(clazz, testfun
                ));
    }

    public <T extends Addon> TestChain addon(Class<T> clazz, Consumer<T> fun) {
        return action(testChain -> fun.accept(testChain.serviceRunner.getServiceConfig().addonInstance(clazz)));
    }

    public <T extends NamedAddon> TestChain addonNamed(String name, Class<T> clazz, Consumer<T> fun) {
        return action(testChain -> fun.accept(testChain.serviceRunner.getServiceConfig().addonInstanceNamed(clazz, name)));
    }

    public <T> TestChain injectee(Class<T> clazz, Consumer<T> fun) {
        return action(testChain -> fun.accept(testChain.serviceLocator.get().getService(clazz)));
    }

    public <T> TestChain injectee(TypeLiteral<T> typeLiteral, Consumer<T> fun) {
        return action(testChain -> fun.accept(testChain.serviceLocator.get().getService(typeLiteral.getType())));
    }

    public TestChain serviceConfig(Consumer<ServiceConfig> fun) {
        return action(testChain -> fun.accept(testChain.serviceRunner.getServiceConfig()));
    }

    public TestChain serviceLocator(Consumer<ServiceLocator> fun) {
        return action(testChain -> fun.accept(testChain.serviceLocator.get()));
    }

    private TestChain action(Action action) {
        return this.withActions(GuavaHelper.plus(actions, action));
    }

    private void doActions() {
        actions.forEach(action -> {
                    action.run(this);
                    serviceRunner.getServiceConfig()
                            .addonInstances(BetweenTestsAddon.class)
                            .forEach(BetweenTestsAddon::beforeNextTest);
                }
        );
    }

    public void run() {
        TestServiceRunnerBase startedServiceRunner = serviceRunner.withStartedRuntime();
        TestChain startedChain = this.withServiceRunner(startedServiceRunner);
        try {
            startedChain.doActions();
        } finally {
            startedServiceRunner.getRuntime().stop();
        }
    }

    interface Action {
        void run(TestChain testChain);
    }


    private static class ServiceLocatorHolder {
        private ServiceLocator serviceLocator = null;

        public ServiceLocator get() {
            return serviceLocator;
        }

        public void set(ServiceLocator s) {
            serviceLocator = s;
        }
    }

}
