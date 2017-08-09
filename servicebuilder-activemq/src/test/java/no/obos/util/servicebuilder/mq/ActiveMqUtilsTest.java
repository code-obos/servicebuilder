package no.obos.util.servicebuilder.mq;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActiveMqUtilsTest {

    @Test
    public void brokerUrl_adds_auto_reconnection() {
        assertThat(ActiveMqUtils.brokerUrl("url")).isEqualTo("failover:url");
    }

    @Test
    public void brokerUrl_has_already_auto_reconnection() {
        assertThat(ActiveMqUtils.brokerUrl("failover:url")).isEqualTo("failover:url");
    }

    @Test
    public void brokerUrl_junit_config() {
        assertThat(ActiveMqUtils.brokerUrl("vm://localhost?broker.persistent=false"))
                .isEqualTo("vm://localhost?broker.persistent=false");
    }

}