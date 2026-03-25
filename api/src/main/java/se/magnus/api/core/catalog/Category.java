package se.magnus.api.core.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
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
