package com.hireconnect.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hireconnect.auth.dto.EmailRequest;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/notifications/email")
    void sendEmail(@RequestBody EmailRequest request);
}