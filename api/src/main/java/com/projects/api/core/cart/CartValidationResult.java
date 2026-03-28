package com.projects.api.core.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationResult {
    private String cartId;
    private boolean valid;
    private List<String> issues;
    private String checkedAt;
}
