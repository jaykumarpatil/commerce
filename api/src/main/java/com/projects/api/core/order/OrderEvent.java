package com.projects.api.core.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventId;
    private String orderId;
    private String eventType;
    private OrderStatus status;
    private String message;
    private LocalDateTime createdAt;
}
