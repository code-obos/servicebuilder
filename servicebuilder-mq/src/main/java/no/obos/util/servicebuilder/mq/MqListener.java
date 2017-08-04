package no.obos.util.servicebuilder.mq;

import com.google.common.collect.ImmutableSet;

public interface MqListener {
    void startListener(ImmutableSet<MqHandlerImpl<?>> handlers);
}
