package com.example.ecom.app.controller;

import com.example.ecom.shared.api.CatalogService;
import com.example.ecom.shared.api.ProductDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {
  private final CatalogService catalogService;

  public CatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping("/products")
  public List<ProductDTO> listProducts() {
    return catalogService.listProducts(0, 20);
  }
}
