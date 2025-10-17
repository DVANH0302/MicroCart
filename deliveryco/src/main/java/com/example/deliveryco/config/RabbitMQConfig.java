package com.example.deliveryco.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    //  QUEUE
    public static final String DELIVERY_REQUEST_QUEUE = "delivery_request_queue";
    public static final String DELIVERY_UPDATE_QUEUE = "delivery_update_queue";
    public static final String EMAIL_QUEUE = "email_queue";

    // EXCHANGE
    public static final String STORE_EXCHANGE = "store_exchange";

    // ROUTING KEYS
    public static final String DELIVERY_REQUEST_KEY = "delivery_request_key";
    public static final String DELIVERY_UPDATE_KEY = "delivery_update_key";
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
