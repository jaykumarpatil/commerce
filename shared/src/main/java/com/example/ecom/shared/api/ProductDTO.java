package com.example.ecom.shared.api;

import java.util.Map;

public record ProductDTO(Long id, String name, String description, Map<String, Object> attributes) {}
