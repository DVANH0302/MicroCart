package com.example.emailservice.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails",schema = "email")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private EmailType type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Email() {}

    private Email(Builder builder) {
        recipient = builder.recipient;
        subject = builder.subject;
        body = builder.body;
        type = builder.type;
        status = builder.status;
    }


    public static final class Builder {
        private String recipient;
        private String subject;
        private String body;
        private EmailType type;
        private EmailStatus status;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder recipient(String val) {
            recipient = val;
            return this;
        }

        public Builder subject(String val) {
            subject = val;
            return this;
        }

        public Builder body(String val) {
            body = val;
            return this;
        }

        public Builder type(EmailType val) {
            type = val;
            return this;
        }

        public Builder status(EmailStatus val) {
            status = val;
            return this;
        }

        public Email build() {
            return new Email(this);
        }
    }

    public int getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public EmailType getType() {
        return type;
    }

    public void setType(EmailType type) {
        this.type = type;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public void setStatus(EmailStatus status) {
        this.status = status;
    }
}
