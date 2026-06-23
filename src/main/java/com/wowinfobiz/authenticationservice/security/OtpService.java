package com.wowinfobiz.authenticationservice.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final long otpTtlSeconds;
    private final OtpSender otpSender;

    public OtpService(
            @Value("${security.otp.ttl-seconds}") long otpTtlSeconds,
            OtpSender otpSender
    ) {
        this.otpTtlSeconds = otpTtlSeconds;
        this.otpSender = otpSender;
    }

    public void sendOtp(String email) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(otpTtlSeconds);
        otpStore.put(email, new OtpEntry(otp, expiresAt));
        otpSender.sendOtp(email, otp, otpTtlSeconds);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email);
            return false;
        }
        if (!entry.otp().equals(otp)) {
            return false;
        }
        otpStore.remove(email);
        return true;
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private record OtpEntry(String otp, Instant expiresAt) {
    }
}
