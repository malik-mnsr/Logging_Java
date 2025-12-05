package com.products.demo.Service;
import com.products.demo.Model.Products;
import com.products.demo.exception.ProductException;
import com.products.demo.repository.ProductsRepository;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ProductsService {
    private static final Logger logger = LogManager.getLogger(ProductsService.class);

    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    public List<Products> getAllProducts() {
        logger.info("Fetching all products from repository");
        return productsRepository.findAll();
    }

    public Products getProductById(Long id) {
        logger.info("Fetching product with ID: {}", id);
        return productsRepository.findById(id).orElseThrow(() -> {
            logger.error("Product with ID: {} not found", id);
            return new ProductException(("Product with ID " + id) + " not found");
        });
    }

    public Products addProduct(Products product) {
        logger.info("Adding product: {}", product.getName());
        if ((product.getId() != null) && productsRepository.existsById(product.getId())) {
            logger.error("Product with ID: {} already exists", product.getId());
            throw new ProductException(("Product with ID " + product.getId()) + " already exists");
        }
        Products savedProduct = productsRepository.save(product);
        logger.debug("Product saved: {}", savedProduct.getName());
        return savedProduct;
    }

    public Products updateProduct(Long id, Products updatedProduct) {
        logger.info("Updating product with ID: {}", id);
        if (!productsRepository.existsById(id)) {
            logger.error("Product with ID: {} not found for update", id);
            throw new ProductException(("Product with ID " + id) + " not found");
        }
        updatedProduct.setId(id);
        Products savedProduct = productsRepository.save(updatedProduct);
        logger.debug("Product updated: {}", savedProduct.getName());
        return savedProduct;
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        if (!productsRepository.existsById(id)) {
            logger.error("Product with ID: {} not found for deletion", id);
            throw new ProductException(("Product with ID " + id) + " not found");
        }
        productsRepository.deleteById(id);
        logger.debug("Product with ID: {} deleted", id);
    }

    public List<Products> findExpensiveProducts(double priceThreshold) {
        logger.info("Fetching products with price above: {}", priceThreshold);
        List<Products> products = productsRepository.findByPriceGreaterThan(priceThreshold);
        logger.debug("Found {} products above price threshold: {}", products.size(), priceThreshold);
        return products;
    }
}