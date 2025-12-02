package com.products.demo.Controller;
import com.products.demo.Model.Products;
import com.products.demo.Service.ProductsService;
import com.products.demo.exception.ProductException;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/products")
public class ProductsController {
    private static final Logger logger = LogManager.getLogger(ProductsController.class);

    private final ProductsService productsService;

    @Autowired
    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public ResponseEntity<List<Products>> getAllProducts() {
        java.util.List<com.products.demo.Model.Products> products = productsService.getAllProducts();for (int i = 0; i < products.size(); i++) {com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),"READ","/api/products",products.get(i).getId());};
        logger.info("Fetching all products");
        return ResponseEntity.ok(productsService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable
    Long id) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "READ", "/api/products/{id}", (java.lang.Long)id);;
        logger.info("Fetching product with ID: {}", id);
        try {
            return ResponseEntity.ok(productsService.getProductById(id));
        } catch (ProductException e) {
            logger.error("Failed to fetch product with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Products> addProduct(@Valid
    @RequestBody
    Products product) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/products", null);;
        logger.info("Adding new product: {}", product.getName());
        try {
            return new ResponseEntity<>(productsService.addProduct(product), HttpStatus.CREATED);
        } catch (ProductException e) {
            logger.error("Failed to add product: {}. Error: {}", product.getName(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Products> updateProduct(@PathVariable
    Long id, @Valid
    @RequestBody
    Products product) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/products/{id}", (java.lang.Long)id);;
        logger.info("Updating product with ID: {}", id);
        try {
            return ResponseEntity.ok(productsService.updateProduct(id, product));
        } catch (ProductException e) {
            logger.error("Failed to update product with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable
    Long id) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/products/{id}", (java.lang.Long)id);;
        logger.info("Deleting product with ID: {}", id);
        try {
            productsService.deleteProduct(id);
            logger.debug("Product with ID: {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (ProductException e) {
            logger.error("Failed to delete product with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<String> handleProductException(ProductException ex) {
        logger.error("Product exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/expensive")
    public ResponseEntity<List<Products>> findExpensiveProducts(@RequestParam("priceThreshold")
    double priceThreshold) {
        java.util.List<com.products.demo.Model.Products> products = productsService.findExpensiveProducts(priceThreshold);for (int i = 0; i < products.size(); i++) {com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),"EXPENSIVE_SEARCH","/api/products/expensive",products.get(i).getId());};
        logger.info("Request to find products with price above: {}", priceThreshold);
        try {
            List<Products> products = productsService.findExpensiveProducts(priceThreshold);
            logger.debug("Found {} expensive products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Failed to fetch expensive products. Error: {}", e.getMessage());
            throw e;
        }
    }
}