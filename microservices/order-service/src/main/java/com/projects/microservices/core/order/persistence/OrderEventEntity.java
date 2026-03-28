package com.projects.microservices.core.order.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("order_events")
public class OrderEventEntity {
    @Id
    private Long id;
    private String eventId;
    private String orderId;
    private String eventType;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}
