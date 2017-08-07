package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.mq.ActiveMqSender;
import no.obos.util.servicebuilder.mq.MessageQueueSender;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActiveMqSenderAddon implements Addon {
    public static final int MAX_QUEUE_ENTRIES = 1000;

    public static final String CONFIG_KEY_URL = "queue.url";
    public static final String CONFIG_KEY_USER = "queue.user";
    public static final String CONFIG_KEY_PASSWORD = "queue.password";
    public static final String CONFIG_KEY_QUEUE = "queue.name";
    public static final String CONFIG_KEY_ENTRIES_GRACE = "queue.entries.grace";

    @Wither(AccessLevel.PRIVATE)
    public final MessageQueueSender mqSender;


    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    public final String url;
    @Wither(AccessLevel.PRIVATE)
    public final String user;
    @Wither(AccessLevel.PRIVATE)
    public final String password;
    @Wither(AccessLevel.PRIVATE)
    public final String queue;
    @Wither(AccessLevel.PRIVATE)
    public final int queueEntriesGrace;
    @Wither(AccessLevel.PRIVATE)
    public final boolean registerHealthcheck;

    public static ActiveMqSenderAddon defaults = new ActiveMqSenderAddon(null, null, null, null, null,  null, 60, true);


    @Override
    public ActiveMqSenderAddon initialize(ServiceConfig serviceConfig) {
        return this.withMqSender(new ActiveMqSender(url, user, password, queue));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder((binder) -> {
            if (Strings.isNullOrEmpty(name)) {
                binder.bind(this.mqSender).to(MessageQueueSender.class);
            } else {
                binder.bind(this.mqSender).named(name).to(MessageQueueSender.class);
            }
        });
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        if (registerHealthcheck) {
            ObosHealthCheckRegistry.registerActiveMqCheck("Sender queue: " + queue + " on " + url,
                    url, queue, MAX_QUEUE_ENTRIES, queueEntriesGrace,
                    user, password);
        }
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";

        properties.failIfNotPresent(
                prefix + CONFIG_KEY_URL,
                prefix + CONFIG_KEY_USER,
                prefix + CONFIG_KEY_PASSWORD,
                prefix + CONFIG_KEY_QUEUE,
                prefix + CONFIG_KEY_ENTRIES_GRACE
        );

        return this
                .url(properties.get(prefix + CONFIG_KEY_URL))
                .user(properties.get(prefix + CONFIG_KEY_USER))
                .password(properties.get(prefix + CONFIG_KEY_PASSWORD))
                .queue(properties.get(prefix + CONFIG_KEY_QUEUE))
                .queueEntriesGrace(Integer.parseInt(properties.get(prefix + CONFIG_KEY_ENTRIES_GRACE)))
                ;
    }

    public ActiveMqSenderAddon mqSender (MessageQueueSender mqSender){return withMqSender(mqSender);}

    public ActiveMqSenderAddon name (String name){return withName(name);}

    public ActiveMqSenderAddon url (String url){return withUrl(url);}

    public ActiveMqSenderAddon user (String user){return withUser(user);}

    public ActiveMqSenderAddon password (String password){return withPassword(password);}

    public ActiveMqSenderAddon queue (String queue){return withQueue(queue);}

    public ActiveMqSenderAddon queueEntriesGrace (int queueEntriesGrace){return withQueueEntriesGrace(queueEntriesGrace);}
}
