package no.obos.util.servicebuilder.mq.mock;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.MqAddon;
import no.obos.util.servicebuilder.addon.MqMockAddon;
import no.obos.util.servicebuilder.addon.ObosLogFilterAddon;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.model.MessageHandler;
import no.obos.util.servicebuilder.model.MessageSender;
import org.junit.Test;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static no.obos.util.servicebuilder.mq.mock.MockMqSendReceiveTest.MyServiceDefinition.MY_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Slf4j
public class MockMqSendReceiveTest {

    @Test
    public void messageSentByCall() {
        MyMessageV1_0 expected = new MyMessageV1_0(LocalDate.now(), "brillefin");
        TestServiceRunner.defaults(serviceConfig)
                .chain()
                .call(MyResource.class, it -> it.addToQueue(expected))
                .injectee(MqMock.class, mq -> {
                    List<MyMessageV1_0> queueContents = mq.getQueueContents(MyServiceDefinition.myMessageV1);
                    assertThat(queueContents).isEqualTo(ImmutableList.of(expected));
                })
                .run();
    }

    @Test
    public void messageIsSent() {
        MyMessageV1_0 expected = new MyMessageV1_0(LocalDate.now(), "brillefin");
        TestServiceRunner.defaults(serviceConfig)
                .chain()
                .message(MyMessageV1_0.class, sender -> sender.send(expected))
                .injectee(MqMock.class, mq -> {
                    List<MyMessageV1_0> queueContents = mq.getQueueContents(MyServiceDefinition.myMessageV1);
                    assertThat(queueContents).isEqualTo(ImmutableList.of(expected));
                })
                .run();
    }

    final MyHandler messageHandler = mock(MyHandler.class);

    ServiceConfig serviceConfig = ServiceConfig.defaults(MY_SERVICE)
            .addon(MqAddon.defaults
                    .listen(MyServiceDefinition.myMessageV1, MyHandler.class)
                    .send(MY_SERVICE)
            )
            .addon(MqMockAddon.defaults)
            .addon(ObosLogFilterAddon.defaults)
            .bind(messageHandler, MyHandler.class)
            .bind(MyResourceImpl.class, MyResource.class);


    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    @Getter
    @Setter
    public static class MyMessageV1_0 {
        public LocalDate time;
        public String string;
    }


    public interface MyHandler extends MessageHandler<MyMessageV1_0> {
    }


    @Path("banan")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public interface MyResource {
        @POST
        void addToQueue(MyMessageV1_0 messageV1);
    }


    public static class MyResourceImpl implements MyResource {
        final MessageSender<MyMessageV1_0> myMessageV1MessageSender;

        @Inject
        MyResourceImpl(MessageSender<MyMessageV1_0> myMessageV1MessageSender) {
            this.myMessageV1MessageSender = myMessageV1MessageSender;
        }



        public void addToQueue(MyMessageV1_0 messageV1) {
            MDC.put(Constants.X_OBOS_REQUEST_ID, UUID.randomUUID().toString());
            myMessageV1MessageSender.send(messageV1);
            MDC.remove(Constants.X_OBOS_REQUEST_ID);
        }
    }


    @Getter
    static class MyServiceDefinition implements ServiceDefinition {
        final String name = "Banan";
        final ImmutableList<MessageDescription<?>> handledMessages = ImmutableList.of(myMessageV1);
        final ImmutableList resources = ImmutableList.of(MyResource.class);

        public final static MessageDescription<MyMessageV1_0> myMessageV1 = MessageDescription.<MyMessageV1_0>builder()
                .description("For test")
                .MessageType(MyMessageV1_0.class)
                .name("myMessage")
                .version("1.0")
                .build();

        final static MyServiceDefinition MY_SERVICE = new MyServiceDefinition();
    }

}
