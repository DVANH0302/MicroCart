package com.example.emailservice.service.impl;


import com.example.emailservice.dto.request.StatusEmailDto;
import com.example.emailservice.entity.Email;
import com.example.emailservice.entity.EmailStatus;
import com.example.emailservice.repository.EmailRepository;
import com.example.emailservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {


    private final EmailRepository emailRepository;

    public EmailServiceImpl(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @Async
    @Override
    public void processingEmail(StatusEmailDto statusEmailDto) {


        try {
            log.info("processingEmail {}", statusEmailDto);
            Email email = Email.Builder.newBuilder()
                    .recipient(statusEmailDto.getRecipient())
                    .subject(statusEmailDto.getSubject())
                    .body(statusEmailDto.getBody())
                    .type(statusEmailDto.getEmailType())
                    .status(EmailStatus.PENDING)
                    .build();
            emailRepository.save(email);

            // simulating sending email
            Thread.sleep(2000);
            email.setStatus(EmailStatus.SENT);
            emailRepository.save(email);
            log.info("""
                    \n
                    ###########################
                    EMAIL SENT TO: {} 
                    SUBJECT: {}
                    BODY: {}
                    ###########################
                    """, email.getRecipient(), email.getSubject(), email.getBody());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

    }

}
