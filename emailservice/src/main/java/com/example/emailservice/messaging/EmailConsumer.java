package com.example.emailservice.messaging;


import com.example.emailservice.config.RabbitMQConfig;
import com.example.emailservice.dto.request.StatusEmailDto;
import com.example.emailservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class EmailConsumer {
    private final EmailService emailService;
    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailRequest(StatusEmailDto statusEmailDto){
        try{
            log.info("Received Status Email Request {}. Email Service is processing the email request!", statusEmailDto);
            emailService.processingEmail(statusEmailDto);
        }
        catch (Exception e){
            throw e;
        }
    }
}
