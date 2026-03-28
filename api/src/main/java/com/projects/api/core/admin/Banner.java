package com.projects.api.core.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banner {
    private String bannerId;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private boolean active;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private String createdAt;
}
