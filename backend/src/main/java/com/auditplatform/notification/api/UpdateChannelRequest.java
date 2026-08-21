package com.auditplatform.notification.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateChannelRequest(
        Boolean enabled,
        @Email @Size(max = 255) String fromAddress
) {
}
