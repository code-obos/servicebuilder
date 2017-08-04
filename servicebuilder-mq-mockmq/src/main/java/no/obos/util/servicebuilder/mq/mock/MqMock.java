package no.obos.util.servicebuilder.mq.mock;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.mq.MqHandlerForwarder;
import no.obos.util.servicebuilder.mq.MqHandlerImpl;
import no.obos.util.servicebuilder.mq.MqListener;
import no.obos.util.servicebuilder.mq.MqMessage;
import no.obos.util.servicebuilder.mq.MqTextSender;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Streams.stream;

/**
 * Mock queue intended for unit testing. Not intended to be run as a listening thread.
 */
@Slf4j
public class MqMock implements MqTextSender, MqListener {
    final Map<String, MessageDescription<?>> senderDescriptions;
    final LinkedBlockingQueue<QueuedMessage> listeningQueue = Queues.newLinkedBlockingQueue();
    final ImmutableMap<String, CopyOnWriteArrayList<String>> sendingQueues;
    final ImmutableSet<String> listeningQueueNames;
    ImmutableMap<String, MqHandlerImpl<?>> handlers;
    final MqHandlerForwarder mqHandlerForwarder;
    boolean running = true;
    Thread handlerThread = null;
    Throwable threadException = null;


    @Builder
    public MqMock(
            Iterable<MessageDescription> listenMessageDescriptions,
            Iterable<MessageDescription<?>> senderDescriptions,
            MqHandlerForwarder mqHandlerForwarder)
    {
        this.mqHandlerForwarder = mqHandlerForwarder;

        this.senderDescriptions = ImmutableMap.copyOf(
                stream(senderDescriptions)
                        .distinct()
                        .collect(Collectors.toMap(MessageDescription::getQueueName, Function.identity()))
        );

        sendingQueues = ImmutableMap.copyOf(
                stream(senderDescriptions)
                        .collect(Collectors.toMap(MessageDescription::getQueueName, it -> Lists.newCopyOnWriteArrayList()))
        );

        listeningQueueNames = stream(listenMessageDescriptions)
                .map(MessageDescription::getQueueName)
                .collect(GuavaHelper.setCollector());

    }


    @Override
    public void startListener(ImmutableSet<MqHandlerImpl<?>> handlers) {
        this.handlers = ImmutableMap.copyOf(
                stream(handlers)
                        .collect(Collectors.toMap(
                                it -> it.handlerDescription.messageDescription.getQueueName(),
                                Function.identity()
                                )
                        )
        );
        startListener();
    }

    @Override
    public void queueMessage(String messageText, String queue) {
        sendingQueues.get(queue).add(messageText);
        if (listeningQueueNames.contains(queue)) {
            QueuedMessage queuedMessage = new QueuedMessage(queue, messageText);
            boolean added = listeningQueue.offer(queuedMessage);
            if (! added) {
                throw new RuntimeException("Could not add to queue");
            }
        }
    }

    private void startListener() {
        if (! handlers.isEmpty()) {
            running = true;
            Runnable runnable = () -> {
                while (running) {
                    try {
                        QueuedMessage message;
                        do {
                            message = listeningQueue.poll(50, TimeUnit.MILLISECONDS);
                            if (message != null) {
                                forward(handlers.get(message.queue), message.message);
                            }
                        } while (message != null);
                    } catch (InterruptedException e) {
                        running = false;
                        Thread.currentThread().interrupt();
                    }
                }
            };
            handlerThread = new Thread(runnable);
            handlerThread.setUncaughtExceptionHandler((thread, e) -> {
                        if (thread.equals(handlerThread)) {
                            threadException = e;
                        }
                    }
            );

            handlerThread.start();
        }
    }

    private <T> void forward(MqHandlerImpl<T> handlerImpl, String messageText) {
        mqHandlerForwarder.forward(handlerImpl, messageText);
    }

    public <T> List<T> getQueueContents(MessageDescription<T> messageDescription) {
        @SuppressWarnings("unchecked")
        MessageDescription<T> senderDescription = (MessageDescription<T>) senderDescriptions.get(messageDescription.getQueueName());
        JavaType javaType = messageDescription.jsonConfig.get().getTypeFactory().constructParametricType(MqMessage.class, messageDescription.MessageType);
        return sendingQueues.get(messageDescription.getQueueName()).stream()
                .map(it -> parseMessage(senderDescription, it))
                .map(it -> it.content)
                .collect(Collectors.toList());
    }

    private <T> MqMessage<T> parseMessage(MessageDescription<T> messageDescription, String messageText) {
        JavaType javaType = messageDescription.jsonConfig.get().getTypeFactory().constructParametricType(MqMessage.class, messageDescription.MessageType);
        try {
            return messageDescription.jsonConfig.get().readValue(messageText, javaType);
        } catch (IOException e) {
            log.error("Problem parsing text of message."
                    + "\nType of message: " + messageDescription.MessageType.getName()
                    + "\nMessage: " + messageText);
            throw new RuntimeException("Problem parsing message text");
        }
    }



    /**
     * Finishes all waiting messages and stops listener.
     */
    public void stop() {
        if (handlerThread != null) {
            running = false;
            try {
                handlerThread.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (threadException != null) {
                if (threadException instanceof Error) {
                    throw (Error) threadException;
                }
                if (threadException instanceof RuntimeException) {
                    throw (RuntimeException) threadException;
                } else {
                    throw new RuntimeException(threadException);
                }
            }
        }
    }

    /**
     * To accomodate unit tests, this method drains current queue and returns
     */
    public void finishWork() {
        stop();
        startListener();
    }


    @AllArgsConstructor
    static class QueuedMessage {
        public final String queue;
        public final String message;
    }
}
