package com.example.ecom.catalog;

import com.example.ecom.shared.api.CatalogService;
import com.example.ecom.shared.api.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class CatalogServiceImpl implements CatalogService {
  private static final List<ProductDTO> SEED_PRODUCTS = Arrays.asList(
      new ProductDTO(1L, "Widget Pro", "A versatile widget", Map.of("color", "red", "rating", 4.6)),
      new ProductDTO(2L, "Gizmo X", "An advanced gizmo", Map.of("color", "blue", "rating", 4.2))
  );

  @Override
  public List<ProductDTO> listProducts(int page, int size) {
    if (size <= 0) size = 20;
    if (page < 0) page = 0;
    int from = page * size;
    return SEED_PRODUCTS.stream()
        .skip(from)
        .limit(size)
        .collect(Collectors.toList());
  }
}
