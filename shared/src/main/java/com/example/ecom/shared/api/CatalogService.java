package com.projects.ecom.shared.api;

import java.util.List;

public interface CatalogService {
  List<ProductDTO> listProducts(int page, int size);
}
