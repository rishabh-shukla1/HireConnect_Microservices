package com.hireconnect.interview.client;

import com.hireconnect.interview.config.FeignConfig;
import com.hireconnect.interview.dto.StatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "application-service",
        configuration = FeignConfig.class
)
public interface ApplicationServiceClient {

    @PutMapping("/applications/{applicationId}/status")
    void updateStatus(
            @PathVariable("applicationId") UUID applicationId,
            @RequestBody StatusUpdateRequest request
    );
}