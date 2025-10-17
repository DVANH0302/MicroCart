package com.example.store.config;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {




    //  QUEUE
    public static final String DELIVERY_REQUEST_QUEUE = "delivery_request_queue";
    public static final String DELIVERY_UPDATE_QUEUE = "delivery_update_queue";
    public static final String STORE_DELIVERY_EVENT_QUEUE = "store_delivery_event_queue";
    public static final String EMAIL_QUEUE = "email_queue";

    // EXCHANGE
    public static final String STORE_EXCHANGE = "store_exchange";

    // ROUTING KEYS
    public static final String DELIVERY_REQUEST_KEY = "delivery.request";
    public static final String DELIVERY_UPDATE_KEY = "delivery.update";
    public static final String DELIVERY_REQUEST_REJECT_KEY  = "delivery.reject";
    public static final String EMAIL_KEY = "email_key";


    // store exchange
    @Bean
    public TopicExchange storeExchange() {
        return new TopicExchange(STORE_EXCHANGE);
    }

    // Queue 1 - delivery request, durable
    @Bean
    public Queue deliveryRequestQueue() {
        return QueueBuilder.durable(DELIVERY_REQUEST_QUEUE).build();
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
        return QueueBuilder.durable(DELIVERY_UPDATE_QUEUE).build();
    }

    @Bean
    public Binding deliveryUpdateBinding(Queue deliveryUpdateQueue, TopicExchange storeExchange) {
        return BindingBuilder
                .bind(deliveryUpdateQueue)
                .to(storeExchange)
                .with(DELIVERY_UPDATE_KEY);
    }

    // Queue 3 - store delivery event, durable, deliveryco send failed business logic message to store
    @Bean
    public Queue storeDeliveryEventQueue() {
        return QueueBuilder.durable(STORE_DELIVERY_EVENT_QUEUE).build();
    }

    @Bean
    public Binding deliveryRejectBinding(Queue storeDeliveryEventQueue, TopicExchange storeExchange) {
        return BindingBuilder
                .bind(storeDeliveryEventQueue)
                .to(storeExchange)
                .with(DELIVERY_REQUEST_REJECT_KEY);
    }


    // Queue 4 - email queue - fire and forget
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
        return rabbitTemplate;
    }


}
