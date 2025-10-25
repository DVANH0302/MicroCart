package com.example.store.config;


import com.example.store.entity.EmailType;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    public static String getSubject(EmailType emailType, Integer orderId) {
        return String.format("Order %d %s",  orderId, emailType.name());
    }

    public static String getBody(EmailType emailType, Integer orderId) {
        if (emailType == EmailType.LOST) {
            return String.format("""
                Your order id %d has been %s. You will get refund for your order!"
                """,orderId,emailType.name());
        }

        if (emailType == EmailType.CANCELLED) {
            return String.format("""
                Your order id %d has been %s. You will get refund for your order!"
                """,orderId,emailType.name());
        }
        if (emailType == EmailType.FAILED_PROCESSING) {
            return String.format("""
                ALERT! Your order id %d has been %s due to our system error. Our team will contact with
                you soon to resolve this issue!"
                """,orderId,emailType.name());
        }


        return String.format("""
                Your order id %d has been %s. You can view the details on the website"
                """,orderId,emailType.name());
    }
}
