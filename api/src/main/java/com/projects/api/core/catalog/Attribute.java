package com.projects.api.core.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    private String attributeId;
    private String name;
    private String value;
}
