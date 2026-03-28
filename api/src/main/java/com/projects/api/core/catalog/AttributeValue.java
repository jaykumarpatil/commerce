package com.projects.api.core.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {
    private String attributeId;
    private String attributeName;
    private String value;
}
