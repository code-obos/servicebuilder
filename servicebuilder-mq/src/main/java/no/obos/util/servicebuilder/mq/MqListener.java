package no.obos.util.servicebuilder.mq;

public interface MqListener {
    /**
     * Only do this once, and before starting listener threads. If not the dark gods of multithreading will bring horrible
     * agony on you and your children and your childrens children.
     */
    void setHandlers(Iterable<MqHandlerImpl<?>> handlers);

    void startListener();
}
