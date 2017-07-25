package no.obos.util.servicebuilder.mq.addon;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.ActiveMqAddon;
import no.obos.util.servicebuilder.addon.MqAddon;
import no.obos.util.servicebuilder.addon.ObosLogFilterAddon;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.MessageHandler;
import no.obos.util.servicebuilder.mq.MessageMeta;
import no.obos.util.servicebuilder.mq.MqSender;
import org.apache.activemq.broker.BrokerService;
import org.glassfish.hk2.api.TypeLiteral;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ActiveMqAddonSendReceiveTest {

    public static final String TCP_LOCALHOST_61616 = "tcp://localhost:61616";

    @Test
    public void sendAndReceiveMessage() {
        BrokerService broker = null;
        boolean fail = false;
        try {
            broker = new BrokerService();

            // configure the broker
            try {
                broker.addConnector(TCP_LOCALHOST_61616);
                broker.setPersistent(false);
                broker.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            MyMessageV1 expected = new MyMessageV1(LocalDate.now(), "brillefin");
            ArgumentCaptor<MyMessageV1> myMessageV1ArgumentCator = ArgumentCaptor.forClass(MyMessageV1.class);
            ArgumentCaptor<MessageMeta> messageMetaArgumentCaptor = ArgumentCaptor.forClass(MessageMeta.class);
            TestServiceRunner.defaults(serviceConfig)
                    .oneShotVoid(MyResource.class, it -> it.addToQueue(expected));
            verify(messageHandler).handle(myMessageV1ArgumentCator.capture(), messageMetaArgumentCaptor.capture());

            MyMessageV1 actual = myMessageV1ArgumentCator.getValue();
            MessageMeta meta = messageMetaArgumentCaptor.getValue();

            assertThat(actual).isEqualTo(expected);
            assertThat(meta.sourceApp).isEqualTo(MyServiceDefinition.instance.getName());
            assertThat(meta.requestId).isNotEmpty();
        } finally {
            if (broker != null) {
                try {
                    broker.stop();
                } catch (Exception e) {
                    System.out.println(e.toString());
                    fail = true;
                }
            }
        }
        if(fail) {
            Assert.fail();
        }
    }

    final MyHandler messageHandler = mock(MyHandler.class);

    ServiceConfig serviceConfig = ServiceConfig.defaults(MyServiceDefinition.instance)
            .addon(MqAddon.defaults
                    .listen(MyServiceDefinition.myMessageV1, MyHandler.class)
                    .send(MyServiceDefinition.myMessageV1, new TypeLiteral<MqSender<MyMessageV1>>() {})
            )
            .addon(ActiveMqAddon.defaults
                    .url(TCP_LOCALHOST_61616)
            )
            .addon(ObosLogFilterAddon.defaults)
            .bind(messageHandler, MyHandler.class)
            .bind(MyResourceImpl.class, MyResource.class);


    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    @Getter
    @Setter
    public static class MyMessageV1 {
        public LocalDate time;
        public String string;
    }


    public interface MyHandler extends MessageHandler<MyMessageV1> {
    }


    @Path("banan")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public interface MyResource {
        @POST
        void addToQueue(MyMessageV1 messageV1);
    }


    public static class MyResourceImpl implements MyResource {
        final MqSender<MyMessageV1> myMessageV1MqSender;

        @Inject
        MyResourceImpl(MqSender<MyMessageV1> myMessageV1MqSender) {
            this.myMessageV1MqSender = myMessageV1MqSender;
        }



        public void addToQueue(MyMessageV1 messageV1) {
            MDC.put(Constants.X_OBOS_REQUEST_ID, UUID.randomUUID().toString());
            myMessageV1MqSender.send(messageV1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MDC.remove(Constants.X_OBOS_REQUEST_ID);
        }
    }


    @Getter
    static class MyServiceDefinition implements ServiceDefinition {
        final String name = "Banan";
        final ImmutableList<MessageDescription> handledMessages = ImmutableList.of(myMessageV1);
        final ImmutableList resources = ImmutableList.of(MyResource.class);

        public final static MessageDescription<MyMessageV1> myMessageV1 = MessageDescription.<MyMessageV1>builder()
                .description("For test")
                .MessageType(MyMessageV1.class)
                .name("myMessage")
                .version("1.0")
                .build();

        final static MyServiceDefinition instance = new MyServiceDefinition();
    }

}
