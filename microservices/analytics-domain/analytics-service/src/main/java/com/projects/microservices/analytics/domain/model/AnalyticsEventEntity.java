package com.projects.microservices.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "analytics_events")
public class AnalyticsEventEntity {
    @Id
    private String id;
    private String eventId;
    private String eventType;
    private String userId;
    private String productId;
    private String orderId;
    private java.time.Instant timestamp;
    private java.util.Map<String, Object> properties;
}
