package com.example.deliveryco.config;


import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.messaging.CustomCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    //  QUEUE
    public static final String DELIVERY_REQUEST_QUEUE = "delivery_request_queue";
    public static final String DELIVERY_REQUEST_DLQ = "delivery_request_dlq";
    public static final String DELIVERY_UPDATE_QUEUE = "delivery_update_queue";
    public static final String DELIVERY_UPDATE_DLQ =  "delivery_update_dlq";
    public static final String EMAIL_QUEUE = "email_queue";

    // EXCHANGE
    public static final String STORE_EXCHANGE = "store_exchange";
    public static final String STORE_DLX = "store_dlx";
    public static final String DELIVERYCO_DLX = "deliveryco_dlx";

    // ROUTING KEYS
    public static final String DELIVERY_REQUEST_KEY = "delivery.request";
    public static final String DELIVERY_UPDATE_KEY = "delivery.update.#";
    public static final String DELIVERY_REQUEST_DLQ_KEY = "delivery.request.dlq";
    public static final String DELIVERY_UPDATE_DLQ_KEY = "delivery.update.dlq";
    public static final String EMAIL_KEY = "email_key";

    // specific delivery key
    public static final String DELIVERY_UPDATE_RECIEVED =  "delivery.update.recieved";
    public static final String DELIVERY_UPDATE_PICKEDUP = "delivery.update.pickedup";
    public static final String DELIVERY_UPDATE_ON_DELIVERY = "delivery.update.ondelivery";
    public static final String DELIVERY_UPDATE_LOST = "delivery.update.lost";
    public static final String DELIVERY_UPDATE_DELIVERED = "delivery.update.delivered";

    // store exchange and update_dlq exchange
    @Bean
    public TopicExchange storeExchange() {
        return new TopicExchange(STORE_EXCHANGE);
    }

    @Bean
    public DirectExchange storeDlx() {
        return new DirectExchange(STORE_DLX);
    }

    @Bean
    public DirectExchange deliveryDlx() {
        return new DirectExchange(DELIVERYCO_DLX);
    }


    // Queue 1 - delivery request, durable
    @Bean
    public Queue deliveryRequestQueue() {

        return QueueBuilder.durable(DELIVERY_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", DELIVERYCO_DLX)
                .withArgument("x-dead-letter-routing-key", DELIVERY_REQUEST_DLQ_KEY)
                .build();
    }
    @Bean
    public Binding deliveryRequestBinding(Queue deliveryRequestQueue, TopicExchange storeExchange) {
        return BindingBuilder
                .bind(deliveryRequestQueue)
                .to(storeExchange)
                .with(DELIVERY_REQUEST_KEY);
    }


    // Queue 2 - delivery update, durable
    @Bean
    public Queue deliveryUpdateQueue() {

        return QueueBuilder.durable(DELIVERY_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", STORE_DLX)
                .withArgument("x-dead-letter-routing-key", DELIVERY_UPDATE_DLQ_KEY)
                .build();
    }

    @Bean
    public Binding deliveryUpdateBinding(Queue deliveryUpdateQueue, TopicExchange storeExchange) {
        return BindingBuilder
                .bind(deliveryUpdateQueue)
                .to(storeExchange)
                .with(DELIVERY_UPDATE_KEY);
    }


    // Queue 3 - email queue - fire and forget
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, false);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange storeExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(storeExchange)
                .with(EMAIL_KEY);
    }

    // Queue 4 - store update dlq
    @Bean
    public Queue deliveryUpdateDlq(){
        return QueueBuilder.durable(DELIVERY_UPDATE_DLQ).build();
    }

    @Bean
    public Binding deliveryUpdateDlqBinding(Queue deliveryUpdateDlq, @Qualifier("storeDlx") DirectExchange storeDlx) {
        return BindingBuilder
                .bind(deliveryUpdateDlq)
                .to(storeDlx)
                .with(DELIVERY_UPDATE_DLQ_KEY);
    }


    // Queue 5 - delivery request dlq
    @Bean
    public Queue deliveryRequestDlq() {
        return QueueBuilder.durable(DELIVERY_REQUEST_DLQ).build();
    }

    @Bean
    public Binding deliveryRequestDlqBinding(Queue deliveryRequestDlq, @Qualifier("deliveryDlx") DirectExchange deliveryDlx) {
        return BindingBuilder
                .bind(deliveryRequestDlq)
                .to(deliveryDlx)
                .with(DELIVERY_REQUEST_DLQ_KEY);
    }


    // POJO to json
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Rabbit template
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                if (correlationData instanceof CustomCorrelationData) {
                    CustomCorrelationData data = (CustomCorrelationData) correlationData;
                    log.info("GLOBAL: RabbitMQ ACK received {}", data.getId());
                } else {
                    log.info("GLOBAL: RabbitMQ ACK received correlationData={}", correlationData);
                }
                return;
            } else {
                if (correlationData instanceof CustomCorrelationData) {
                    CustomCorrelationData data = (CustomCorrelationData) correlationData;
                    log.error("GLOBAL: RabbitMQ NOT ACK received {} cause={}", data.getId(), cause);
                } else {
                    log.error("GLOBAL: RabbitMQ NOT ACK received correlationData={} cause={}", correlationData, cause);
                }
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("Returned unroutable exchange={} routingKey={} message={}", returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });

        return rabbitTemplate;
    }
    // helper
    public static String getStatusKey(DeliveryStatus status) {
        String key = switch (status) {
            case RECEIVED -> DELIVERY_UPDATE_RECIEVED;
            case PICKED_UP -> DELIVERY_UPDATE_PICKEDUP;
            case ON_DELIVERY -> DELIVERY_UPDATE_ON_DELIVERY;
            case LOST  -> DELIVERY_UPDATE_LOST;
            case DELIVERED -> DELIVERY_UPDATE_DELIVERED;
            default -> throw  new IllegalArgumentException("Unknown status " + status);
        };
        return key;
    }

    public static String getStatusMessage(DeliveryStatus status) {
        String message = switch (status) {
            case RECEIVED -> "Your delivery order has been RECIEVED!";
            case PICKED_UP ->  "Your delivery order has been PICKED_UP!";
            case ON_DELIVERY -> "Your delivery order has been ON_DELIVERY!";
            case LOST  -> "Your delivery order has been LOST!";
            case DELIVERED -> "Your delivery order has been DELIVERED!";
            case CANCELLED ->  "Your delivery order has been cancelled!";
        };
        return  message;
    }

}
