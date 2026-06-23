package com.wowinfobiz.authenticationservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpOtpSender implements OtpSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpOtpSender(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendOtp(String email, String otp, long ttlSeconds) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Super Admin OTP");
        message.setText("Your OTP is " + otp + ". It is valid for " + ttlSeconds + " seconds.");
        mailSender.send(message);
    }
}
