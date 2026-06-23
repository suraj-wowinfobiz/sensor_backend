package com.wowinfobiz.authenticationservice.security;

public interface OtpSender {
    void sendOtp(String email, String otp, long ttlSeconds);
}
