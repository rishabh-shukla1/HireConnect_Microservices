package com.hireconnect.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent implements Serializable {

    private UUID userId;
    private String email;
    private String role;
    private String mobileNumber;
}