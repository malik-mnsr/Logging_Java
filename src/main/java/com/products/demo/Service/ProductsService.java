package com.products.demo.Service;

import com.products.demo.Model.Products;
import com.products.demo.exception.ProductException;
import com.products.demo.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    // Fetch all products
    public List<Products> getAllProducts() {
        return productsRepository.findAll();
    }

    // Fetch a product by ID
    public Products getProductById(Long id) {
        return productsRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product with ID " + id + " not found"));
    }

    // Add a new product
    public Products addProduct(Products product) {
        if (product.getId() != null && productsRepository.existsById(product.getId())) {
            throw new ProductException("Product with ID " + product.getId() + " already exists");
        }
        return productsRepository.save(product);
    }

    // Update a product's info
    public Products updateProduct(Long id, Products updatedProduct) {
        if (!productsRepository.existsById(id)) {
            throw new ProductException("Product with ID " + id + " not found");
        }
        updatedProduct.setId(id); // Ensure the ID remains the same
        return productsRepository.save(updatedProduct);
    }

    // Delete a product by ID
    public void deleteProduct(Long id) {
        if (!productsRepository.existsById(id)) {
            throw new ProductException("Product with ID " + id + " not found");
        }
        productsRepository.deleteById(id);
    }
}
