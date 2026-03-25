package se.magnus.microservices.core.admin.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("banners")
public class BannerEntity {
    @Id
    private Long id;
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
