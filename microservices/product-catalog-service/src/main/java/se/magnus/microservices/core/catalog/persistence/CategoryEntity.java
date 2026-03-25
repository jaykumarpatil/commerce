package se.magnus.microservices.core.catalog.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("categories")
public class CategoryEntity {
    @Id
    private Long id;
    private String categoryId;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private Integer sortOrder;
    private boolean active;
    private String createdAt;
    private String updatedAt;
}
