package com.products.demo.repository;
import com.products.demo.Model.Products;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductsRepository extends JpaRepository<Products, Long> {
    List<Products> findByPriceGreaterThan(double priceThreshold);
}