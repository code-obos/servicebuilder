package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;

import java.util.List;

class TestService implements ServiceDefinition {

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public List<Class> getResources() {
        return Lists.newArrayList();
    }
}
